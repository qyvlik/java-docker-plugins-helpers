package io.github.qyvlik.jdph.examples.volumes;

import io.github.qyvlik.jdph.examples.volume.JDPHVolumeDriver;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.GetResponse;
import io.github.qyvlik.jdph.plugins.volume.resp.ListResponse;
import io.github.qyvlik.jdph.plugins.volume.resp.MountResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

class JDPHVolumeDriverTest {

    @Test
    public void testTime() {

        String CreatedAt = ZonedDateTime
                .now(Clock.systemUTC())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        System.out.println(CreatedAt);
    }

    @Test
    public void test() {
        final String dataPath = "/tmp/jdph-volume";
        final String Name = "Test1";
        JDPHVolumeDriver driver = new JDPHVolumeDriver(dataPath);
        error err = null;

        ret<GetResponse> get = driver.Get(new GetRequest(Name));
        Assertions.assertNotNull(get.err());

        err = driver.Create(new CreateRequest(Name, Map.of(
                "secret.source", "https://api.github.com/zen",
                "secret.content-type", "text",
                "template.content.app1", "API_GITHUB_ZEN={{.}}",
                "template.output.app1", "application.properties"
        )));


        Assertions.assertNull(err);

        get = driver.Get(new GetRequest(Name));

        Assertions.assertNull(get.err());
        Assertions.assertEquals(Name, get.result().Volume().Name());
        Assertions.assertEquals(Name, get.result().Volume().Mountpoint());

        ret<MountResponse> mount = driver.Mount(new MountRequest(Name, "0x123456"));
        Assertions.assertNotNull(mount);
        Assertions.assertNull(mount.err());
        Assertions.assertNotNull(mount.result());
        Assertions.assertEquals(Name, mount.result().Mountpoint());

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