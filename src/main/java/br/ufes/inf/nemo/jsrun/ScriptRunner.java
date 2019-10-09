/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author luciano
 */
public class ScriptRunner {
    
    Context context;
    ScriptableObject global;
    
    @FunctionalInterface
    public interface IOSupplier<T> {
        T get() throws IOException;
    }

    public void init() {
        // create a script engine manager
        if (context == null) {
            context = Context.enter();
            global = context.initStandardObjects();
            ScriptableObject.putProperty(global, "global", global);
        }
    }
    
    public void init(
            MavenProject mavenProject, 
            MavenSession mavenSession, 
            BuildPluginManager buildPluginManager) throws MojoFailureException {
        init();
        initMavenObjects(mavenProject, mavenSession, buildPluginManager);
    }
    
    public void initMavenObjects(
            MavenProject mavenProject, 
            MavenSession mavenSession, 
            BuildPluginManager buildPluginManager) throws MojoFailureException {
        putProperty("project", mavenProject);
        putProperty("session", mavenSession);
        putProperty("pluginManager", buildPluginManager);
        
        try (InputStream stream = getClass().getResourceAsStream("init.js")) {
            evaluateStream("init.js", stream);
        } catch (IOException ex) {
            throw new MojoFailureException("Error on initializing script engine", ex);
        }
    }

    private void putProperty(String propertyName, Object object) {
        Object jsObject = Context.javaToJS(object, global);
        ScriptableObject.putProperty(global, propertyName, jsObject);
    }
    
    public void evalulateFile(File file) throws FileNotFoundException, IOException {
        try (InputStream stream = new FileInputStream(file)) {
            evaluateStream(file.getName(), stream);
        }
    }

    private void evaluateStream(String streamName, InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        context.evaluateReader(global, reader, streamName, 1, null);
    }
    
    public void end() {
        Context.exit();
    }
}
