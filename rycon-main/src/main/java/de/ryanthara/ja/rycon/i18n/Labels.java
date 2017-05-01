/*
 * License: GPL. Copyright 2017- (C) by Sebastian Aust (https://www.ryanthara.de/)
 *
 * This file is part of the package de.ryanthara.ja.rycon.i18n
 *
 * This package is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This package is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this package. If not, see <http://www.gnu.org/licenses/>.
 */
package de.ryanthara.ja.rycon.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Labels handles character string for multi-language support in RyCON.
 * <p>
 * The strings for different label texts are stored in the Resource Bundle LabelBundle and loaded from this class.
 * <p>
 * <h3>Changes:</h3>
 * <ul>
 * <li>1: basic implementation </li>
 * </ul>
 *
 * @author sebastian
 * @since 1
 */
public class Labels {

    private static final String BUNDLE_NAME = "de/ryanthara/ja/rycon/gui/LabelBundle";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Return the key-text-pair which represents the character string.
     *
     * @param key key to look up
     *
     * @return matched text
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Returns the Caplan K column description string by a given string.
     *
     * @param columnType column type to get the string for
     * @return Caplan K description string
     */
    public static String getCaplanColumnTyp(String columnType) {
        return CaplanKDescription.getDescription(columnType);
    }

} // end of Labels