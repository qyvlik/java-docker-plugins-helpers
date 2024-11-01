package io.github.qyvlik.jdph.examples.coffer.fetcher;

import io.github.qyvlik.jdph.examples.coffer.fetcher.aws.AwsCredential;
import io.github.qyvlik.jdph.examples.coffer.fetcher.aws.sm.AwsSecretsManagerFetcher;
import io.github.qyvlik.jdph.examples.coffer.fetcher.git.GitFetcher;
import io.github.qyvlik.jdph.examples.coffer.fetcher.git.SSHKeyCredential;
import io.github.qyvlik.jdph.examples.coffer.fetcher.http.HttpFetcher;
import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Map;

public class SourceSelector {
    public static final String SOURCE_URL = "source.url";

    public static final String SOURCE_PATH = "source.path";

    public static final String SOURCE_GIT_BRANCH = "source.git-branch";

    public static final String SOURCE_CONTENT_TYPE = "source.content-type";

    public static final String SOURCE_CREDENTIAL_ID = "source.credential-id";

    public static final String SOURCE_PROXY = "source.proxy";

    private final CredentialManager credentialManager;

    public SourceSelector(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    public SourceFetcher fetcher(String workspace, String volumeName, Map<String, String> parameters) {
        if (StringUtils.isBlank(workspace)) {
            throw new IllegalArgumentException("workspace is blank");
        }
        if (!StringUtils.startsWith(workspace, "/")) {
            throw new IllegalArgumentException("workspace must absolute path");
        }

        String url = parameters.get(SOURCE_URL);
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(String.format("option's %s is blank", SOURCE_URL));
        }

        ContentType contentType = ContentType.valueOf(parameters.get(SOURCE_CONTENT_TYPE));

        if (StringUtils.startsWith(url, "http://") || StringUtils.startsWith(url, "https://")) {
            String proxy = parameters.get(SOURCE_PROXY);
            return new HttpFetcher(contentType, url, proxy);
        }

        // aws+sm@${region}:${secretName}
        if (StringUtils.startsWith(url, "aws+sm@")) {
            return getAwsSecretsManagerFetcher(contentType, url, parameters);
        }

        // git
        if (StringUtils.startsWith(url, "git@")) {
            return getGitFetcher(workspace, contentType, url, parameters, volumeName);
        }

        return null;
    }


    /**
     * @param parameters
     * @param url        // aws+sm@${region}:${secretName}
     * @return
     */
    AwsSecretsManagerFetcher getAwsSecretsManagerFetcher(ContentType contentType, String url, Map<String, String> parameters) {
        String regionAndSecretName = StringUtils.removeStart(url, "aws+sm@");
        if (StringUtils.isBlank(regionAndSecretName)) {
            throw new IllegalArgumentException(String.format("aws+sm url = %s is invalidate", url));
        }
        String[] strings = regionAndSecretName.split(":");
        if (strings.length != 2) {
            throw new IllegalArgumentException(String.format("aws+sm url = %s is invalidate", url));
        }
        String region = strings[0];
        String secretName = strings[1];

        String credentialId = parameters.get(SOURCE_CREDENTIAL_ID);
        if (StringUtils.isBlank(credentialId)) {
            throw new IllegalArgumentException(String.format("aws+sm loss parameter = %s", SOURCE_CREDENTIAL_ID));
        }

        CredentialManager.Credential credential = this.credentialManager.get(credentialId);
        if (credential == null) {
            throw new IllegalArgumentException(String.format("aws+sm credential %s not found", credentialId));
        }

        if (credential instanceof AwsCredential awsCredential) {
            String proxy = parameters.get(SOURCE_PROXY);

            return new AwsSecretsManagerFetcher(
                    contentType,
                    awsCredential.accessKeyId(),
                    awsCredential.secretKeyId(),
                    region,
                    secretName,
                    proxy
            );
        } else {
            throw new IllegalArgumentException(String.format("aws+sm credential %s type not AwsCredential", credentialId));
        }
    }


    GitFetcher getGitFetcher(String workspace, ContentType contentType, String url, Map<String, String> parameters, String volumeName) {
        String gitBranch = parameters.get(SOURCE_GIT_BRANCH);
        if (StringUtils.isBlank(gitBranch)) {
            throw new IllegalArgumentException("git branch is blank");
        }

        String credentialId = parameters.get(SOURCE_CREDENTIAL_ID);
        if (StringUtils.isBlank(credentialId)) {
            throw new IllegalArgumentException(String.format("git loss parameter = %s", SOURCE_CREDENTIAL_ID));
        }

        CredentialManager.Credential credential = this.credentialManager.get(credentialId);
        if (credential == null) {
            throw new IllegalArgumentException(String.format("git credential %s not found", credentialId));
        }

        String path = parameters.get(SOURCE_PATH);
        if (credential instanceof SSHKeyCredential sshKeyCredential) {
            return new GitFetcher(
                    contentType,
                    url,
                    gitBranch,
                    path,
                    sshKeyCredential,
                    Path.of(workspace, volumeName).toString()
            );
        } else {
            throw new IllegalArgumentException(String.format("git credential %s type not SSHKeyCredential", credentialId));
        }
    }

}
