var
    elementSet, buildEnvironment, log
    ;
    
annotationProcessor(function process() {    
    var
        TestAnnotation = "br.inf.ufes.nemo.annoscript.it.TestAnnotation",
        it, typeElement, annotation, elements
        ;
        
    function processAnnotationValue(element, annotation, key, value) {
        log.warn([
            "          ", key.simpleName, "=", value
        ].join(""));
    }
    
    function processAnnotation(element, annotation) {
        var
            isThis = String(annotation.annotationType) === String(typeElement),
            it = annotation.elementValues.entrySet().iterator(),
            entry
            ;
        log.warn([
            isThis ? " (!) " : "     ", 
            element.simpleName, "is annotated with", annotation.annotationType
        ].join(" "));
        while (it.hasNext()) {
            entry = it.next();
            processAnnotationValue(element, annotation, entry.key, entry.value);
        }
        
    }
    
    function processAnnotationMirrors(element) {
        var
            it = element.annotationMirrors.iterator()
            ;
        while (it.hasNext()) {
            processAnnotation(element, it.next());
        }
    }
        
    function processElements(elements) {
        var
            it = elements.iterator()
            ;
        while (it.hasNext()) {
            processAnnotationMirrors(it.next());            
        }
    }
        
    process.count = (process.count || 0) + 1;
    log.warn("======================================================");
    log.warn("Round number " + process.count);
    log.warn("======================================================");
    log.warn(annotationProcessor.annotatedTypes.toSource());    
    log.warn("======================================================");
    it = elementSet.iterator();
    while (it.hasNext()) {
        typeElement = it.next();
        log.warn("--------------------------------------------");
        log.warn("Annotation Type is: " + typeElement.qualifiedName);
        processElements(roundEnvironment.getElementsAnnotatedWith(typeElement));
    }
});