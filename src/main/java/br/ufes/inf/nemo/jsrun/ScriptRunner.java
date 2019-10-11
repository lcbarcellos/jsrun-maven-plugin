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
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author luciano
 */
public class ScriptRunner {

    Context context;
    ScriptableObject global;

    public void init() {
        // create a script engine manager
        if (context == null) {
            context = Context.enter();
            global = context.initStandardObjects();
            ScriptableObject.putProperty(global, "global", global);
            putProperty("runner", this);
            try {
                evaluateResource(getClass(), "init-script-runner.js");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
        try {
            evaluateResource(getClass(), "init-script-runner.js");
        } catch (IOException ex) {
            throw new MojoFailureException(
                    "Error on initializing script engine", ex);
        }
    }

    public void putProperty(String propertyName, Object object) {
        Object jsObject = Context.javaToJS(object, global);
        ScriptableObject.putProperty(global, propertyName, jsObject);
    }

    public Object getProperty(String propertyName) {
        return global.get(propertyName, global);
    }

    public Object callFunction(String path, Object... args) {
        Scriptable thisObj = global;
        Scriptable last = global;
        String pathArray[] = path.split("\\.");
        for (String pathItem : pathArray) {
            thisObj = last;
            last = (Scriptable) last.get(pathItem, last);
        }
        Function func = (Function) last;
        return func.call(context, global, thisObj, args);
    }

    public Object evaluateFile(File file) throws FileNotFoundException, IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return evaluateStream(file.getName(), stream);
        }
    }

    public Object evaluateFile(String fileName) throws IOException {
        return evaluateFile(new File(fileName));
    }

    public Object evaluateStream(String streamName, InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return context.evaluateReader(global, reader, streamName, 1, null);
    }

    public Object evaluateResource(Class<?> clazz, String resourceName) throws IOException {
        try (InputStream stream = clazz.getResourceAsStream(resourceName)) {
            return evaluateStream(resourceName, stream);
        }
    }

    public Object evaluateString(String scriptName, String scriptSource) {
        return context.evaluateString(global, scriptSource, scriptName, 1, null);
    }

    public void enter() {
        context = Context.enter();
    }

    public void end() {
        Context.exit();
    }

    public ScriptableObject getGlobalScope() {
        return global;
    }
}
