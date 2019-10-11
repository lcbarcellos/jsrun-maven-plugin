/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 *
 * @author luciano
 */
public class ConfigWriter extends JSONWriter {

    public ConfigWriter(Appendable appendable) {
        super(appendable);
    }
    
    public static ConfigWriter with(Appendable appendable) {
        return new ConfigWriter(appendable);
    }

    @Override
    public ConfigWriter value(Object object) throws JSONException {
        super.value(object); return this;
    }

    @Override
    public ConfigWriter value(long l) throws JSONException {
        super.value(l); return this;
    }

    @Override
    public ConfigWriter value(double d) throws JSONException {
        super.value(d); return this;
    }

    @Override
    public ConfigWriter value(boolean b) throws JSONException {
        super.value(b); return this;
    }

    @Override
    public ConfigWriter object() throws JSONException {
        super.object(); return this;
    }

    @Override
    public ConfigWriter key(String string) throws JSONException {
        super.key(string); return this;
    }

    @Override
    public ConfigWriter endObject() throws JSONException {
        super.endObject(); return this;
    }

    @Override
    public ConfigWriter endArray() throws JSONException {
        super.endArray(); return this;
    }

    @Override
    public ConfigWriter array() throws JSONException {
        super.array(); return this;
    }
    
    private void properties(Properties properties) {
        for (Object key : properties.keySet()) {
            String strKey = key.toString();
            super.key(strKey).value(properties.getProperty(strKey));
        }
    }
    
    public ConfigWriter object(Object object) throws JSONException {
        return object().objectProperties(object).endObject();
    }
    
    public ConfigWriter objectProperties(Object object) throws JSONException {
        if (object == null) {
            return this;
        }
        try {
            if (object instanceof Properties) {
                properties((Properties) object);
                return this;
            }
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
                    key(propertyName).value(propertyValue);
                }
            }
            return this;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new JSONException("Error on writing JSON representation", ex);
        }
    }
}
