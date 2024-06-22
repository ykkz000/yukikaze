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

import pers.ykkz000.yukikaze.framework.annotation.BindCommand;
import pers.ykkz000.yukikaze.framework.annotation.Controller;
import pers.ykkz000.yukikaze.framework.api.AnnotationProcessor;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public class BaseAnnotationProcessor implements AnnotationProcessor {
    private final ApplicationContext context;

    public BaseAnnotationProcessor(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void processBeanAnnotationsAfterCreateInstance(@Nonnull Object bean) {
        Class<?> clazz = bean.getClass();
        if (clazz.isAnnotationPresent(Controller.class)) {
            processControllerAnnotations(bean);
        }
    }

    private void processControllerAnnotations(@Nonnull Object bean) {
        Class<?> clazz = bean.getClass();
        Controller controller = clazz.getAnnotation(Controller.class);
        String prefix = controller.prefix();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(BindCommand.class)) {
                method.setAccessible(true);
                BindCommand bindCommand = method.getAnnotation(BindCommand.class);
                String command = prefix + bindCommand.value();
                context.getCommandRouter().addRoute(command, bean, method);
            }
        }
    }
}
