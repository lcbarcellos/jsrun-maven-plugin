var
    Packages, project, session, pluginManager
    ;

function executeMojo(args) {
    var
        MojoExecutor = Packages.org.twdata.maven.mojoexecutor.MojoExecutor,
        filters,
        sortedArgs = {
            "plugin": args.plugin,
            "goal": args.goal,
            "configuration": args.configuration,
            "executionEnvironment": {
                "mavenProject": project,
                "mavenSession": session,
                "pluginManager":  pluginManager
            }
        }
        ;
    
    filters = {
        "plugin": function (value) {
            var
                regexp = /(.*?):(.*?):(.*)/,
                match
                ;
            return (
                (typeof value === "string") &&
                (match = regexp.exec(value)) &&
                {
                    groupId: match[1],
                    artifactId: match[2],
                    version: match[3]
                } || value
            );
        },
        "configuration": {
            map: function configuration(value) {
                return {
                    map: {},
                    list: toElement("", value)
                };
            }
        }
    };
    
    function toElement(elementName, data) {
        var
            attributes = [],
            elements = [],
            name,
            value,
            textContent
            ;
        if (typeof data === "string") {
            return MojoExecutor.element(elementName, data);
        } 
        for (name in data) {
            if (value.hasOwnProperty(name)) {
                value = data[name];
                if (value) {
                    if (name[0] === "@") {
                        name = name.substring(1);
                        attributes.push(MojoExecutor.attribute(name, value));
                    } else if (name === "$") {
                        textContent = value;
                    } else {
                        elements.push(toElement(name, value));
                    }
                }
            }
        }
        if (elementName) {
            if (textContent) {
                return MojoExecutor.element(elementName, textContent, attributes);
            } else {
                return MojoExecutor.element(elementName, attributes, elements);
            }
        } else {
            return elements;
        }
    }
    
    function mapObject(object) {
        var
            propertyName,
            propertyValue,
            mappedValue,
            result = { map: {}, list: [] }
            ;
        for (propertyName in object) {
            if (object.hasOwnProperty(propertyName)) {
                propertyValue = object[propertyName];
                mappedValue = callFunction(propertyName, propertyValue);
                result.map[propertyName] = mappedValue;
                result.list.push(mappedValue);
            }
        }        
        return result;
    }
    
    function callFunction(functionName, objectArgs) {
        var
            mojoFunction = MojoExecutor[functionName],
            filter = filters[functionName],
            mapped
            ;
        if (typeof mojoFunction === "function") {
            if (filter) {
                if (filter.map) {
                    mapped = filter.map(objectArgs);
                } else {
                    mapped = mapObject(filter(objectArgs));
                }
            } else {
                mapped = mapObject(objectArgs);
            }
            return mojoFunction.apply(MojoExecutor, mapped.list);
        } else {
            return objectArgs;
        }        
    }
    
    return callFunction("executeMojo", sortedArgs);        
}
