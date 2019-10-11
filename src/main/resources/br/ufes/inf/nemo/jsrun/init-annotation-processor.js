var
    processingEnv,
    annotationProcessor
    ;

annotationProcessor = (function defineAnnotationProcessor(processingEnv) {

    var
        processorList = [],
        annotationTypes = {}
        ;

    function annotationProcessor(processor) {

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
            annotationTypes[annotationType] = true;
        });
        processorList.push(processor);
    }

    annotationProcessor.processAll = function processAll(round) {
        processorList.forEach(function process(processor) {
            processor.process(round);
        });
    };

    return annotationProcessor;
} (processingEnv));
