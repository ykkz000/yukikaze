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

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Util for yaml
 *
 * @author ykkz000
 */
public class YamlUtil {
    public static void extractYamlProperties(InputStream inputStream, BiConsumer<String, Object> consumer) {
        Map<String, Object> map = new Yaml().load(inputStream);
        extractYamlProperties("", map, consumer);
    }
    /**
     * Extract yaml properties
     * @param prefix Prefix of the key
     * @param currentNode Current YAML node
     * @param consumer Consumer
     */
    public static void extractYamlProperties(String prefix, Map<String, Object> currentNode, BiConsumer<String, Object> consumer) {
        for (Map.Entry<String, Object> entry : currentNode.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                extractYamlProperties(prefix + key + ".", (Map<String, Object>) value, consumer);
            } else {
                consumer.accept(prefix + key, value);
            }
        }
    }
}
