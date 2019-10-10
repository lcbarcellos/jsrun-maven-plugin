package br.ufes.inf.nemo.jsrun;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static br.ufes.inf.nemo.jsrun.AnnotationProcessor.JSRUN_PROCESSOR_FILE;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class AnnotationMojo extends AbstractJsRunMojo {

    private String groupId;
    private String artifactId;
    private String version;
    
    @Parameter(
            defaultValue = "${basedir}/src/build/javascript/annotation-processor.js",
            property = "annotationProcessor", 
            required = true
    )
    protected File annotationProcessor;

    public File getAnnotationProcessor() {
        return annotationProcessor;
    }
    
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
        File processorFile = new File(workDirectory, "annotation-processor-mojo.js");
        try (
                FileWriter fileWriter = new FileWriter(processorFile);
                Writer writer = new BufferedWriter(fileWriter);
                InputStream resource = getClass().getResourceAsStream("annotation-processor-mojo.js");
        ) {
            writer.append("mavenConfig = ");
            ConfigWriter.with(writer)
                .object()
                    .key("project").object()
                        .objectProperties(mavenProject)
                        .key("build").object(mavenProject.getBuild())
                        .key("properties").object(mavenProject.getProperties())
                    .endObject()
                    .key("jsRun").object(this)
                .endObject()
                ;            
            writer.append(";\n");
            
            IOUtil.copy(resource, writer);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error on preparing javascript mojo");
        }
        executeAnnotationProcessor(processorFile);
    }

    private void executeAnnotationProcessor(File processorFile) throws MojoExecutionException {
        
        executeMojo(plugin(
                groupId("org.apache.maven.plugins"),
                artifactId("maven-compiler-plugin"),
                version("3.8.1")
            ),
            goal("compile"),
            configuration(element("proc", "only"),
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
                                    .append(JSRUN_PROCESSOR_FILE)
                                    .append("=")
                                    .append(processorFile.getAbsolutePath())
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
