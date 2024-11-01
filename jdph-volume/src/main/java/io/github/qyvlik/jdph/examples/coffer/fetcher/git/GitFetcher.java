package io.github.qyvlik.jdph.examples.coffer.fetcher.git;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import io.github.qyvlik.jdph.examples.coffer.fetcher.SourceFetcher;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/*
volumes:
  app_secret:
    secret.source.url: git@github.com:xxx/xxx
    secret.source.filename: xxxx/xxxx/xxx.json
    secret.source.git-branch: master
    secret.source.credential-id: id_rsa
    secret.source.content-type: json
*/

/**
 * https://www.codeaffine.com/2014/12/09/jgit-authentication/
 */
public class GitFetcher implements SourceFetcher {
    private final ContentType contentType;
    private final String gitUrl;
    private final String gitBranch;
    private final Path workDirectory;
    private final String path;
    private final JschConfigSessionFactory sshSessionFactory;

    public GitFetcher(ContentType contentType,
                      String gitUrl,
                      String gitBranch,
                      String path,
                      SSHKeyCredential credential,
                      String workDirectory) {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType is null");
        }
        if (contentType == ContentType.yaml) {
            throw new IllegalArgumentException("not support to parse yaml!");
        }
        if (StringUtils.isBlank(gitUrl)) {
            throw new IllegalArgumentException("gitUrl is blank");
        }
        if (!StringUtils.startsWith(gitUrl, "git")) {
            throw new IllegalArgumentException("gitUrl only support git protocol");
        }
        if (StringUtils.isBlank(workDirectory)) {
            throw new IllegalArgumentException("workDirectory is blank");
        }
        if (!StringUtils.startsWith(workDirectory, "/")) {
            throw new IllegalArgumentException("workDirectory must absolute path");
        }
        if (contentType != ContentType.dir) {
            if (StringUtils.isBlank(path)) {
                throw new IllegalArgumentException("when content type not dir, please make path not blank!");
            }
            if (StringUtils.startsWith(path, "/")) {
                throw new IllegalArgumentException("path must relative path");
            }
        }

        this.contentType = contentType;
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.workDirectory = Path.of(workDirectory);
        this.path = path;

        if (credential != null) {
            this.sshSessionFactory = new JschConfigSessionFactory() {
                public void configure(OpenSshConfig.Host hc, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.setConfigRepository(ConfigRepository.nullConfig);
                    defaultJSch.addIdentity(credential.id(), credential.prvKeyContent(), null, credential.prvKeyPassphrase());
                    return defaultJSch;
                }
            };
        } else {
            this.sshSessionFactory = new JschConfigSessionFactory();
        }


        FS fs = FS.detect();
        fs.setUserHome(this.workDirectory.resolve("user").toFile());
        fs.setGitSystemConfig(this.workDirectory.resolve(".ssh").resolve("config").toFile());
        this.sshSessionFactory.setConfig(OpenSshConfig.get(fs));
    }


    @Override
    public Source get() {
        Path targetDirectory = workDirectory.resolve("repo");
        boolean runClone = !targetDirectory.resolve(".git").toFile().exists();
        try {
            if (runClone) {
                this.executeClone(targetDirectory);
            } else {
                this.executePull(targetDirectory);
            }
        } catch (Exception e) {
            return Source.failure(contentType, e);
        }

        if (contentType == ContentType.dir) {
            Map<String, Object> files = new TreeMap<>();


            Path walkPath = Path.of(targetDirectory.toString(), this.path);

            try (Stream<Path> stream = Files.walk(walkPath)) {
                for (Iterator<Path> it = stream.iterator(); it.hasNext(); ) {
                    Path filePath = it.next();

                    if (Files.isRegularFile(filePath)) {
                        String val = null;
//                        System.out.printf("git fetcher walkPath = %s, filePath = %s \n", walkPath, filePath);
                        try {
                            val = Files.readString(filePath, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            return Source.failure(this.contentType, e);
                        }

                        String filename = StringUtils.removeStart(filePath.toString(), walkPath.toString());
                        filename = StringUtils.removeStart(filename, "/");
                        files.put(filename, val);
                    }
                }

                return Source.success(contentType, files);
            } catch (Exception e) {
                return Source.failure(this.contentType, e);
            }
        }


        String val = null;
        try {
            val = Files.readString(targetDirectory.resolve(this.path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return Source.failure(contentType, e);
        }

        if (this.contentType == ContentType.text) {
            return Source.text(val);
        }

        if (this.contentType == ContentType.json) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, Object> obj = null;
            try {
                obj = mapper.readValue(val,
                        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
            } catch (Exception e) {
                return Source.failure(this.contentType, e);
            }
            return Source.success(this.contentType, obj);
        }
        return Source.failure(this.contentType,
                new IllegalStateException(String.format("%s not raw type not implements %s parser", this.gitUrl, this.contentType)));
    }


    void executeClone(Path targetDirectory) throws GitAPIException {
        CloneCommand cloneCommand = new CloneCommand();
        cloneCommand.setURI(gitUrl);
        cloneCommand.setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        });
        cloneCommand.setDepth(1);
        cloneCommand.setBranch(gitBranch);
        cloneCommand.setDirectory(targetDirectory.toFile());
        try (Git git = cloneCommand.call()) {

        }
    }

    void executePull(Path targetDirectory) throws IOException, GitAPIException {
        try (Git git = Git.open(targetDirectory.toFile())) {
            PullCommand pullCommand = git.pull();
            pullCommand.setRemote("origin");
            pullCommand.setRemoteBranchName(gitBranch);
            pullCommand.setTransportConfigCallback(transport -> {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            });
            pullCommand.call();
        }
    }
}
