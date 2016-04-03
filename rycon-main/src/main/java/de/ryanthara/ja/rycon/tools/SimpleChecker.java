/*
 * License: GPL. Copyright 2014- (C) by Sebastian Aust (https://www.ryanthara.de/)
 *
 * This file is part of the package de.ryanthara.ja.rycon.tools
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

package de.ryanthara.ja.rycon.tools;

import org.eclipse.swt.widgets.Text;

import java.io.File;

/**
 * This class provides simple checking routines in RyCON.
 * <p>
 * The methods are called static from different classes.
 *
 * <h3>Changes:</h3>
 * <ul>
 *     <li>1: basic implementation </li>
 * </ul>
 *
 * @author sebastian
 * @version 1
 * @since 7
 *
 */
public class SimpleChecker {

    /**
     * Class constructor without parameters.
     */
    public SimpleChecker() {}

    /**
     * Checks a swt text (text field) for not being empty.
     * @param textField text to be checked
     * @return success of the check
     */
    public static boolean checkIsTextEmpty(Text textField) {
        return textField != null & (textField != null && textField.getText().trim().equals(""));
    }

    /**
     * Checks the content string of a swt text (text field) for being a valid file in the file system.
     * @param textField text to be checked
     * @return success of the check
     */
    public static boolean checkIsTextValidFile(Text textField) {
        if (!checkIsTextEmpty(textField)) {

            File f = new File(textField.getText());
            return f.exists();
        } else {
            return false;
        }
    }

} // end of SimpleChecker