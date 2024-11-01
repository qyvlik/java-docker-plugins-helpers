package io.github.qyvlik.jdph.examples.coffer.fetcher.aws.sm;

import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import org.junit.jupiter.api.Test;


class AwsSecretsManagerFetcherTest {

    @Test
    public void testAsText() {
        AwsSecretsManagerFetcher fetcher = new AwsSecretsManagerFetcher(
                ContentType.text,
                System.getenv("ak"),
                System.getenv("sk"),
                "ap-northeast-1",
                System.getenv("secret_name"),
                null
        );

        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.string().ifPresent((s) -> {
            System.out.printf("body = %s", s);
        });
    }

    @Test
    public void testAsJson() {
        AwsSecretsManagerFetcher fetcher = new AwsSecretsManagerFetcher(
                ContentType.json,
                System.getenv("ak"),
                System.getenv("sk"),
                "ap-northeast-1",
                System.getenv("secret_name"),
                null
        );

        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.object().ifPresent((objectMap) ->
                objectMap.forEach((k, v) -> System.out.printf("%s = %s\n", k, v)));
    }
}