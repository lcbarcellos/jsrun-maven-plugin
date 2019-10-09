package br.ufes.inf.nemo.jsrun;

import static br.ufes.inf.nemo.jsrun.AnnotationProcessor.JSRUN_CONFIG_FILE;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AnnotationMojo extends AbstractJsRunMojo {

    private String groupId;
    private String artifactId;
    private String version;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;
    
    private void initVersion() throws MojoExecutionException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("version.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error on gettting version information");
        }
        groupId = properties.getProperty("groupId");
        artifactId = properties.getProperty("artifactId");
        version = properties.getProperty("version");
    }

    @Override
    public void execute() throws MojoExecutionException {
        initVersion();
        workDirectory.mkdirs();
        File configFile = new File(workDirectory, "config.js");

        ConfigWriter.writeConfig(mavenProject, configFile);

        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-compiler-plugin"),
                        version("3.8.1")
                ),
                goal("compile"),
                configuration(
                        element("proc", "only"),
                        element("annotationProcessorPaths",
                                element("path",
                                        element("groupId", groupId),
                                        element("artifactId", artifactId),
                                        element("version", version)
                                )
                        ),
                        element("compilerArgs",
                                element("arg", new StringBuilder()
                                        .append("-A")
                                        .append(JSRUN_CONFIG_FILE)
                                        .append("=")
                                        .toString())
                        )
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
    }
}
