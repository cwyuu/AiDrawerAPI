package com.example.aidrawerapi.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class BeanUtils {

    public static <T, S> T convert(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            for (Field sourceField : source.getClass().getDeclaredFields()) {
                try {
                    Field targetField = targetClass.getDeclaredField(sourceField.getName());
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    targetField.set(target, sourceField.get(source));
                } catch (NoSuchFieldException e) {
                    // Ignore the field if it doesn't exist in the target class
                }
            }
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create an instance of " + targetClass.getName(), e);
        }
    }

    public static <S, T> T convertToContained(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            for (Field sourceField : source.getClass().getDeclaredFields()) {
                try {
                    Field targetField = targetClass.getDeclaredField(sourceField.getName());
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    targetField.set(target, sourceField.get(source));
                } catch (NoSuchFieldException e) {
                    // Ignore the field if it doesn't exist in the target class
                }
            }
            return target;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create an instance of " + targetClass.getName(), e);
        }
    }

}
