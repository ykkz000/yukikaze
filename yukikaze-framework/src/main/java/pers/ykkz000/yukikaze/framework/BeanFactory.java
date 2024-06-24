/*
 * yukikaze
 * Copyright (C) 2024  ykkz000
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pers.ykkz000.yukikaze.framework;

import pers.ykkz000.yukikaze.framework.annotation.AutoWire;
import pers.ykkz000.yukikaze.framework.annotation.BeanConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Bean factory
 *
 * @author ykkz000
 */
public class BeanFactory {
    private final ApplicationContext context;
    private final Map<String, String> beanClassNames;
    private final Map<String, ClassLoader> beanClassLoaders;
    private final Map<String, Object> beanInstances;

    public BeanFactory(ApplicationContext context) {
        this.context = context;
        beanClassNames = new Hashtable<>();
        beanClassLoaders = new Hashtable<>();
        beanInstances = new Hashtable<>();
    }

    public void defineBean(String name, String className, ClassLoader classLoader) {
        beanClassNames.put(name, className);
        beanClassLoaders.put(name, classLoader);
    }

    public Object getBean(String name) throws IllegalStateException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!beanClassNames.containsKey(name)) {
            throw new IllegalStateException("Bean not found: " + name);
        }
        if (!beanInstances.containsKey(name)) {
            beanInstances.put(name, createBeanInstance(beanClassNames.get(name), beanClassLoaders.get(name)));
        }
        return beanInstances.get(name);
    }
    
    protected Object createBeanInstance(String className, ClassLoader classLoader) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> beanClass = classLoader.loadClass(className);
        List<Constructor<?>> beanConstructors = Arrays.stream(beanClass.getDeclaredConstructors()).filter(constructor -> constructor.isAnnotationPresent(BeanConstructor.class)).toList();
        if (beanConstructors.size() > 1) {
            throw new IllegalStateException("Multiple or no bean constructors found for class: " + className);
        }
        Constructor<?> beanConstructor = beanConstructors.get(0);
        beanConstructor.setAccessible(true);
        Parameter[] parameters = beanConstructor.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].isAnnotationPresent(AutoWire.class)) {
                throw new IllegalStateException("No @AutoWire annotation found for parameter: " + parameters[i].getName());
            }
            String parameterBeanName = parameters[i].getAnnotation(AutoWire.class).value();
            args[i] = getBean(parameterBeanName);
        }
        context.getAnnotationProcessors().forEach(annotationProcessor -> annotationProcessor.processBeanAnnotationsBeforeCreateInstance(beanClass));
        Object bean = beanConstructor.newInstance(args);
        context.getAnnotationProcessors().forEach(annotationProcessor -> annotationProcessor.processBeanAnnotationsAfterCreateInstance(bean));
        return bean;
    }
}
