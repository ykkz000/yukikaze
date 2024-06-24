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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import pers.ykkz000.yukikaze.framework.annotation.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scanner for classpath to scan beans.
 *
 * @author ykkz000
 */
public class ClassPathBeanScanner {
    public List<String> scanBeans(String basePackage, Class<?> clazz, BeanFactory beanFactory) throws IOException {
        List<String> autoLoadBeanNames = new ArrayList<>();
        String basePackagePath = basePackage.replaceAll("\\.", "/");
        Enumeration<URL> dirs = clazz.getClassLoader().getResources(basePackagePath);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                File file = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
                scanBeansInDirectory(file, clazz, beanFactory, autoLoadBeanNames);
            } else if ("jar".equals(protocol)) {
                JarURLConnection jarUrlConnection = (JarURLConnection) url.openConnection();
                JarFile jarFile = jarUrlConnection.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while  (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.endsWith(".class") && entryName.startsWith(basePackagePath)) {
                        ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, 0);
                        processClass(classNode, clazz, beanFactory, autoLoadBeanNames);
                    }
                }
            }
        }
        return autoLoadBeanNames;
    }

    public void scanBeansInDirectory(File parent, Class<?> clazz, BeanFactory beanFactory, List<String> autoLoadBeanNames) throws IOException {
        File[] files = parent.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanBeansInDirectory(file, clazz, beanFactory, autoLoadBeanNames);
            } else if (file.canRead()) {
                String filePath = file.getPath();
                if (filePath.endsWith(".class")) {
                    ClassReader classReader = new ClassReader(new FileInputStream(file));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    processClass(classNode, clazz, beanFactory, autoLoadBeanNames);
                }
            }
        }
    }

    private void processClass(ClassNode classNode, Class<?> clazz, BeanFactory beanFactory, List<String> autoLoadBeanNames) {
        String className = Type.getObjectType(classNode.name).getClassName();
        if (classNode.visibleAnnotations == null) {
            return;
        }
        Optional<AnnotationNode> beanNodeOptional = classNode.visibleAnnotations.stream()
                .filter(annotationNode -> isBeanAnnotation(annotationNode.desc))
                .findAny();
        if (beanNodeOptional.isEmpty()) {
            return;
        }
        AnnotationNode beanNode = beanNodeOptional.get();
        String name = null;
        boolean autoLoad = false;
        for (int i = 0; i < beanNode.values.size(); i += 2) {
            if ("name".equals(beanNode.values.get(i))) {
                name = (String) beanNode.values.get(i + 1);
            } else if ("autoLoad".equals(beanNode.values.get(i))) {
                autoLoad = (boolean) beanNode.values.get(i + 1);
            }
        }
        beanFactory.defineBean(name, className, clazz.getClassLoader());
        if (autoLoad) {
            autoLoadBeanNames.add(name);
        }
    }

    private static boolean isBeanAnnotation(String desc) {
        return desc.equals(Type.getType(Bean.class).getDescriptor());
    }
}
