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

import java.io.Reader;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class YamlNode {
    private final Map<String, Object> properties;

    public YamlNode() {
        properties = new Hashtable<>();
    }

    public void load(Reader reader) {
        properties.putAll(new Yaml().load(reader));
    }

    public String value(String key) {
        if (!properties.containsKey(key)) {
            return null;
        }
        return properties.get(key).toString();
    }

    @SuppressWarnings("unchecked")
    public YamlNode child(String key) {
        YamlNode node = new YamlNode();
        node.properties.putAll((Map<String, Object>) properties.get(key));
        return node;
    }

    @SuppressWarnings("unchecked")
    public List<YamlNode> list(String key) {
        return ((List<Object>) properties.get(key)).stream().map(obj -> {
            YamlNode node = new YamlNode();
            node.properties.putAll((Map<String, Object>) obj);
            return node;
        }).toList();
    }
}
