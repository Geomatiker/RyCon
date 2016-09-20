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
package de.ryanthara.ja.rycon.tools;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * FileToolsExcel implements basic file operations to write Microsoft xls, xlsx, ... files.
 *
 * @author sebastian
 * @version 2
 * @since 9
 */
public class FileToolsExcel {

    private Workbook workbook;

    /**
     * Class constructor for writing the filled table to the file system.
     *
     * @param workbook {@link Workbook} object
     */
    public FileToolsExcel(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Writes the converted XLS file to the file system.
     *
     * @param writeFile file to be written
     *
     * @return success write success
     */
    public boolean writeXLS(File writeFile) {
        try (FileOutputStream fileOut = new FileOutputStream(writeFile)) {
            workbook.write(fileOut);

            fileOut.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error while writing XLS file to disk.");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Writes the converted XLSX file to the file system.
     *
     * @param writeFile file to be written
     *
     * @return success write success
     */
    public boolean writeXLSX(File writeFile) {
        try (FileOutputStream fileOut = new FileOutputStream(writeFile)) {

            workbook.write(fileOut);

            fileOut.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error while writing XLSX file to disk.");
            e.printStackTrace();
        }

        return false;
    }

} // end of FileToolsExcel
