package io.github.qyvlik.jdph.examples.coffer.id.env;

import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class MemoryCredentialManager implements CredentialManager {

    private final Map<String, Credential> credentials;

    public static final String CREDENTIAL_PREFIX = "credential_";

    public static MemoryCredentialManager create(Map<String, String> envs, String prefix) {
        MemoryCredentialManager m = new MemoryCredentialManager();
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            if (StringUtils.isBlank(entry.getValue())) {
                continue;
            }
            if (!StringUtils.startsWith(entry.getKey(), prefix)) {
                continue;
            }
            String id = StringUtils.removeStart(entry.getKey(), prefix);
            m.add(id, entry.getValue());
        }
        return m;
    }

    public MemoryCredentialManager() {
        this.credentials = new ConcurrentSkipListMap<>();
    }

    @Override
    public void add(String id, Credential credential) {
        this.credentials.put(id, credential);
    }

    @Override
    public Credential get(String id) {
        return this.credentials.get(id);
    }
}
