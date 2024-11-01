package io.github.qyvlik.jdph.examples.coffer.out;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.SafeMustacheFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

// get from git | tojson | marker | write to file
public class Marker {

    private final static MustacheFactory mf = new SafeMustacheFactory(Collections.emptySet(), (String) null);
    private final String volumeName;

    // <template filename, Mustache>
    private final Map<String, Mustache> mustaches;
    // <template filename, output filename>

    public Marker(final String volumeName) {
        this.volumeName = volumeName;
        this.mustaches = new TreeMap<>();
    }

    /**
     * @param templateFilename Relative path
     * @param templateString   Mustache style template string
     */
    public void add(String templateFilename,
                    String templateString) {
        if (StringUtils.startsWith(templateFilename, "/")) {
            throw new IllegalArgumentException("templateFilename must relative path");
        }
//        if (StringUtils.endsWith(templateFilename, ".mustache")) {
//            throw new IllegalArgumentException("templateFilename must ends with .mustache");
//        }
        this.mustaches.put(templateFilename,
                mf.compile(new StringReader(templateString), templateFilename)
        );
    }


    public Map<String, String> markByString(String string) throws IOException {
        Map<String, String> out = new TreeMap<>();
        for (Map.Entry<String, Mustache> entry : this.mustaches.entrySet()) {
            String outputFilename = StringUtils.removeEnd(entry.getKey(), ".mustache");

            Mustache mustache = entry.getValue();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            mustache.execute(writer, string);
            writer.flush();

            out.put(outputFilename, outputStream.toString());
        }
        return out;
    }

    public Map<String, String> markByMap(Map<String, Object> map) throws IOException {
        Map<String, String> out = new TreeMap<>();
        for (Map.Entry<String, Mustache> entry : this.mustaches.entrySet()) {
            String outputFilename = entry.getKey();

            Mustache mustache = entry.getValue();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            mustache.execute(writer, map);
            writer.flush();

            out.put(outputFilename, outputStream.toString());
        }
        return out;
    }

    public void write(String parentPath, Map<String, String> out) throws IOException {
        if (!StringUtils.startsWith(parentPath, "/")) {
            throw new IllegalArgumentException("parent path not starts with /");
        }
        for (Map.Entry<String, String> entry : out.entrySet()) {
            File filename = Path.of(parentPath, entry.getKey()).toFile();
            String fileContent = entry.getValue();
            FileUtils.write(filename, fileContent, StandardCharsets.UTF_8);
        }
    }
}
