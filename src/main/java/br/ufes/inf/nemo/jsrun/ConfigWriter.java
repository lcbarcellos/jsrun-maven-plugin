/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.json.JSONWriter;

/**
 *
 * @author luciano
 */
public class ConfigWriter {

    private final MavenProject mavenProject;

    private final JSONWriter writer;

    public ConfigWriter(MavenProject mavenProject, JSONWriter writer) {
        this.mavenProject = mavenProject;
        this.writer = writer;
    }

    public static void writeConfig(MavenProject mavenProject, File file) throws MojoExecutionException {
        try (
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            writeConfig(mavenProject, bufferedWriter);
        } catch (IOException ex) {
            throw new MojoExecutionException("Error on creating config file", ex);
        }
    }
    public static void writeConfig(MavenProject mavenProject, Writer writer) throws MojoExecutionException {
        try {
            writer.append("setMavenConfig(");
            JSONWriter jsonWriter = new JSONWriter(writer);
            ConfigWriter configWriter = new ConfigWriter(mavenProject, jsonWriter);
            configWriter.write();
            writer.append(");");
        } catch (IOException ex) {
            throw new MojoExecutionException("Error on creating config file", ex);
        }
    }

    public void write() throws MojoExecutionException {

        writer.object()
            .key("project").object();
        
                writeObject(mavenProject);
                
                writer.key("build").object();
                    writeObject(mavenProject.getBuild());
                writer.endObject()
                        
                .key("properties").object();
                {
                    Properties properties = mavenProject.getProperties();
                    for (Object key : properties.keySet()) {
                        String strKey = key.toString();
                        writer.key(strKey).value(properties.getProperty(strKey));
                    }
                }
                writer.endObject()
            .endObject()
        .endObject();
    }
    
    public void writeObject(Object object) throws MojoExecutionException {
        try {
            for (Method method : object.getClass().getMethods()) {
                if (
                        method.getName().startsWith("get") &&
                        method.getParameterCount() == 0
                ) {
                    String propertyName = 
                            method.getName().substring(3, 4).toLowerCase() +
                            method.getName().substring(4);
                    String propertyValue;
                    
                    
                    if (String.class.isAssignableFrom(method.getReturnType())) {
                        propertyValue = (String) method.invoke(object);
                    } else if (File.class.isAssignableFrom(method.getReturnType())) {
                        final File file = (File) method.invoke(object);
                        if (file != null) {
                            propertyValue = file.getAbsolutePath();
                        } else {
                            propertyValue = null;
                        }
                    } else {
                        continue;
                    }
                    writer.key(propertyName).value(propertyValue);
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new MojoExecutionException("Error on creating config file", ex);
        }
    }
}
