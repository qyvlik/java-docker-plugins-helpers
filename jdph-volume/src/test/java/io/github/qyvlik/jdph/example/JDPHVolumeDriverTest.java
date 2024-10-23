package io.github.qyvlik.jdph.example;

import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.req.CreateRequest;
import io.github.qyvlik.jdph.plugins.volume.req.GetRequest;
import io.github.qyvlik.jdph.plugins.volume.resp.GetResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JDPHVolumeDriverTest {

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


        get = driver.Get(new GetRequest("Test1"));

        Assertions.assertNull(get.err());
        Assertions.assertEquals(Name, get.result().Volume().Name());
        Assertions.assertEquals(String.format("%s/volumes/%s", dataPath, Name), get.result().Volume().Mountpoint());




    }

}