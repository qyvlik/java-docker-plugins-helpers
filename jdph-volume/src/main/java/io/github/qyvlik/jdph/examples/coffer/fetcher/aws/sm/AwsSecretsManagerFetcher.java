package io.github.qyvlik.jdph.examples.coffer.fetcher.aws.sm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import io.github.qyvlik.jdph.examples.coffer.fetcher.SourceFetcher;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.HashMap;

/*
volumes:
  app_secret:
    secret.source.url: aws+sm@${region}:${secretName}
    secret.source.content-type: text
    secret.source.credential-id: AK1
    secret.source.proxy: http://192.168.1.1:8118
*/
/**
 * 1. https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_secrets-manager_code_examples.html
 * 2. https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-config-proxy-support.html
 */
public class AwsSecretsManagerFetcher implements SourceFetcher {

    private final ContentType contentType;
    private final SecretsManagerClient client;
    private final String secretName;

    public AwsSecretsManagerFetcher(ContentType contentType,
                                    String accessKeyId,
                                    String secretAccessKey,
                                    String region,
                                    String secretName,
                                    String proxyUrl) {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType is null");
        }
        if (contentType == ContentType.yaml
                || contentType == ContentType.dir) {
            throw new IllegalArgumentException("not support to parse yaml or dir !");
        }

        if (StringUtils.isBlank(accessKeyId)) {
            throw new IllegalArgumentException("accessKeyId is blank");
        }
        if (StringUtils.isBlank(secretAccessKey)) {
            throw new IllegalArgumentException("secretAccessKey is blank");
        }
        if (StringUtils.isBlank(region)) {
            throw new IllegalArgumentException("region is blank");
        }
        if (StringUtils.isBlank(secretName)) {
            throw new IllegalArgumentException("secretName is blank");
        }

        this.contentType = contentType;

        var secretsManagerClientBuilder = SecretsManagerClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of(region));

        if (StringUtils.isNotBlank(proxyUrl)) {
            secretsManagerClientBuilder.httpClientBuilder(ApacheHttpClient.builder()
                    .proxyConfiguration(ProxyConfiguration.builder()
                            .endpoint(URI.create(proxyUrl))
                            .build())
            );
        }

        this.client = secretsManagerClientBuilder.build();
        this.secretName = secretName;
    }

    @Override
    public Source get() {
        GetSecretValueRequest getRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        GetSecretValueResponse getResponse = null;
        try {
            getResponse = this.client.getSecretValue(getRequest);
        } catch (Exception e) {
            return Source.failure(this.contentType, e);
        }
        String val = getResponse.secretString();
        if (val == null) {
            return Source.failure(this.contentType, new IllegalStateException(String.format("%s not raw type not text", this.secretName)));
        }

        if (this.contentType == ContentType.text) {
            return Source.text(val);
        }

        if (this.contentType == ContentType.json) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, Object> obj = null;
            try {
                obj = mapper.readValue(val,
                        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
            } catch (Exception e) {
                return Source.failure(this.contentType, e);
            }
            return Source.success(this.contentType, obj);
        }

        return Source.failure(this.contentType, new IllegalStateException(String.format("%s not raw type not implements %s parser", this.secretName, this.contentType)));
    }

}

