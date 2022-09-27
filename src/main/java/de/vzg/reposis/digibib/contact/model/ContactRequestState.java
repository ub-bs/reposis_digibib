/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.reposis.digibib.contact.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Contants that describe request states.
 */
public enum ContactRequestState {
    RECEIVED(0), PROCESSING(1), PROCESSING_FAILED(2), PROCESSED(3), SENDING(4), SENDING_FAILED(5), SENT(6),
    CONFIRMED(7);

    private final int value;

    ContactRequestState(int value) {
        this.value = value;
    }

    public static ContactRequestState resolve(int value) {
        return Arrays.stream(values()).filter(o -> o.value == value).findFirst().orElse(null);
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
