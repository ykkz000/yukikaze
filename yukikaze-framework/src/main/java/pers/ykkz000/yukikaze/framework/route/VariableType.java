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

import lombok.Getter;

@Getter
public enum VariableType {
    /**
     * String type.
     */
    STRING(String::valueOf),
    /**
     * Byte type.
     */
    BYTE(Byte::valueOf),
    /**
     * Character type.
     */
    CHARACTER(str -> str.charAt(0)),
    /**
     * Short type.
     */
    SHORT(Short::valueOf),
    /**
     * Integer type.
     */
    INTEGER(Integer::valueOf),
    /**
     * Long type.
     */
    LONG(Long::valueOf),
    /**
     * Float type.
     */
    FLOAT(Float::valueOf),
    /**
     * Long type.
     */
    DOUBLE(Double::valueOf),
    /**
     * Boolean type.
     */
    BOOLEAN(Boolean::valueOf);

    private final Parser parser;
    VariableType(Parser parser) {
        this.parser = parser;
    }

    public interface Parser {
        Object parse(String str) throws IllegalArgumentException;
    }
}
