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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import pers.ykkz000.yukikaze.framework.api.AnnotationProcessor;
import pers.ykkz000.yukikaze.framework.route.CommandRouter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Application Context.
 *
 * @author ykkz000
 */
public class ApplicationContext {
    @Getter
    private static ApplicationContext instance;
    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final List<AnnotationProcessor> annotationProcessors;
    @Getter
    private final BeanFactory beanFactory;
    @Getter
    private final Class<?> mainClass;
    @Getter
    private final String[] args;
    @Getter
    private final Map<String, String> properties;
    @Getter
    private final CommandRouter commandRouter;

    private ApplicationContext(Class<?> mainClass, String[] args) {
        instance = this;
        this.mainClass = mainClass;
        this.args = args;
        this.commandRouter = new CommandRouter(this);
        objectMapper = new ObjectMapper();
        annotationProcessors = new ArrayList<>();
        annotationProcessors.add(new BaseAnnotationProcessor(this));
        beanFactory = new BeanFactory(this);
        properties = new Hashtable<>();
    }

    public static class Builder {
        private Class<?> mainClass;
        private String[] args;

        public Builder mainClass(Class<?> mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Builder args(String[] args) {
            this.args = args;
            return this;
        }

        public ApplicationContext build() {
            return new ApplicationContext(mainClass, args);
        }
    }
}
