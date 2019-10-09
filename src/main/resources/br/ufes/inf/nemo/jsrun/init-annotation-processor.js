var
    Packages, processingEnv, roundEnvironment, elementSet
    ;

log.warning("Iniciou init-annotation-processor.js");

function processAnnotations(annotationTypeNames, process) {
    var
        it,
        annotationTypes,
        next
        ;
    if (typeof annotationTypeNames.map !== "function") {
        annotationTypeNames = [annotationTypeNames];
    }
    annotationTypes = annotationTypeNames.map(function map(item) {
        return Packages[item];
    }).filter(function filterClasses(item) {
        return typeof item === "function";
    });

    it = roundEnvironment.getElementsAnnotatedWithAny(annotationTypes);
    while (it.hasNext()) {
        next = it.next();
        process(next, annotationTypes.reduce(function (r, annotationType) {
            r[annotationType.class.name] = next.getAnnotation(annotationType);
            return r;
        }, {}));
    }
}

function processSingleAnnotation(annotationTypeName, process) {
    processAnnotations([annotationTypeName], function (type, annotations) {
        process(type, annotations[annotationTypeName]);
    });
}

log.warning("Finalizou init-annotation-processor.js");