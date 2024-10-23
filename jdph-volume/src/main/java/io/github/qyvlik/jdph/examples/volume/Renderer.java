package io.github.qyvlik.jdph.examples.volume;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.SafeMustacheFactory;
import io.github.qyvlik.jdph.plugins.volume.req.CreateRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Renderer {

    private final MustacheFactory factory;

    private final Map<String, Rendering> renderings;

    public Renderer() {
        this.renderings = new TreeMap<>();
        this.factory = new SafeMustacheFactory(Collections.emptySet(), (String) null);
    }



    public void add(Template template) {
        Mustache mustache = this.factory.compile(new StringReader(template.template()), template.app());
        this.renderings.put(template.app(), new Rendering(mustache, template.output()));
    }

    public void clear() {
        this.renderings.clear();
    }

    private SecretSource secretSource(String secretSource, String secretContentType) {
        try {
            return new HttpSecretSource(URI.create(secretSource), SecretContentType.valueOf(secretContentType));
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            System.out.printf("create secret source failure :%s \n", e.getMessage());
            return null;
        }
    }

    public Map<String, Output> render(CreateRequest request) throws IOException {
        Map<String, String> options = request.Opts();
        String secretSource = options.get("secret.source");
        String secretContentType = options.get("secret.content-type");

        Map<String, Template> templates = new TreeMap<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (entry.getKey().startsWith("template.content.")) {
                String app = StringUtils.removeStart(entry.getKey(), "template.content.");
                String mustacheContent = entry.getValue();
                String mustacheOutput = options.get(String.format("template.output.%s", app));
                if (StringUtils.isBlank(mustacheOutput)) {
                    mustacheOutput = app;
                }
                templates.put(app, new Template(app, mustacheContent, mustacheOutput));
            }
        }

        SecretSource source = secretSource(secretSource, secretContentType);

        if (source == null) {
            throw new RemoteException(String.format("render failure : fetch %s error", secretSource));
        }

        return this.render(templates, source);
    }

    public void write(Path prefix, Map<String, Output> outputs) throws IOException {
        for(Output output : outputs.values()) {
            FileUtils.write(
                    Path.of(prefix.toString(), output.filename()).toFile(),
                    output.content(),
                    StandardCharsets.UTF_8
            );
        }
    }

    public Map<String, Output> render(Map<String, Template> templates, SecretSource secretSource) throws IOException {
        this.clear();
        for (Template template : templates.values()) {
            this.add(template);
        }
        var scope = secretSource.scope();
        if (scope != null) {
            return this.render(scope);
        }
        return this.render(secretSource.scopes());
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
