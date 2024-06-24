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

import pers.ykkz000.yukikaze.framework.annotation.EnableModule;
import pers.ykkz000.yukikaze.framework.annotation.EnableModules;
import pers.ykkz000.yukikaze.framework.annotation.LoadProperties;
import pers.ykkz000.yukikaze.framework.api.AnnotationProcessor;
import pers.ykkz000.yukikaze.framework.api.ModuleStarter;
import pers.ykkz000.yukikaze.framework.api.annotation.DefineAnnotationProcessors;
import pers.ykkz000.yukikaze.framework.util.ResourceUtil;

import jakarta.annotation.Nonnull;
import pers.ykkz000.yukikaze.framework.util.YamlUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The entry point of the application.
 *
 * @author ykkz000
 */
public class YukikazeApplication {
    private final Class<?> mainClass;

    public YukikazeApplication(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public ApplicationContext run(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        ApplicationContext context = new ApplicationContext.Builder()
                .mainClass(mainClass)
                .args(args)
                .build();
        ClassPathBeanScanner classPathBeanScanner = new ClassPathBeanScanner();
        try {
            List<ModuleStarter> moduleStarters = processMainClassAnnotations(context, mainClass);
            List<String> autoLoadBeanNames = classPathBeanScanner.scanBeans(mainClass.getPackageName(), mainClass, context.getBeanFactory());
            for (String beanName : autoLoadBeanNames) {
                context.getBeanFactory().getBean(beanName);
            }
            for (ModuleStarter moduleStarter : moduleStarters) {
                moduleStarter.start(context);
            }
        } catch (IOException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return context;
    }

    private static List<ModuleStarter> processMainClassAnnotations(ApplicationContext context, @Nonnull Class<?> clazz) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<ModuleStarter> moduleStarters = new ArrayList<>();
        if (clazz.isAnnotationPresent(LoadProperties.class)) {
            processLoadPropertiesAnnotations(context, clazz);
        }
        if (clazz.isAnnotationPresent(EnableModules.class)) {
            moduleStarters.addAll(processEnableModulesAnnotations(context, clazz));
        }
        if (clazz.isAnnotationPresent(EnableModule.class)) {
            moduleStarters.addAll(processEnableModuleAnnotations(context, clazz));
        }
        return moduleStarters;
    }

    private static void processLoadPropertiesAnnotations(ApplicationContext context, @Nonnull Class<?> clazz) throws IOException {
        LoadProperties loadProperties = clazz.getAnnotation(LoadProperties.class);
        String[] value = loadProperties.path();
        for (String s : value) {
            YamlUtil.extractYamlProperties(ResourceUtil.getResourceInputStream(clazz, s), (key, obj) -> context.getProperties().put(key, obj.toString()));
        }
    }

    private static List<ModuleStarter> processEnableModulesAnnotations(ApplicationContext context, @Nonnull Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<ModuleStarter> moduleStarters = new ArrayList<>();
        EnableModules enableModules = clazz.getAnnotation(EnableModules.class);
        EnableModule[] modules = enableModules.value();
        for (EnableModule module : modules) {
            Class<?> moduleClass = module.value();
            if (moduleClass.isAnnotationPresent(DefineAnnotationProcessors.class)) {
                processDefineAnnotationProcessorsAnnotations(context, moduleClass);
            }
            ModuleStarter moduleInstance = (ModuleStarter) moduleClass.getDeclaredConstructor().newInstance();
            moduleStarters.add(moduleInstance);
        }
        return moduleStarters;
    }

    private static List<ModuleStarter> processEnableModuleAnnotations(ApplicationContext context, @Nonnull Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        EnableModule module = clazz.getAnnotation(EnableModule.class);
        Class<?> moduleClass = module.value();
        if (moduleClass.isAnnotationPresent(DefineAnnotationProcessors.class)) {
            processDefineAnnotationProcessorsAnnotations(context, moduleClass);
        }
        ModuleStarter moduleInstance = (ModuleStarter) moduleClass.getDeclaredConstructor().newInstance();
        return List.of(moduleInstance);
    }

    private static void processDefineAnnotationProcessorsAnnotations(ApplicationContext context, @Nonnull Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        DefineAnnotationProcessors defineAnnotationProcessors = clazz.getAnnotation(DefineAnnotationProcessors.class);
        Class<?>[] classes = defineAnnotationProcessors.value();
        for (Class<?> aClass : classes) {
            AnnotationProcessor annotationProcessor = (AnnotationProcessor) aClass.getDeclaredConstructor().newInstance();
            context.getAnnotationProcessors().add(annotationProcessor);
        }
    }

    public static ApplicationContext run(Class<?> mainClass, String[] args) {
        return new YukikazeApplication(mainClass).run(args);
    }
}