package io.github.qyvlik.jdph.examples.coffer.id;

import io.github.qyvlik.jdph.examples.coffer.fetcher.aws.AwsCredential;
import io.github.qyvlik.jdph.examples.coffer.fetcher.git.SSHKeyCredential;

import java.util.Base64;

public interface CredentialManager {

    default void add(String id, String credentialString) {
        String[] array = credentialString.split("\\|");
        CredentialType type = CredentialType.valueOf(array[0]);
        switch (type) {
            case rsa -> {
                byte[] prvKeyContent = Base64.getDecoder().decode(array[1]);
                byte[] prvKeyPassphrase = array.length == 3 ? Base64.getDecoder().decode(array[2]) : null;
                this.add(id, new SSHKeyCredential(id, prvKeyContent, prvKeyPassphrase));
            }
            case aws -> {
                String ak = array[1];
                String sk = array[2];
                this.add(id, new AwsCredential(id, ak, sk));
            }
        }
    }

    void add(String id, Credential credential);

    Credential get(String id);

    interface Credential {
        CredentialType type();

        String id();
    }

    enum CredentialType {
        aws,
        rsa
    }
}
