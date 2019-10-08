/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufes.inf.nemo.jsrun;

/**
 *
 * @author luciano
 */
public enum Constants {
    
    BASE_DIR,
    SCRIPT_DIR,
    OUTPUT_DIR
    ;
    
    private String getPropertyName() {
        return "annoscript.maven." + name();
    }
        
    public String get() {
        return System.getProperty(getPropertyName());
    }
    
    public void set(String propertyValue) {
        System.setProperty(getPropertyName(), propertyValue);
    }
    
    public boolean isNull() {
        return get() == null;
    }
}
