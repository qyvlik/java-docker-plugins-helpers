package io.github.qyvlik.jdph.examples.coffer.fetcher.aws;


import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;

public record AwsCredential(String id, String accessKeyId, String secretAccessKey) implements CredentialManager.Credential {
    @Override
    public CredentialManager.CredentialType type() {
        return CredentialManager.CredentialType.aws;
    }
}
