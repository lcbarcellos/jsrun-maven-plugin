/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author luciano
 */
public class ScriptRunner {
    
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final BuildPluginManager buildPluginManager;

    Context context;
    ScriptableObject global;

    public ScriptRunner(MavenProject mavenProject, MavenSession mavenSession, BuildPluginManager buildPluginManager) {
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.buildPluginManager = buildPluginManager;
    }
    
    public void run() {
        // create a script engine manager
        context = Context.enter();
        global = context.initStandardObjects();
        ScriptableObject.putProperty(global, "global", global);
    }
}
