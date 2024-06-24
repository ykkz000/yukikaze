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

package pers.ykkz000.yukikaze.framework.util;

import java.io.*;

/**
 * Util about resources.
 *
 * @author ykkz000
 */
public class ResourceUtil {
    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Get input stream of resource.
     * @param clazz Relative class.
     * @param rawPath Path of resource.
     * @return Input stream of resource.
     * @throws FileNotFoundException If resource not found
     */
    public static InputStream getResourceInputStream(Class<?> clazz, String rawPath) throws FileNotFoundException {
        if (rawPath.startsWith(CLASSPATH_PREFIX)) {
            String path = rawPath.substring(CLASSPATH_PREFIX.length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            return clazz.getResourceAsStream(path);
        } else {
            return new FileInputStream(rawPath);
        }
    }
}
