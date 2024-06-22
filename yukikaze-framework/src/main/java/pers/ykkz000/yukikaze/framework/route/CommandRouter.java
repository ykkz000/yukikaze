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

package pers.ykkz000.yukikaze.framework.route;

import org.apache.commons.math3.util.Pair;
import pers.ykkz000.yukikaze.framework.ApplicationContext;
import pers.ykkz000.yukikaze.framework.annotation.ParamVariable;
import pers.ykkz000.yukikaze.framework.exception.ResponseErrorException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class CommandRouter {
    private final ApplicationContext context;
    private final Map<String, HandlerEntry> queryHandlers;

    public CommandRouter(ApplicationContext context) {
        this.context = context;
        this.queryHandlers = new Hashtable<>();
    }

    public String execute(String command, Map<String, String> args) throws ResponseErrorException {
        HandlerEntry entry = queryHandlers.get(command);
        if (entry == null) {
            throw new ResponseErrorException(404, "Command not found");
        }
        try {
            return entry.execute(context, args);
        } catch (Exception e) {
            throw new ResponseErrorException(500, e.getMessage(), e);
        }
    }

    public void addRoute(String command, Object instance, Method method) throws IllegalArgumentException {
        List<Pair<String, VariableType>> variableTypes = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(ParamVariable.class)) {
                throw new IllegalArgumentException("Parameter " + parameter.getName() + " of method " + method.getName() + " has no @ParamVariable");
            }
            Class<?> type = parameter.getType();
            VariableType variableType;
            if (type == String.class) {
                variableType = VariableType.STRING;
            } else if (type == byte.class || type == Byte.class) {
                variableType = VariableType.BYTE;
            } else if (type == char.class || type == Character.class) {
                variableType = VariableType.CHARACTER;
            } else if (type == int.class || type == Integer.class) {
                variableType = VariableType.INTEGER;
            } else if (type == short.class || type == Short.class) {
                variableType = VariableType.SHORT;
            } else if (type == long.class || type == Long.class) {
                variableType = VariableType.LONG;
            } else if (type == float.class || type == Float.class) {
                variableType = VariableType.FLOAT;
            } else if (type == double.class || type == Double.class) {
                variableType = VariableType.DOUBLE;
            } else if (type == boolean.class || type == Boolean.class) {
                variableType = VariableType.BOOLEAN;
            } else {
                throw new IllegalArgumentException("Parameter " + parameter.getName() + " of method " + method.getName() + " has invalid type");
            }
            variableTypes.add(new Pair<>(parameter.getAnnotation(ParamVariable.class).value(), variableType));
        }
        queryHandlers.put(command, new HandlerEntry(instance, method, variableTypes));
    }

    protected record HandlerEntry(Object instance, Method method, List<Pair<String, VariableType>> variableTypes) {
        private String execute(ApplicationContext context, Map<String, String> args) throws ResponseErrorException {
            Object[] params = new Object[variableTypes.size()];
            for (int i = 0; i < variableTypes.size(); i++) {
                VariableType type = variableTypes.get(i).getSecond();
                try {
                    params[i] = type.getParser().parse(args.get(variableTypes.get(i).getFirst()));
                } catch (IllegalArgumentException e) {
                    throw new ResponseErrorException(400, "Invalid request");
                }
            }
            try {
                Object result = method.invoke(instance, params);
                if (result instanceof String || result instanceof Integer || result instanceof Boolean ||
                        result instanceof Float || result instanceof Double || result instanceof Byte ||
                        result instanceof Character || result instanceof Short || result instanceof Long) {
                    return result.toString();
                } else {
                    return context.getObjectMapper().writeValueAsString(result);
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof ResponseErrorException) {
                    throw (ResponseErrorException) e.getCause();
                } else {
                    throw new ResponseErrorException(500, e.getCause().getMessage(), e.getCause());
                }
            } catch (Exception e) {
                throw new ResponseErrorException(500, e.getMessage(), e);
            }
        }
    }
}
