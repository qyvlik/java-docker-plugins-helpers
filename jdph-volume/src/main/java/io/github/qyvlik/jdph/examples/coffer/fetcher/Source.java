package io.github.qyvlik.jdph.examples.coffer.fetcher;

import java.util.Map;
import java.util.Optional;

public interface Source {
    ContentType contentType();

    default Optional<Exception> exception() {
        return Optional.empty();
    }

    default Optional<Map<String, Object>> object() {
        return Optional.empty();
    }

    default Optional<String> string() {
        return Optional.empty();
    }

    static Source failure(ContentType contentType, Exception exception) {
        return new Failure(contentType, exception);
    }

    static Source success(ContentType contentType, Map<String, Object> obj) {
        return new Common(contentType, obj, null);
    }

    static Source text(String str) {
        return new Common(ContentType.text, null, str);
    }

    record Failure(ContentType contentType, Exception e) implements Source {
        public Optional<Exception> exception() {
            return Optional.of(this.e);
        }
    }

    record Common(ContentType contentType, Map<String, Object> obj, String str) implements Source {
        public Optional<Map<String, Object>> object() {
            return obj == null ? Optional.empty() : Optional.of(obj);
        }

        public Optional<String> string() {
            return str == null ? Optional.empty() : Optional.of(str);
        }
    }

}
