package io.github.qyvlik.jdph.examples.coffer;

import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import io.github.qyvlik.jdph.examples.coffer.id.env.MemoryCredentialManager;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Driver;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.GetResponse;
import io.github.qyvlik.jdph.plugins.volume.resp.ListResponse;
import io.github.qyvlik.jdph.plugins.volume.resp.MountResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CofferVolumeDriverTest {

    @Test
    public void test() {
        final String path = "/tmp/coffer";
        final String Name = "Test1";


        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);


        Driver driver = new CofferVolumeDriver(path, cm);
        error err = null;

        ret<GetResponse> get = driver.Get(new GetRequest(Name));
        Assertions.assertNotNull(get.err());

        err = driver.Create(new CreateRequest(Name, Map.of(
                "secret.source.url", "git@github.com:qyvlik/java-docker-plugins-helpers.git",
                "secret.source.git-branch", "beard",
                "secret.source.path", "docs/secrets/002.json",
                "secret.source.credential-id", "rsa",
                "secret.source.content-type", "json",

                "template.source.url", "git@github.com:qyvlik/java-docker-plugins-helpers.git",
                "template.source.git-branch", "beard",
                "template.source.path", "docs/templates",
                "template.source.credential-id", "rsa",
                "template.source.content-type", "dir"
        )));

        Assertions.assertNull(err);

        get = driver.Get(new GetRequest(Name));

        Assertions.assertNull(get.err());
        Assertions.assertEquals(Name, get.result().Volume().Name());
        Assertions.assertEquals(String.format("%s/volumes/%s/_data", path, Name), get.result().Volume().Mountpoint());

        ret<MountResponse> mount = driver.Mount(new MountRequest(Name, "0x123456"));
        Assertions.assertNotNull(mount);
        Assertions.assertNull(mount.err());
        Assertions.assertNotNull(mount.result());
        Assertions.assertEquals(String.format("%s/volumes/%s/_data", path, Name), mount.result().Mountpoint());

        ret<ListResponse> list = driver.List();
        Assertions.assertNotNull(list);
        Assertions.assertNull(list.err());
        Assertions.assertNotNull(list.result());
        Assertions.assertNotNull(list.result().Volumes());
        Assertions.assertEquals(1, list.result().Volumes().size());

        err = driver.Unmount(new UnmountRequest(Name, "0x123456"));
        Assertions.assertNull(err);

        err = driver.Remove(new RemoveRequest(Name));
        Assertions.assertNull(err);

        get = driver.Get(new GetRequest(Name));
        Assertions.assertNotNull(get.err());

        list = driver.List();
        Assertions.assertNotNull(list);
        Assertions.assertNull(list.err());
        Assertions.assertNotNull(list.result());
        Assertions.assertNotNull(list.result().Volumes());
        Assertions.assertEquals(0, list.result().Volumes().size());
    }
}