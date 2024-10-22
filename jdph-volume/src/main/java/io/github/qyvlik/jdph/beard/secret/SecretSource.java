package io.github.qyvlik.jdph.beard.secret;

import java.util.List;

/**
 * https://docs.gomplate.ca/datasources/
 */
public interface SecretSource {

    SecretContentType contentType();

    Object scope();

    List<Object> scopes();
}
