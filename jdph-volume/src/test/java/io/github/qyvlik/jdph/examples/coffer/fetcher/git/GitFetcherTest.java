package io.github.qyvlik.jdph.examples.coffer.fetcher.git;

import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class GitFetcherTest {

    @Test
    public void testAsText() throws IOException {
        GitFetcher fetcher = new GitFetcher(
                ContentType.text,
                "git@github.com:qyvlik/java-docker-plugins-helpers.git",
                "beard",
                "docs/examples/001.yaml",
                new SSHKeyCredential("rsa",
                        Files.readAllBytes(Path.of(System.getenv("prv_key_filename"))),
                        System.getenv("ph").getBytes(StandardCharsets.UTF_8)
                ),
                "/tmp/jgit2"
        );

        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.object().ifPresent((objectMap) ->
                objectMap.forEach((k, v) -> System.out.printf("%s = %s\n", k, v)));
    }

    @Test
    public void testAsDir() throws IOException {
        GitFetcher fetcher = new GitFetcher(
                ContentType.dir,
                "git@github.com:qyvlik/java-docker-plugins-helpers.git",
                "beard",
                "docs/templates",
                new SSHKeyCredential("rsa",
                        Files.readAllBytes(Path.of(System.getenv("prv_key_filename"))),
                        System.getenv("ph").getBytes(StandardCharsets.UTF_8)
                ),
                "/tmp/jgit2"
        );

        Source source = fetcher.get();

        source.exception().ifPresent(Throwable::printStackTrace);
        source.object().ifPresent((objectMap) ->
                objectMap.forEach((k, v) -> System.out.printf("%s = %s\n", k, v)));
    }
}