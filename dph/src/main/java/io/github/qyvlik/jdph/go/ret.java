package io.github.qyvlik.jdph.go;

public record ret<R>(R result, error err) {
    public static <R> ret<R> success(R result) {
        return new ret<>(result, null);
    }

    public static <R> ret<R> failure(error msg) {
        return new ret<>(null, error.Create(msg.Error()));
    }

    public static <R> ret<R> failure(String msg) {
        return new ret<>(null, error.Create(msg));
    }

    public static <R> ret<R> failure(String format, Object... args) {
        return new ret<>(null, error.Create(format, args));
    }
}
