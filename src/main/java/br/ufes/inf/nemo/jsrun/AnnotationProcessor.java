/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import static br.ufes.inf.nemo.jsrun.AnnotationProcessor.JSRUN_PROCESSOR_FILE;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author luciano
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@SupportedOptions({JSRUN_PROCESSOR_FILE})
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    
    public static final String JSRUN_PROCESSOR_FILE="jsrun.processor.file";

    Logger log = Logger.getLogger(AnnotationProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {        
        boolean result = false;
        ScriptRunner scriptRunner = new ScriptRunner();        
        try {
            scriptRunner.init();
            scriptRunner.putProperty("processingEnv", processingEnv);
            scriptRunner.putProperty("elementSet", set);
            scriptRunner.putProperty("roundEnvironment", re);
            scriptRunner.putProperty("log", log);
            scriptRunner.evaluateResource(getClass(), "init-annotation-processor.js");
            scriptRunner.evaluateFile(
                    processingEnv.getOptions().get(JSRUN_PROCESSOR_FILE));
        } catch (IOException ex) {
            return true;
        } finally {
            scriptRunner.end();
        }
        return result;
    }
}
