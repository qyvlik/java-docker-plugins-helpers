package io.github.qyvlik.jdph.examples.coffer.fetcher;

import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import io.github.qyvlik.jdph.examples.coffer.id.env.MemoryCredentialManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class SourceSelectorTest {
    @Test
    public void testFetchFromHttp() {
        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);

        SourceSelector selector = new SourceSelector(cm);
        SourceFetcher fetcher = selector.fetcher(
                "/tmp/jdph-volume/workspace",
                "test",
                Map.of(SourceSelector.SOURCE_URL, "https://api.github.com/zen",
                        SourceSelector.SOURCE_CONTENT_TYPE, ContentType.text.name(),
                        SourceSelector.SOURCE_PROXY, "socks5://127.0.0.1:1086"
                )
        );

        Source source = fetcher.get();
        Assertions.assertEquals(source.contentType(), ContentType.text);
        if (source.exception().isPresent()) {
            System.out.printf("exception = %s", source.exception().get());
            source.exception().get().printStackTrace(System.out);
        }
        Assertions.assertTrue(source.string().isPresent());
        System.out.printf("text = %s \n", source.string().get());
    }

    @Test
    public void testFetchFromHttpAsJson() {
        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);

        SourceSelector selector = new SourceSelector(cm);
        SourceFetcher fetcher = selector.fetcher(
                "/tmp/jdph-volume/workspace",
                "test",
                Map.of(SourceSelector.SOURCE_URL, "https://httpbin.org/get",
                        SourceSelector.SOURCE_CONTENT_TYPE, ContentType.json.name()
                )
        );

        Source source = fetcher.get();
        Assertions.assertEquals(source.contentType(), ContentType.json);
        if (source.exception().isPresent()) {
            System.out.printf("exception = %s", source.exception().get());
            source.exception().get().printStackTrace(System.out);
        }
        Assertions.assertTrue(source.object().isPresent());
        System.out.printf("object = %s \n", source.object().get());
    }

    @Test
    public void testFetchFromGitAsDir() {
        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);

        SourceSelector selector = new SourceSelector(cm);
        SourceFetcher fetcher = selector.fetcher(
                "/tmp/jdph-volume/workspace/test",
                "test",
                Map.of(SourceSelector.SOURCE_URL, "git@github.com:qyvlik/java-docker-plugins-helpers.git",
                        SourceSelector.SOURCE_GIT_BRANCH, "beard",
                        SourceSelector.SOURCE_PATH, "docs/templates",
                        SourceSelector.SOURCE_CREDENTIAL_ID, "rsa",
                        SourceSelector.SOURCE_CONTENT_TYPE, ContentType.dir.name()
                )
        );

        Source source = fetcher.get();
        Assertions.assertEquals(source.contentType(), ContentType.dir);
        if (source.exception().isPresent()) {
            System.out.printf("exception = %s", source.exception().get());
            source.exception().get().printStackTrace(System.out);
        }
        Assertions.assertTrue(source.object().isPresent());
        System.out.printf("object = %s \n", source.object().get());
    }

    @Test
    public void testFetchFromAwsSMAsTest() {
        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);

        SourceSelector selector = new SourceSelector(cm);
        SourceFetcher fetcher = selector.fetcher(
                "/tmp/jdph-volume/workspace/test",
                "test",
                Map.of(SourceSelector.SOURCE_URL, "aws+sm@ap-northeast-1:test/test",
                        SourceSelector.SOURCE_CREDENTIAL_ID, "aws",
                        SourceSelector.SOURCE_CONTENT_TYPE, ContentType.json.name()
                )
        );

        Source source = fetcher.get();
        Assertions.assertEquals(source.contentType(), ContentType.json);
        if (source.exception().isPresent()) {
            System.out.printf("exception = %s", source.exception().get());
            source.exception().get().printStackTrace(System.out);
        }
        Assertions.assertTrue(source.object().isPresent());
        source.object().ifPresent((objectMap) ->
                objectMap.forEach((k, v) -> System.out.printf("%s = %s\n", k, v)));
    }
}