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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
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

        final String jsRunProcessorFileOption
                = processingEnv.getOptions().get(JSRUN_PROCESSOR_FILE);

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
                scriptRunner.evaluateResource(getClass(),
                        "init-annotation-processor.js");
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
    public boolean process(
            Set<? extends TypeElement> set, RoundEnvironment re) {

        if (scriptRunner == null) {
            return false;
        } else {
            try {
                scriptRunner.enter();
                scriptRunner.putProperty("elementSet", set);
                scriptRunner.putProperty("roundEnvironment", re);
                scriptRunner.callFunction(
                        "annotationProcessor.processAll",
                        roundArgument(set, re));
            } finally {
                scriptRunner.end();
            }
            return true;
        }
    }

    private Scriptable roundArgument(
            Set<? extends TypeElement> set, RoundEnvironment re) {
        Scriptable result = ScriptableBuilder.from(set, re, scriptRunner);
        Scriptable scope = scriptRunner.getGlobalScope();
        result.put("processingEnv", result,
                scriptRunner.getProperty("processingEnv"));
        result.put("roundEnvironment", result, Context.javaToJS(re, scope));
        result.put("annotationTypeSet", result, Context.javaToJS(set, scope));
        return result;
    }
}

class ScriptableBuilder implements AnnotationValueVisitor<Object, Scriptable> {

    private Context context;

    public static Scriptable from(
            Set<? extends TypeElement> typeElementSet,
            RoundEnvironment roundEnvironment,
            ScriptRunner runner) {
        ScriptableBuilder builder = new ScriptableBuilder();
        Scriptable result;
        try {
            result = builder.build(typeElementSet, roundEnvironment,
                    runner.getGlobalScope());
        } finally {
            builder.exitContext();
        }
        return result;
    }

    public static Scriptable from(TypeElement typeElement, ScriptRunner runner) {
        ScriptableBuilder builder = new ScriptableBuilder();
        Scriptable result;
        try {
            result = builder.build(typeElement, runner.getGlobalScope());
        } finally {
            builder.exitContext();
        }
        return result;
    }

    public Scriptable build(Set<? extends TypeElement> annotationSet,
            RoundEnvironment roundEnvironment, final Scriptable scope) {

        class Mapper {

            Map<TypeElement, Scriptable> javaToJS = new HashMap();

            public Scriptable addTypeElement(TypeElement typeElement) {
                if (!javaToJS.containsKey(typeElement)) {
                    javaToJS.put(typeElement, build(typeElement, scope));
                }
                return javaToJS.get(typeElement);
            }

            private Scriptable getTypeElements() {
                Scriptable result = getContext().newObject(scope);
                for (Map.Entry<TypeElement, Scriptable> entry : javaToJS.entrySet()) {
                    final Name typeName = entry.getKey().getQualifiedName();
                    result.put(typeName.toString(), result, entry.getValue());
                }
                return result;
            }
        }

        Mapper mapper = new Mapper();
        Scriptable annotationTypes = getContext().newObject(scope);

        for (TypeElement annotationType : annotationSet) {
            final Set<? extends Element> annotatedElements
                    = roundEnvironment.getElementsAnnotatedWith(annotationType);
            List<Scriptable> annotatedTypes
                    = new ArrayList(annotatedElements.size());
            for (Element annotatedElement : annotatedElements) {
                if (annotatedElement instanceof TypeElement) {
                    final TypeElement annotatedType
                            = (TypeElement) annotatedElement;
                    Scriptable annotatedTypeJS
                            = mapper.addTypeElement(annotatedType);
                    annotatedTypes.add(annotatedTypeJS);
                }
            }
            Scriptable annotationJS = mapper.addTypeElement(annotationType);
            annotationJS.put("annotatedTypes", annotationJS,
                    getContext().newArray(scope, annotatedTypes.toArray()));
            annotationTypes.put(
                    annotationType.getQualifiedName().toString(),
                    annotationTypes, annotationJS);
        }

        Scriptable result = getContext().newObject(scope);
        result.put("annotationTypes", result, annotationTypes);
        result.put("typeElements", result, mapper.getTypeElements());
        return result;
    }

    public Scriptable build(TypeElement typeElement, Scriptable scope) {

        Scriptable annotations = getContext().newObject(scope);

        for (AnnotationMirror annotation : typeElement.getAnnotationMirrors()) {
            annotations.put(
                    annotation.getAnnotationType().toString(),
                    annotations, visitAnnotation(annotation, scope));
        }

        Scriptable result = getContext().newObject(scope);
        result.put("typeElement", result, Context.javaToJS(typeElement, scope));
        result.put("annotations", result, annotations);
        return result;
    }

    private Context getContext() {
        return (context != null) ? context
                : (context = ContextFactory.getGlobal().enterContext());
    }

    private void exitContext() {
        if (context != null) {
            context = null;
            Context.exit();
        }
    }

    @Override
    public Object visit(AnnotationValue av, Scriptable scope) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object visitBoolean(boolean bln, Scriptable scope) {
        return bln;
    }

    @Override
    public Object visitByte(byte b, Scriptable scope) {
        return b;
    }

    @Override
    public Object visitChar(char c, Scriptable scope) {
        return c;
    }

    @Override
    public Object visitDouble(double d, Scriptable scope) {
        return d;
    }

    @Override
    public Object visitFloat(float f, Scriptable scope) {
        return f;
    }

    @Override
    public Object visitInt(int i, Scriptable scope) {
        return i;
    }

    @Override
    public Object visitLong(long l, Scriptable scope) {
        return l;
    }

    @Override
    public Object visitShort(short s, Scriptable scope) {
        return s;
    }

    @Override
    public Object visitString(String string, Scriptable scope) {
        return string;
    }

    @Override
    public Object visitType(TypeMirror tm, Scriptable scope) {
        return tm.toString();
    }

    @Override
    public Object visitEnumConstant(VariableElement ve, Scriptable scope) {
        return ve.getSimpleName();
    }

    @Override
    public Object visitAnnotation(AnnotationMirror am, Scriptable scope) {
        Scriptable object = getContext().newObject(scope);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            object.put(key, object, entry.getValue().accept(this, scope));
        }
        return object;
    }

    @Override
    public Object visitArray(List<? extends AnnotationValue> list, Scriptable scope) {
        Object[] objects = new Object[list.size()];
        int index = 0;
        for (AnnotationValue annotationValue : list) {
            objects[index++] = annotationValue.accept(this, scope);
        }
        return context.newArray(scope, objects);
    }

    @Override
    public Object visitUnknown(AnnotationValue av, Scriptable scope) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
