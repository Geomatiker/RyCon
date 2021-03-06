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
package de.ryanthara.ja.rycon.elements;

import de.ryanthara.ja.rycon.i18n.ResourceBundleUtils;
import de.ryanthara.ja.rycon.i18n.WordIndices;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import static de.ryanthara.ja.rycon.i18n.ResourceBundles.WORDINDICES;

/**
 * Instances of this class represents an object to store and handle the values of a Leica GSI block.
 * <p>
 * The Leica Geo Serial Interface (GSI) is a general purpose, serial data interface
 * for bi-directional communication between TPS Total Stations, Levelling instruments and computers.
 * <p>
 * The GSI interface is composed in a sequence of blocks, ending with a terminator (CR or CR/LF).
 * The later introduced enhanced GSI16 format starts every line with a <code>*</code> sign.
 *
 * @author sebastian
 * @version 5
 * @since 8
 */
public class GSIBlock {

    private boolean isGSI16;
    private int wordIndex;
    private String dataGSI, information, sign;

    /**
     * Constructs a new instance of this class given a read GSI block as string.
     * <p>
     * This constructor is used for reading Leica GSI formatted files.
     *
     * @param blockAsString complete GSI block as string
     */
    public GSIBlock(String blockAsString) {
        blockAsString = blockAsString.trim();

        this.isGSI16 = blockAsString.length() == 23;
        this.wordIndex = Integer.parseInt(blockAsString.substring(0, 2));
        this.information = blockAsString.substring(2, 6);
        this.sign = blockAsString.substring(6, 7);
        this.dataGSI = blockAsString.substring(7, blockAsString.length());
    }

    /**
     * Constructs a new instance of this class given its GSI8/GSI16 identifier, the word index, the line number and
     * the data string.
     * <p>
     * This constructor is used for the GSI block that contains the line number and the point number string. (WI=11)
     *
     * @param isGSI16    boolean for indicating a GSI16 file
     * @param wordIndex  word index (WI) of the block
     * @param lineNumber information for the point number (filled up with zeros)
     * @param dataGSI    GSI data as string
     */
    public GSIBlock(boolean isGSI16, int wordIndex, int lineNumber, String dataGSI) {
        int length = isGSI16 ? 16 : 8;

        if (wordIndex == 11) {
            this.wordIndex = wordIndex;
            this.information = String.format("%04d", lineNumber);
            this.sign = "+";
            this.dataGSI = fillWithZeros(length, dataGSI);
        }
    }

    // TODO: 23.10.16 implement unit handling

    /**
     * Constructs a new instance of this class given its GSI8/GSI16 identifier, the word index and the data string.
     * <p>
     * This constructor is used for all GSI blocks except the point number one (WI=11). The data string can contains
     * the sign ('+' or '-') or not.
     *
     * @param isGSI16   boolean for indicating a GSI16 file
     * @param wordIndex word index (WI) of the block
     * @param dataGSI   GSI data as string with sign
     */
    public GSIBlock(boolean isGSI16, int wordIndex, String dataGSI) {
        int length = isGSI16 ? 16 : 8;

        this.wordIndex = wordIndex;

        dataGSI = removeSign(dataGSI);                          // sets the sign internally

        if (wordIndex == 71) {                                  // code
            this.information = "..46";
        } else if ((wordIndex > 80) & (wordIndex < 90)) {       // coordinates
            this.information = "..46";

            try {
                Double d = Double.parseDouble(dataGSI);
                if (d == 0d) {
                    dataGSI = "0";
                } else {
                    d = d * 10000.0; // value d in 1/10mm

                    BigDecimal bigDecimal = new BigDecimal(d);
                    bigDecimal = bigDecimal.setScale(0, RoundingMode.HALF_UP);

                    dataGSI = bigDecimal.toString();
                }
            } catch (NumberFormatException e) {
                System.err.println("Error while parsing String to double in GSIBlock:GSIBlock()");
                e.printStackTrace();
            }
        } else {
            // not used other values
            this.information = "..4.";
        }

        this.dataGSI = fillWithZeros(length, dataGSI);
    }

