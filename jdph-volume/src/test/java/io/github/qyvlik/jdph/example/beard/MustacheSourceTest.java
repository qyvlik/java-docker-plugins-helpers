package io.github.qyvlik.jdph.example.beard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.SafeMustacheFactory;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class MustacheSourceTest {


    @Test
    public void test() throws IOException {
        Writer writer = new OutputStreamWriter(System.out);
        MustacheFactory mf = new SafeMustacheFactory(Collections.emptySet(), (String) null);
        Mustache mustache = mf.compile(new StringReader("root = {{.}}"), "example");
        mustache.execute(writer, "hello,world");
        writer.flush();
    }

    public record Item(int x, int y) {

    }

    @Test
    public void testClass() throws IOException {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        MustacheFactory mf = new SafeMustacheFactory(Collections.emptySet(), (String) null);

        Mustache mustache = mf.compile(new StringReader("root.x = {{x}}, root.y={{y}}"), "example");
        mustache.execute(writer, new Item(1, 2));
        writer.flush();
        System.out.println(outputStream.toString());
    }


    @Test
    public void testJackson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("{}");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        MustacheFactory mf = new SafeMustacheFactory(Collections.emptySet(), (String) null);

        Mustache mustache = mf.compile(new StringReader("root = {{.}}"), "example");
        mustache.execute(writer, node);
        writer.flush();
        System.out.println(outputStream.toString());
    }

    @Test
    public void testJackson2() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> scope = mapper.readValue("{\"Name\" : \"1\", \"Objects\" : { \"Key\": \"123\", \"Arrays\": [ 1, 2, 3] } }",
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        MustacheFactory mf = new SafeMustacheFactory(Collections.emptySet(), (String) null);

        Mustache mustache = mf.compile(new StringReader("root = {{ Name }}"), "example");
        mustache.execute(writer, scope);
        writer.flush();
        System.out.println(outputStream.toString());
    }

}