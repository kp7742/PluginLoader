package com.kmods.stub;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
class Reflector {
    static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Object[] args, Class<?>... parameterTypes){
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static Object invokeMethod(String className, Object obj, String methodName, Object[] args, Class<?>... parameterTypes){
        try {
            if (parameterTypes == null) {
                parameterTypes = new Class[0];
            }
            if (args == null) {
                args = new Object[0];
            }
            Class<?> clazz = Class.forName(className);
            return invokeMethod(clazz, obj, methodName, args, parameterTypes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Object getFieldValue(Class<?> clazz, Object obj, String fieldName){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static Object getFieldValue(String className, Object obj, String fieldName){
        try {
            Class<?> clazz = Class.forName(className);
            return getFieldValue(clazz, obj, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void setFieldValue(Class<?> clazz, Object obj, String fieldName, Object value){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setFieldValue(String className, Object obj, String fieldName, Object value){
        try {
            Class<?> clazz = Class.forName(className);
            setFieldValue(clazz, obj, fieldName, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
