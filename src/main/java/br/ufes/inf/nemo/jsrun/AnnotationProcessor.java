/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import com.google.auto.service.AutoService;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import static br.ufes.inf.nemo.jsrun.AnnotationProcessor.JSRUN_PROCESSOR_FILE;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author luciano
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_9)
@SupportedOptions({JSRUN_PROCESSOR_FILE})
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    public static final String JSRUN_PROCESSOR_FILE = "jsrun.processor.file";    

    Logger log = LoggerFactory.getLogger(AnnotationProcessor.class);
    
    private ScriptRunner scriptRunner;

    @Override
    public void init(ProcessingEnvironment pe) {

        super.init(pe);
        
        final String jsRunProcessorFileOption =
                processingEnv.getOptions().get(JSRUN_PROCESSOR_FILE);
        
        if (jsRunProcessorFileOption == null) {
            log.info("Annotation processor javascript file not specified. Skipping.");
        } else if (!(new File(jsRunProcessorFileOption)).canRead()) {
            log.warn("Cannot read file '" + jsRunProcessorFileOption + "' for annotation processing.");
        } else {
            scriptRunner = new ScriptRunner();
            try {
                scriptRunner.init();
                scriptRunner.putProperty("processingEnv", processingEnv);
                scriptRunner.putProperty("log", log);
                scriptRunner.evaluateResource(getClass(), "init-annotation-processor.js");
                scriptRunner.evaluateFile(
                        processingEnv.getOptions().get(JSRUN_PROCESSOR_FILE));                
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                scriptRunner.end();
            }
        }
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
       
        if (scriptRunner == null) {
            return false;
        } else {            
            
            ElementVisitor elementVisitor = new ElementVisitor();
            elementVisitor.append("annotationProcessor.annotatedTypes=");
            if (set != null) {
                for (TypeElement typeElement : set) {
                    // TODO: STOPPED HERE!!
                    re.getElementsAnnotatedWith(typeElement);
                }
            } else {                
                elementVisitor.append("{}");
            }            
            elementVisitor.append(";");

            try {
                scriptRunner.enter();
                scriptRunner.putProperty("elementSet", set);
                scriptRunner.putProperty("roundEnvironment", re);
                log.warn(elementVisitor.toString());
                scriptRunner.evaluateString("process", elementVisitor.toString());
                scriptRunner.evaluateString("process", "annotationProcessor.processAll()");                
            } finally {
                scriptRunner.end();
            }
            
            return true;
        }
    }
    
}

class ElementVisitor {
    
    StringBuilder script = new StringBuilder();
    ConfigWriter configWriter = new ConfigWriter(script);
    Set<TypeElement> visited = new HashSet();
    ValueVisitor valueVisitor = new ValueVisitor();
    
    public void append(String text) {
        script.append(text);
    }

    @Override
    public String toString() {
        return script.toString();
    }
    
    public void add(TypeElement typeElement) {
        if (visited.add(typeElement)) {
            configWriter.object()
                    .key("qualifiedName").value(typeElement.getQualifiedName().toString())
                    .key("annotations").object();
                        annotations(typeElement)
                    .endObject()
            .endObject();
        }
    }
    
    private ConfigWriter annotations(TypeElement typeElement) {
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            annotation(annotationMirror);
        }
        return configWriter;
    }
    
    private void annotation(AnnotationMirror annotationMirror) {
        configWriter
                .key(annotationMirror.getAnnotationType().toString());
        valueVisitor.visitAnnotation(annotationMirror, configWriter);
    }
}

class ValueVisitor implements AnnotationValueVisitor<ConfigWriter, ConfigWriter> {

    @Override
    public ConfigWriter visit(AnnotationValue av, ConfigWriter p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfigWriter visitBoolean(boolean bln, ConfigWriter p) {
        return p.value(bln);
    }

    @Override
    public ConfigWriter visitByte(byte b, ConfigWriter p) {
        return p.value(b);
    }

    @Override
    public ConfigWriter visitChar(char c, ConfigWriter p) {
        return p.value(String.valueOf(c));
    }

    @Override
    public ConfigWriter visitDouble(double d, ConfigWriter p) {
        return p.value(d);
    }

    @Override
    public ConfigWriter visitFloat(float f, ConfigWriter p) {
        return p.value(f);
    }

    @Override
    public ConfigWriter visitInt(int i, ConfigWriter p) {
        return p.value(i);
    }

    @Override
    public ConfigWriter visitLong(long l, ConfigWriter p) {
        return p.value(l);
    }

    @Override
    public ConfigWriter visitShort(short s, ConfigWriter p) {
        return p.value(s);
    }

    @Override
    public ConfigWriter visitString(String string, ConfigWriter p) {
        return p.value(string);
    }

    @Override
    public ConfigWriter visitType(TypeMirror tm, ConfigWriter p) {
        return p.value(tm.toString());
    }

    @Override
    public ConfigWriter visitEnumConstant(VariableElement ve, ConfigWriter p) {
        return p.value(ve.getSimpleName());
    }

    @Override
    public ConfigWriter visitAnnotation(AnnotationMirror am, ConfigWriter p) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = am.getElementValues();        
        if (elementValues.size() == 1) {
            Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> single = am.getElementValues().entrySet().iterator().next();
            if ("value".equals(single.getKey().getSimpleName().toString())) {
                return single.getValue().accept(this, p);
            }
        } 
        p.object();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
            p.key(entry.getKey().getSimpleName().toString());
            entry.getValue().accept(this, p);
        }
        return p.endObject();
    }

    @Override
    public ConfigWriter visitArray(List<? extends AnnotationValue> list, ConfigWriter p) {
        p.array();
        for (AnnotationValue annotationValue : list) {
            annotationValue.accept(this, p);
        }
        return p.endArray();
    }

    @Override
    public ConfigWriter visitUnknown(AnnotationValue av, ConfigWriter p) {
        return p.object().key("#ERROR").value("Unknown Annotation Value").endObject();
    }
}