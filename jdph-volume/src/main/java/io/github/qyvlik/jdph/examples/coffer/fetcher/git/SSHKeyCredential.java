package io.github.qyvlik.jdph.examples.coffer.fetcher.git;

import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;

public record SSHKeyCredential(String id, byte[] prvKeyContent, byte[] prvKeyPassphrase) implements CredentialManager.Credential {
    @Override
    public CredentialManager.CredentialType type() {
        return CredentialManager.CredentialType.rsa;
    }
}
