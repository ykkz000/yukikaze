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

package pers.ykkz000.yukikaze.framework.exception;

import lombok.Getter;

@Getter
public class ResponseErrorException extends Exception {
    private final int status;

    public ResponseErrorException(int status) {
        this.status = status;
    }

    public ResponseErrorException(int status, String message) {
        super(message);
        this.status = status;
    }

    public ResponseErrorException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public ResponseErrorException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public ResponseErrorException(int status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }
}