    /**
     * Constructs a new instance of this class given its GSI8/GSI16 identifier, the word index, the information
     * related to data, the sign and the data string.
     * <p>
     * This constructor is additionally used to change between GSI8 and GSI16 data in the levelling widgets.
     *
     * @param isGSI16     boolean for indicating a GSI16 file
     * @param wordIndex   word index (pos 1-2)
     * @param information information related to data (pos 3-6)
     * @param sign        sign (+ or -)(pos 7)
     * @param dataGSI     GSI8 data (pos 8-15) or GSI16 data (pos8-23)
     */
    public GSIBlock(boolean isGSI16, int wordIndex, String information, String sign, String dataGSI) {
        this.wordIndex = wordIndex;
        this.information = information;
        this.sign = sign;

        char[] leadingZeros;

        // fill the GSI data up to 8 or 16 signs with leading zeros
        if (isGSI16) {
            leadingZeros = new char[16 - dataGSI.length()];
        } else {
            leadingZeros = new char[8 - dataGSI.length()];
        }
        Arrays.fill(leadingZeros, '0');

        this.dataGSI = new String(leadingZeros) + dataGSI.substring(0, dataGSI.length());
    }

    /**
     * Returns the gsi data as string
     *
     * @return gsi data as string
     */
    public String getDataGSI() {
        return dataGSI;
    }

    /**
     * Returns the word index as integer value.
     *
     * @return word index as integer value
     */
    public int getWordIndex() {
        return wordIndex;
    }

    /**
     * Returns true if GSIBlock is GSI16 format.
     *
     * @return true if GSIBlock is GSI16 format
     */
    public boolean isGSI16() {
        return isGSI16;
    }

    /**
     * Returns a GSIBlock in csv format without separation sign. No additional invisible spaces are created.
     *
     * @return formatted {@code String} for CSV output
     */
    public String toPrintFormatCSV() {
        return this.toPrintFormatTXT().trim();
    }

    /**
     * Returns a GSIBlock in a printable format filled up with invisible spaces to a defined length (e.g. 16 characters).
     *
     * @return formatted {@code String} for column based TXT output
     */
    // TODO: 29.10.16 checks the right length for print string length (16, 17, sign)
    public String toPrintFormatTXT() {
        String s = this.dataGSI;
        int length = s.length();

        StringBuilder stringBuilder;

        switch (wordIndex) {
            case 11:        // point number
                s = preparePrintString(s);
                break;
            case 21:        // angle Hz
            case 22:        // angle Vz
            case 24:        // angle Hz0
            case 25:        // angle difference (Hz0 - Hz)
                if (this.information.endsWith("2") || this.information.endsWith("3")) {
                    stringBuilder = new StringBuilder(s);
                    s = stringBuilder.insert(length - 5, ".").toString();
                    s = trimLeadingZeros(s);
                    s = fillWithSpaces(length + 1, s);
                }
                break;
            case 26:        // offset
            case 27:        // angle Vz0
            case 28:        // angle difference (Vz0 - Vz)
                break;
            case 31:        // slope distance
            case 32:        // horizontal distance
            case 33:        // height difference
                stringBuilder = new StringBuilder(s);

                if (this.information.endsWith("0")) {
                    s = stringBuilder.insert(length - 3, ".").toString();
                } else if (this.information.endsWith("6")) {
                    s = stringBuilder.insert(length - 4, ".").toString();
                } else if (this.information.endsWith("8")) {
                    s = stringBuilder.insert(length - 5, ".").toString();
                } else {
                    s = stringBuilder.insert(length - 3, ".").toString();
                }

                s = insertMinusSign(s);
                s = fillWithSpaces(length + 2, s);
                break;
            case 41:        // code
                s = trimLeadingZeros(s);
                s = fillWithSpaces(length, s);
                break;
            case 58:        // addition constant in 1/10 mm
                stringBuilder = new StringBuilder(s);
                s = stringBuilder.insert(length - 4, ".").toString();

                s = this.sign + trimLeadingZeros(s);
                s = fillWithSpaces(length, s);
                break;
            case 71:        // comment 1, mostly used for code
            case 72:        // attribute 1
            case 73:        // attribute 2
            case 74:        // attribute 3
            case 75:        // attribute 4
            case 76:        // attribute 5
            case 77:        // attribute 6
            case 78:        // attribute 7
            case 79:        // attribute 8
                s = preparePrintString(s);
                break;
            case 81:        // easting E
            case 82:        // northing N
            case 83:        // height H
            case 84:        // easting E0
            case 85:        // northing N0
            case 86:        // height H0
            case 87:        // target height
            case 88:        // instrument height
                stringBuilder = new StringBuilder(s);

                if (this.information.endsWith("0")) {           // mm
                    s = stringBuilder.insert(this.dataGSI.length() - 3, ".").toString();
                } else if (this.information.endsWith("6")) {    // 1/10 mm
                    s = stringBuilder.insert(this.dataGSI.length() - 4, ".").toString();
                }

                s = insertMinusSign(s);

                // add two spaces, one for the sign and one for the decimal dot
                s = fillWithSpaces(length + 2, s);
                break;
            default:
                s = ResourceBundleUtils.getLangString(WORDINDICES, WordIndices.WI9999);
                System.err.println("GSIBlock.toPrintFormatTXT() : block contains the wrong word index " + dataGSI);
        }

        return s;
    }

