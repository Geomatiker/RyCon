/*
 * License: GPL. Copyright 2016- (C) by Sebastian Aust (https://www.ryanthara.de/)
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
package de.ryanthara.ja.rycon.converter.excel;

/**
 * This class provides static access to members to distinguish between XLS (true) and XLSX (false) output files.
 *
 * @author sebastian
 * @version 1
 * @since 12
 */
public class BaseToolsExcel {

    /**
     * Member which helps distinguish between XLS and XLSX file format.
     * <p>
     * The member isXLS holds 'true'
     */
    public static final boolean isXLS = true;

    /**
     * Member which helps distinguish between XLS and XLSX file format.
     * <p>
     * The member isXLSX holds 'false'
     */
    public static final boolean isXLSX = false;

} // end of BaseToolsExcel
