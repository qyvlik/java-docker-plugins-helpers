package io.github.qyvlik.jdph.beard;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.SafeMustacheFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Renderer {

    private final MustacheFactory factory;

    private SecretSource secretSource;

    private final Map<String, Rendering> renderings;

    public Renderer() {
        this.renderings = new TreeMap<>();
        this.factory = new SafeMustacheFactory(Collections.emptySet(), (String) null);
    }

    public SecretSource getSecretSource() {
        return secretSource;
    }

    public void setSecretSource(SecretSource secretSource) {
        this.secretSource = secretSource;
    }

    public void add(Template template) {
        Mustache mustache = this.factory.compile(new StringReader(template.template()), template.name());
        this.renderings.put(template.name(), new Rendering(mustache, template.output()));
    }

    public void clear() {
        this.renderings.clear();
    }

    public Map<String, Output> render() throws IOException {
        var scope = this.secretSource.scope();
        if (scope != null) {
            return this.render(scope);
        } else {
            return this.render(this.secretSource.scopes());
        }
    }

    public Map<String, Output> render(Object scope) throws IOException {
        Map<String, Output> map = new TreeMap<>();

        for (Map.Entry<String, Rendering> entry : this.renderings.entrySet()) {
            var name = entry.getKey();
            var mustache = entry.getValue().mustache();
            var filename = entry.getValue().filename();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);

            mustache.execute(writer, scope);

            writer.flush();
            map.put(name, new Output(filename, outputStream.toString()));
        }
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Output> render(List<Object> scopes) throws IOException {
        Map<String, Output> map = new TreeMap<>();
        for (Map.Entry<String, Rendering> entry : this.renderings.entrySet()) {
            var name = entry.getKey();
            var mustache = entry.getValue().mustache();
            var filename = entry.getValue().filename();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);

            mustache.execute(writer, scopes);

            writer.flush();
            map.put(name, new Output(filename, outputStream.toString()));
        }
        return Collections.unmodifiableMap(map);
    }
}
