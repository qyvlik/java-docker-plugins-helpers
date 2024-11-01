package io.github.qyvlik.jdph.examples.coffer.fetcher.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import io.github.qyvlik.jdph.examples.coffer.fetcher.SourceFetcher;
import io.github.qyvlik.jdph.examples.coffer.proxy.RequestProxy;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/*
volumes:
  app_secret:
    secret.source.url: https://api.github.com/zen
    secret.source.proxy: http://192.168.1.1:8118
    secret.source.content-type: text
*/
public class HttpFetcher implements SourceFetcher {
    private final ContentType contentType;
    private final String url;
    private final HttpClient client;

    public HttpFetcher(ContentType contentType, String url, String proxy) {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType is null");
        }
        if (contentType == ContentType.yaml
                || contentType == ContentType.dir) {
            throw new IllegalArgumentException("not support to parse yaml or dir !");
        }

        this.contentType = contentType;
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url is blank");
        }
        if (!StringUtils.startsWith(url, "http://") && !StringUtils.startsWith(url, "https://")) {
            throw new IllegalArgumentException("url not start with http:// or https://");
        }

        this.url = url;
        var builder = HttpClient.newBuilder();

        if (StringUtils.isNotBlank(proxy)) {
            builder.proxy(RequestProxy.proxy(proxy));
        }

        this.client = builder.build();
    }

    @Override
    public Source get() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return Source.failure(this.contentType, e);
        }

        String val = response.body();

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

        return Source.failure(this.contentType, new IllegalStateException(String.format("%s not raw type not implements %s parser", this.url, this.contentType)));
    }
}
