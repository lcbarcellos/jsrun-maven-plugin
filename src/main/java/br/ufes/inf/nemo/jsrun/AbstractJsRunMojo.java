/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import java.io.File;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author luciano
 */
public abstract class AbstractJsRunMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject mavenProject;

    @Parameter( defaultValue = "${session}", readonly = true )
    protected MavenSession mavenSession;    

    @Component
    protected BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    protected File baseDir;
    
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    protected File outputDirectory;
    
    @Parameter(defaultValue = "${project.build.directory}/jsrun", property = "workDir", required = true)
    protected File workDirectory;
}
