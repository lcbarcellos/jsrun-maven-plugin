package br.ufes.inf.nemo.jsrun;

import static br.ufes.inf.nemo.jsrun.Constants.BASE_DIR;
import static br.ufes.inf.nemo.jsrun.Constants.OUTPUT_DIR;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AnnotationMojo extends AbstractMojo {

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    private String groupId;
    private String artifactId;
    private String version;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${basedir}", property = "baseDir", required = true, readonly = true)
    private File baseDir;
    
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
        BASE_DIR.set(baseDir.getAbsolutePath());
        OUTPUT_DIR.set(outputDirectory.getAbsolutePath());
        initVersion();
        
        new ScriptRunner(mavenProject, mavenSession, pluginManager).run();
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
