package qyvlik.github.io.jdph.go;

public interface error {
    String Error() ;

    static error Create(String msg) {
        return () -> msg;
    }
}
