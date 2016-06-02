package org.springlite.beans;

import java.beans.PropertyEditor;

/**
 * Created by swy on 2016/5/25.
 */
public interface PropertyEditorRegistry {

    void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);


    void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);

    PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);
}
