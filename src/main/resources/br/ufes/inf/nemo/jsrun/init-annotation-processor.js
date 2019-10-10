var
    Packages, processingEnv, roundEnvironment, elementSet, java
    ;

function annotationProcessor(processor) {
    
    var
        it,
        annotationTypes,
        next
        ;
        
    if (!processor) {
        return;
    } else if (typeof processor === "function") {
        processor = {
            supportedAnnotationTypes: [ "*" ],
            process: processor
        };
    } else if (processor.constructor === Array) {
        return processor.forEach(annotationProcessor);
    } else if (typeof processor.process !== "function") {
        return annotationProcessor(Object.keys(processor)
                .filter(function isFunction(key) {
                    return typeof processor[key] === "function";
                })
                .map(function toProcessor(key) {
                    return { 
                        supportedAnnotationTypes: [ key ],
                        process: processor[key]
                    };
                })
            );
    }
    processor.supportedAnnotationTypes.forEach(function registerType(annotationType) {
        annotationProcessor.annotationTypes[annotationType] = true;
    });
    annotationProcessor.processorList.push(processor);
}

annotationProcessor.processorList = [];
annotationProcessor.annotationTypes = {};
annotationProcessor.getAnnotationTypes = function getAnnotationTypes() {
    return Object.keys(this.annotationTypes)
            .reduce(function addToSet(set, value) {
                return (set.add(java.lang.String.valueOf(set)), set);
            }, new java.util.HashSet());
};
annotationProcessor.process = function process(processor) {
    processor.process(elementSet, roundEnvironment);
};
annotationProcessor.processAll = function processAll() {
    this.processorList.forEach(annotationProcessor.process);
};

