/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import static br.ufes.inf.nemo.jsrun.AnnotationProcessor.JSRUN_CONFIG_FILE;
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

/**
 *
 * @author luciano
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@SupportedOptions({JSRUN_CONFIG_FILE})
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    
    public static final String JSRUN_CONFIG_FILE="jsrun.config.file";

    Logger log = Logger.getLogger(AnnotationProcessor.class.getName());
    
    
    
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        
        String configFile = processingEnv.getOptions().get(JSRUN_CONFIG_FILE);
        if (configFile == null) {
            log.warning("Maven environment not detected");
        } else {
            log.warning("Config file is " + configFile);
        }
        
        return true;
    }
}
