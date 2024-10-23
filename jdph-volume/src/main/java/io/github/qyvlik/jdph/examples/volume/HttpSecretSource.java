package io.github.qyvlik.jdph.examples.volume;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * https://www.baeldung.com/java-url-vs-uri
 * https://openjdk.org/groups/net/httpclient/intro.html
 * Only Support GET request
 * Only Support UTF-8
 */
public class HttpSecretSource implements SecretSource {

    private final SecretContentType contentType;

    private final String secret;


    public HttpSecretSource(URI uri, SecretContentType contentType) throws IOException, InterruptedException {
        if (contentType != SecretContentType.text) {
            throw new UnsupportedOperationException("not support other content type!");
        }

        this.contentType = contentType;

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(uri).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        this.secret = response.body();
    }

    @Override
    public SecretContentType contentType() {
        return this.contentType;
    }

    @Override
    public Object scope() {
        return this.secret;
    }

    @Override
    public List<Object> scopes() {
        return null;
    }
}