    /**
     * Returns a GSIBlock as String in the origin format.
     *
     * @return GSIBlock as String
     */
    public String toString() {
        return wordIndex + information + sign + dataGSI;
    }

    /**
     * Returns a GSIBlock as String in defined format (GSI8 or GSI16).
     * <p>
     * Due to issues of the format, leading zeros are added or values are cut off.
     *
     * @param isGSI16 True for GSI16 format
     *
     * @return GSIBlock as String depending on format GSI8/GSI16
     */
    public String toString(boolean isGSI16) {
        String data;
        String leadingZeros = "00000000";
        String result;

        if (isGSI16) {
            result = wordIndex + information + sign;

            data = dataGSI.length() == 8 ? leadingZeros.concat(dataGSI) : dataGSI;

            result = result.concat(data);
        } else {
            if (dataGSI.length() == 8) {
                result = wordIndex + information + sign + dataGSI;
            } else {
                result = wordIndex + information + sign + dataGSI.substring(dataGSI.length() - 8, dataGSI.length());
            }
        }

        return result;
    }

    private String fillWithSpaces(int length, String input) {
        String format = "%" + length + "." + length + "s";
        return String.format(format, input);
    }

    private String fillWithZeros(int length, String input) {
        String format = "%" + length + "s";
        return String.format(format, input).replace(' ', '0');
    }

    private String insertMinusSign(String s) {
        if (this.sign.equals("-")) {
            return this.sign + trimLeadingZeros(s);
        } else {
            return trimLeadingZeros(s);
        }
    }

    private String preparePrintString(String s) {
        return fillWithSpaces(s.length(), trimLeadingZeros(s));
    }

    private String removeSign(String dataGSI) {
        if (dataGSI.startsWith("+")) {
            sign = "+";
            return dataGSI.substring(1, dataGSI.length());
        } else if (dataGSI.startsWith("-")) {
            sign = "-";
            return dataGSI.substring(1, dataGSI.length());
        } else {
            sign = "+";
            return dataGSI;
        }
    }

    private String trimLeadingZeros(String s) {
        // cut off leading zeros with regex;
        String intern = s.replaceFirst("^0+(?!$)", "");

        if (intern.startsWith(".")) {
            return "0" + intern;
        } else {
            return intern;
        }
    }

} // end of GSIBlock
