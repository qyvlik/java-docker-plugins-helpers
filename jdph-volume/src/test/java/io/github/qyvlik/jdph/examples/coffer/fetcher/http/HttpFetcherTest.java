package io.github.qyvlik.jdph.examples.coffer.fetcher.http;

import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpFetcherTest {

    @Test
    public void testAsText() {
        var fetcher = new HttpFetcher(ContentType.text, "https://api.github.com/zen", "socks5://127.0.0.1:1086");
        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.string().ifPresent((s) -> {
            System.out.printf("body = %s", s);
        });
    }

    @Test
    public void testAsJson() {
        var fetcher = new HttpFetcher(ContentType.json, "https://httpbin.org/get", null);
        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.object().ifPresent((objectMap) ->
                objectMap.forEach((k, v) -> System.out.printf("%s = %s\n", k, v)));
    }
}