/*
 * License: GPL. Copyright 2014- (C) by Sebastian Aust (https://www.ryanthara.de/)
 *
 * This file is part of the package de.ryanthara.ja.rycon.gui
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

package de.ryanthara.ja.rycon.gui;

import com.opencsv.CSVReader;
import de.ryanthara.ja.rycon.Main;
import de.ryanthara.ja.rycon.data.I18N;
import de.ryanthara.ja.rycon.data.PreferenceHandler;
import de.ryanthara.ja.rycon.io.LineReader;
import de.ryanthara.ja.rycon.io.LineWriter;
import de.ryanthara.ja.rycon.tools.FileToolsCPlanK;
import de.ryanthara.ja.rycon.tools.FileToolsLeicaGSI;
import de.ryanthara.ja.rycon.tools.FileToolsText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a complete widget and it's functionality.
 * <p>
 * The ConverterWidget of RyCON is used to convert measurement and coordinate
 * files into different formats. RyCON can be used to convert special formats
 * e.g. coordinate files from governmental services in switzerland
 *
 * <h3>Changes:</h3>
 * <ul>
 *     <li>8: code optimizations, little corrections</li>
 *     <li>7: defeat txt to gsi conversion bug (not listed)</li>
 *     <li>6: implement support for cadwork-files (node.dat) </li>
 *     <li>5: defeat bug #3 </li>
 *     <li>4: simplification and improvements, extract input fields and bottom button bar into separate classes </li>
 *     <li>3: code improvements and clean up </li>
 *     <li>2: basic improvements </li>
 *     <li>1: basic implementation </li>
 * </ul>
 *
 * @author sebastian
 * @version 8
 * @since 1
 */
public class ConverterWidget {

    private Button chkBoxCadworkUseCode;
    private Button chkBoxCadworkUseZeroHeights;
    private Button chkBoxCSVSemiColonSeparator;
    private Button chkBoxSourceContainsCode;
    private Button chkBoxTXTSpaceSeparator;
    private Button chkBoxWriteCommentLine;
    private File[] files2read = new File[0];
    private Group groupSource;
    private Group groupTarget;
    private InputFieldsComposite inputFieldsComposite;
    private Shell innerShell = null;

    /**
     * Class constructor without parameters.
     * <p>
     * The user interface is initialized in a separate method, which is called from here.
     */
    public ConverterWidget() {

        // TODO Delete after test
        FileToolsCPlanK cPlanK = new FileToolsCPlanK();
        cPlanK.convertGSI2KFile();

        initUI();
    }

    private void initUI() {
        int height = Main.getRyCONWidgetHeight();
        int width = Main.getRyCONWidgetWidth();

        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gridData.heightHint = height;
        gridData.widthHint = width;

        innerShell = new Shell(Main.shell, SWT.CLOSE | SWT.DIALOG_TRIM | SWT.MAX | SWT.TITLE | SWT.APPLICATION_MODAL);
        innerShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                actionBtnCancel();
            }
        });
        innerShell.setText(I18N.getWidgetTitleConverter());
        innerShell.setSize(width, height);
        innerShell.setLayout(gridLayout);
        innerShell.setLayoutData(gridData);

        gridLayout = new GridLayout(1, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;

        inputFieldsComposite = new InputFieldsComposite(this, innerShell, SWT.NONE);
        inputFieldsComposite.setLayout(gridLayout);

        createCompositeSourceTarget();
        createOptions(width);
        createDescription(width);

        new BottomButtonBar(this, innerShell, SWT.NONE);

        innerShell.setLocation(ShellCenter.centerShellOnPrimaryMonitor(innerShell));

        Main.setSubShellStatus(true);

        innerShell.pack();
        innerShell.open();
    }

    private void createCompositeSourceTarget() {
        Composite compositeSourceTarget = new Composite(innerShell, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        compositeSourceTarget.setLayout(gridLayout);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        compositeSourceTarget.setLayoutData(gridData);

        groupSource = new Group(compositeSourceTarget, SWT.NONE);
        groupSource.setText(I18N.getGroupTitleSourceFileFormat());
        groupSource.setLayout(new GridLayout(2, false));

        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupSource.setLayoutData(gridData);

        groupTarget = new Group(compositeSourceTarget, SWT.NONE);
        groupTarget.setText(I18N.getGroupTitleTargetFileFormat());
        groupTarget.setLayout(new GridLayout(2, false));

        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupTarget.setLayoutData(gridData);


        SelectionListener selectionListenerSource = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // control of double fired events
                boolean isSelected = ((Button) e.getSource()).getSelection();
                if (isSelected) {
                    Control[] childrenSource = groupSource.getChildren();
                    Control[] childrenTarget = groupTarget.getChildren();

                    RadioHelper.toggleBtn(childrenSource, childrenTarget);
                }
            }
        };

        SelectionListener selectionListenerTarget = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // control of double fired events
                boolean isSelected = ((Button) e.getSource()).getSelection();
                if (isSelected) {
                    Control[] childrenSource = groupSource.getChildren();
                    Control[] childrenTarget = groupTarget.getChildren();

                    RadioHelper.toggleBtn(childrenTarget, childrenSource);
                }

            }
        };

        String[] formatSource = {"GSI8", "GSI16", "TXT", "CSV", "cadwork node.dat", "Basel Stadt CSV"};
        // String[] formatTarget = {"GSI8", "GSI16", "TXT", "CSV", "C-PLAN", "Excel 2007 (.xlsx)","Excel '97 (.xls)"};
        String[] formatTarget = {"GSI8", "GSI16", "TXT", "CSV", "C-PLAN"};

        for (int i = 0; i < formatSource.length; i++) {
            Button btnSourceFormats = new Button(groupSource, SWT.RADIO);
            btnSourceFormats.addSelectionListener(selectionListenerSource);
            btnSourceFormats.setText(formatSource[i]);

            if (i == 1) {
                btnSourceFormats.setSelection(true);
            }
        }

        for (int i = 0; i < formatTarget.length; i++) {
            Button btnTargetFormats = new Button(groupTarget, SWT.RADIO);
            btnTargetFormats.addSelectionListener(selectionListenerTarget);
            btnTargetFormats.setText(formatTarget[i]);

            if (i == 0) {
                btnTargetFormats.setSelection(true);
            }
        }

    }

    private void createOptions(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(I18N.getGroupTitleOptions());

        GridLayout gridLayout = new GridLayout(1, true);
        group.setLayout(gridLayout);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;
        group.setLayoutData(gridData);

        chkBoxSourceContainsCode = new Button(group, SWT.CHECK);
        chkBoxSourceContainsCode.setSelection(false);
        chkBoxSourceContainsCode.setText(I18N.getBtnChkBoxSourceContainsCode());

        chkBoxTXTSpaceSeparator = new Button(group, SWT.CHECK);
        chkBoxTXTSpaceSeparator.setSelection(false);
        chkBoxTXTSpaceSeparator.setText(I18N.getBtnChkConverterTXTSpaceSeparator());

        chkBoxCSVSemiColonSeparator = new Button(group, SWT.CHECK);
        chkBoxCSVSemiColonSeparator.setSelection(false);
        chkBoxCSVSemiColonSeparator.setText(I18N.getBtnChkConverterCSVSemiColonSeparator());

        chkBoxCadworkUseCode = new Button(group, SWT.CHECK);
        chkBoxCadworkUseCode.setSelection(false);
        chkBoxCadworkUseCode.setText(I18N.getBtnChkBoxCadworkUseCode());

        chkBoxCadworkUseZeroHeights = new Button(group, SWT.CHECK);
        chkBoxCadworkUseZeroHeights.setSelection(false);
        chkBoxCadworkUseZeroHeights.setText(I18N.getBtnChkBoxCadworkUseZeroHeights());

        chkBoxWriteCommentLine = new Button(group, SWT.CHECK);
        chkBoxWriteCommentLine.setSelection(false);
        chkBoxWriteCommentLine.setText(I18N.getBtnChkConverterWriteCommentLine());
    }

    private void createDescription(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(I18N.getGroupTitleGeneratorNumberInputAdvice());

        GridLayout gridLayout = new GridLayout(1, true);
        group.setLayout(gridLayout);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;
        group.setLayoutData(gridData);

        Label tip = new Label(group, SWT.WRAP | SWT.BORDER | SWT.LEFT);
        tip.setText(String.format(I18N.getLabelTipConverterWidget()));
        tip.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1));
    }

    private void actionBtnCancel() {
        Main.setSubShellStatus(false);
        Main.statusBar.setStatus("", StatusBar.OK);
        innerShell.dispose();
    }

    /*
     * This method is used from the class InputFieldsComposite!
     */
    private void actionBtnDestination() {
        String filterPath = Main.pref.getUserPref(PreferenceHandler.DIR_PROJECT);

        // Set the initial filter path according to anything selected or typed in
        if (!inputFieldsComposite.getDestinationTextField().getText().trim().equals("")) {
            File destinationDir = new File(inputFieldsComposite.getDestinationTextField().getText());
            if (destinationDir.isDirectory()) {
                filterPath = inputFieldsComposite.getDestinationTextField().getText();
            }
        }

        GuiHelper.showAdvancedDirectoryDialog(innerShell, inputFieldsComposite.getDestinationTextField(),
                I18N.getFileChooserConverterSourceText(), I18N.getFileChooserConverterSourceMessage(), filterPath);
    }

    private int actionBtnOk() {
        if (files2read.length == 0) {
            files2read = new File[1];
            files2read[0] = new File(inputFieldsComposite.getSourceTextField().getText());
        } else {
            files2read = WidgetHelper.checkSourceAndDestinationTextFields(
                    inputFieldsComposite.getSourceTextField(),
                    inputFieldsComposite.getDestinationTextField(), files2read);
        }
        if ((files2read != null) && (files2read.length > 0)) {
            if (processFileOperations()) {
                // use counter to display different text on the status bar
                if (Main.countFileOps == 1) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusConvertSuccess(Main.TEXT_SINGULAR), Main.countFileOps), StatusBar.OK);
                } else {
                    Main.statusBar.setStatus(String.format(I18N.getStatusConvertSuccess(Main.TEXT_PLURAL), Main.countFileOps), StatusBar.OK);
                }
            }
            return 1;
        }
        return 0;
    }

    /*
     * This method is used from the class BottomButtonBar!
     */
    private void actionBtnOkAndExit() {
        switch (actionBtnOk()) {
            case 0:

                break;
            case 1:
                Main.setSubShellStatus(false);
                Main.statusBar.setStatus("", StatusBar.OK);

                innerShell.dispose();
                break;
        }
    }

    /*
     * This method is used from the class InputFieldsComposite!
     */
    private void actionBtnSource() {
        FileDialog fileDialog = new FileDialog(innerShell, SWT.MULTI);

        String filterPath = Main.pref.getUserPref(PreferenceHandler.DIR_PROJECT);

        // Set the initial filter path according to anything pasted or typed in
        if (!inputFieldsComposite.getSourceTextField().getText().trim().equals("")) {
            File sourceFile = new File(inputFieldsComposite.getSourceTextField().getText());
            if (sourceFile.isDirectory()) {
                filterPath = inputFieldsComposite.getSourceTextField().getText();
            } else if (sourceFile.isFile()) {
                GuiHelper.prepareDestinationText(inputFieldsComposite.getDestinationTextField(), sourceFile);
            }
        }

        fileDialog.setFilterPath(filterPath);
        fileDialog.setText(I18N.getFileChooserTidyUpSourceText());
        fileDialog.setFilterExtensions(new String[]{"*.gsi", "*.txt", "*.csv", "*.dat"});
        fileDialog.setFilterNames(new String[]{
                I18N.getFileChooserFilterNameGSI(),
                I18N.getFileChooserFilterNameTXT(),
                I18N.getFileChooserFilterNameCSV(),
                I18N.getFileChooserFilterNameCadwork()});

        // determine filter index by source group selection
        int selectedBtnSource = RadioHelper.getSelectedBtn(groupSource.getChildren());
        switch (selectedBtnSource) {
            case 0:
            case 1:
                fileDialog.setFilterIndex(0);
                break;
            case 2:
                fileDialog.setFilterIndex(1);
                break;
            case 3:
                fileDialog.setFilterIndex(2);
                break;
            case 4:
                fileDialog.setFilterIndex(3);
                break;
            case 5:
                fileDialog.setFilterIndex(2);
                break;
        }

        String firstFile = fileDialog.open();

        if (firstFile != null) {
            String[] files = fileDialog.getFileNames();

            files2read = new File[files.length];

            // displaying file names without path in text field
            String concatString = "";

            String workingDir = fileDialog.getFilterPath();

            for (int i = 0; i < files.length; i++) {
                concatString = concatString.concat(files[i]);
                concatString = concatString.concat(" ");

                files2read[i] = new File(workingDir + File.separator + files[i]);
            }

            inputFieldsComposite.getDestinationTextField().setText(fileDialog.getFilterPath());
            inputFieldsComposite.getSourceTextField().setText(concatString);

            // set the radio buttons
            Control[] childrenSource = groupSource.getChildren();
            Control[] childrenTarget = groupTarget.getChildren();

            switch (fileDialog.getFilterIndex()) {
                case 0:
                    RadioHelper.selectBtn(childrenSource, 1);
                    RadioHelper.selectBtn(childrenTarget, 2);
                    break;

                case 1:
                    RadioHelper.selectBtn(childrenSource, 2);
                    RadioHelper.selectBtn(childrenTarget, 1);
                    break;

                case 2:
                    RadioHelper.selectBtn(childrenSource, 3);
                    RadioHelper.selectBtn(childrenTarget, 2);
                    break;

            }
        }
    }

    private boolean processFileOperations() {
        boolean success;
        boolean GSIFormat;

        int sourceNumber = RadioHelper.getSelectedBtn(groupSource.getChildren());
        int targetNumber = RadioHelper.getSelectedBtn(groupTarget.getChildren());

        if (sourceNumber == 0) {
            GSIFormat = Main.getGSI8();
        } else {
            GSIFormat = Main.getGSI16();
        }

        int counter = 0;

        for (File file2read : files2read) {
            boolean readFileSuccess = false;

            LineReader lineReader;

            List<String[]> readCSVFile = null;
            ArrayList<String> readFile = null;
            ArrayList<String> writeFile;

            FileToolsLeicaGSI gsiTools;
            FileToolsText fileToolsText;

            String separator;

            // read files
            switch (sourceNumber) {
                case 0:     // fall through for GSI8 format
                case 1:     // fall through GSI16 format
                case 2:     // TXT format (tabulator or space separated)
                    lineReader = new LineReader(file2read);
                    if (lineReader.readFile()) {
                        readFile = lineReader.getLines();
                        readFileSuccess = true;
                    } else {
                        System.err.println("File " + file2read.getName() + " could not be read.");

                        GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), I18N.getMsgConvertReaderFailed());
                    }
                    break;

                case 3:     // CSV format (comma or semicolon separated)
                    char separatorCSV = ',';

                    if (chkBoxCSVSemiColonSeparator.getSelection()) {
                        separatorCSV = ';';
                    }

                    // use opencsv project for reading -> could be done better?
                    try {
                        CSVReader reader = new CSVReader(new FileReader(file2read), separatorCSV);
                        readCSVFile = reader.readAll();
                        readFileSuccess = true;
                    } catch (IOException e) {
                        System.err.println("File " + file2read.getName() + " could not be read.");

                        GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), I18N.getMsgConvertReaderCSVFailed());
                    }
                    break;

                case 4:     // cadwork node.dat format from the cadwork CAD program
                    lineReader = new LineReader(file2read);
                    if (lineReader.readFile()) {
                        readFile = lineReader.getLines();
                        readFileSuccess = true;
                    } else {
                        System.err.println("File " + file2read.getName() + " could not be read.");

                        GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), I18N.getMsgConvertReaderFailed());
                    }
                    break;
                case 5:     // CSV format from the geo data server 'Basel Stadt' (http://shop.geo.bs.ch/geoshop_app/geoshop/)
                    try {
                        CSVReader reader = new CSVReader(new FileReader(file2read), ';', '"', 1); // skip first line
                        readCSVFile = reader.readAll();
                        readFileSuccess = true;
                    } catch (IOException e) {
                        System.err.println("File " + file2read.getName() + " could not be read.");

                        GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), I18N.getMsgConvertReaderCSVFailed());
                    }
                    break;
            }

            if (readFileSuccess) {
                // write files
                switch (targetNumber) {
                    case 0:     // GSI8 format
                        switch (sourceNumber) {
                            case 0:     // GSI8 format (not possible)
                                break;

                            case 1:     // GSI16 format
                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertGSI8vsGSI16(Main.getGSI8());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 2:     // TXT format (space or tabulator separated)
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertTXT2GSI(Main.getGSI8(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 3:     // CSV format (comma or semicolon separated)
                                gsiTools = new FileToolsLeicaGSI(readCSVFile);
                                writeFile = gsiTools.convertCSV2GSI(Main.getGSI8(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertCadwork2GSI(Main.getGSI8(),
                                        chkBoxCadworkUseCode.getSelection(),
                                        chkBoxCadworkUseZeroHeights.getSelection());

                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                gsiTools = new FileToolsLeicaGSI(readCSVFile);
                                writeFile = gsiTools.convertCSVBaselStadt2GSI(Main.getGSI8(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                        }
                        break;

                    case 1:     // GSI16 format
                        switch (sourceNumber) {
                            case 0:     // GSI8 format
                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertGSI8vsGSI16(Main.getGSI16());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 1:     // GSI16 format (not possible)
                                break;

                            case 2:     // TXT format (space or tabulator separated)
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertTXT2GSI(Main.getGSI16(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 3:     // CSV format (comma or semicolon separated)
                                gsiTools = new FileToolsLeicaGSI(readCSVFile);
                                writeFile = gsiTools.convertCSV2GSI(Main.getGSI16(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertCadwork2GSI(Main.getGSI16(),
                                        chkBoxCadworkUseCode.getSelection(),
                                        chkBoxCadworkUseZeroHeights.getSelection());

                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                gsiTools = new FileToolsLeicaGSI(readCSVFile);
                                writeFile = gsiTools.convertCSVBaselStadt2GSI(Main.getGSI16(),
                                        chkBoxSourceContainsCode.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".GSI")) {
                                    counter++;
                                }
                                break;

                        }
                        break;

                    case 2:     // TXT format (space or tabulator separated)
                        switch (sourceNumber) {
                            case 0:     // fall through for GSI8 format
                            case 1:     // GSI16 format
                                // get separator sign
                                if (chkBoxTXTSpaceSeparator.getSelection()) {
                                    separator = Main.getSeparatorSpace();
                                } else {
                                    separator = Main.getSeparatorTab();
                                }

                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertGSI2TXT(separator, GSIFormat, chkBoxWriteCommentLine.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".TXT")) {
                                    counter++;
                                }
                                break;

                            case 2:     // TXT format (not possible)
                                break;

                            case 3:     // CSV format (comma or semicolon separated)
                                // get separator sign
                                if (chkBoxTXTSpaceSeparator.getSelection()) {
                                    separator = Main.getSeparatorSpace();
                                } else {
                                    separator = Main.getSeparatorTab();
                                }

                                // process file operations
                                fileToolsText = new FileToolsText(readCSVFile);
                                writeFile = fileToolsText.convertCSV2TXT(separator);

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".TXT")) {
                                    counter++;
                                }
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                // get separator sign
                                if (chkBoxTXTSpaceSeparator.getSelection()) {
                                    separator = Main.getSeparatorSpace();
                                } else {
                                    separator = Main.getSeparatorTab();
                                }

                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                ArrayList<String> stopOver = gsiTools.convertCadwork2GSI(Main.getGSI16(),
                                        chkBoxCadworkUseCode.getSelection(),
                                        chkBoxCadworkUseZeroHeights.getSelection());
                                FileToolsLeicaGSI stopOverFile = new FileToolsLeicaGSI(stopOver);
                                writeFile = stopOverFile.convertGSI2TXT(separator, GSIFormat, chkBoxWriteCommentLine.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".TXT")) {
                                    counter++;
                                }
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                // get separator sign
                                if (chkBoxTXTSpaceSeparator.getSelection()) {
                                    separator = Main.getSeparatorSpace();
                                } else {
                                    separator = Main.getSeparatorTab();
                                }

                                fileToolsText = new FileToolsText(readCSVFile);
                                writeFile = fileToolsText.convertCSVBaselStadt2TXT(separator);

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".TXT")) {
                                    counter++;
                                }
                                break;

                        }
                        break;

                    case 3:     // CSV format (comma or semicolon separated)
                        switch (sourceNumber) {
                            case 0:     // fall through for GSI8 format
                            case 1:     // GSI16 format

                                // get separator sign
                                if (chkBoxCSVSemiColonSeparator.getSelection()) {
                                    separator = Main.getSeparatorSemicolon();
                                } else {
                                    separator = Main.getSeparatorComma();
                                }

                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                writeFile = gsiTools.convertGSI2CSV(separator, chkBoxWriteCommentLine.getSelection());

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".CSV")) {
                                    counter++;
                                }
                                break;

                            case 2:     // TXT format (space or tabulator separated)
                                // get separator sign
                                if (chkBoxCSVSemiColonSeparator.getSelection()) {
                                    separator = Main.getSeparatorSemicolon();
                                } else {
                                    separator = Main.getSeparatorComma();
                                }

                                // process file operations
                                fileToolsText = new FileToolsText(readFile);
                                writeFile = fileToolsText.convertTXT2CSV(separator);

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".CSV")) {
                                    counter++;
                                }
                                break;

                            case 3:     // CSV format (not possible)
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                break;
                            
                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                // get separator sign
                                if (chkBoxCSVSemiColonSeparator.getSelection()) {
                                    separator = Main.getSeparatorSemicolon();
                                } else {
                                    separator = Main.getSeparatorComma();
                                }

                                fileToolsText = new FileToolsText(readCSVFile);
                                writeFile = fileToolsText.convertCSVBaselStadt2TXT(separator);

                                // write file line by line
                                if (writeFile(file2read, writeFile, ".CSV")) {
                                    counter++;
                                }
                                break;

                        }
                        break;

                    case 4:     // C-PLAN (.K)
                        switch (sourceNumber) {
                            case 0:     // fall through for GSI8 format
                            case 1:     // GSI16 format
                                break;
                            case 2:     // TXT format (space or tabulator separated)
                                break;
                            case 3:     // CSV format (not possible)
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                break;
                        }

                        break;

                    case 5:     // Excel 2007 (.xlsx)
                        switch (sourceNumber) {
                            case 0:     // fall through for GSI8 format
                            case 1:     // GSI16 format
                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                gsiTools.prepareTableData(chkBoxWriteCommentLine.getSelection());
                                break;
                            case 2:     // TXT format (space or tabulator separated)
                                break;
                            case 3:     // CSV format (not possible)
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                break;

                        }

                        break;

                    case 6:     // Excel 97 (.xls)
                        switch (sourceNumber) {
                            case 0:     // fall through for GSI8 format
                            case 1:     // GSI16 format
                                // process file operations
                                gsiTools = new FileToolsLeicaGSI(readFile);
                                gsiTools.prepareTableData(chkBoxWriteCommentLine.getSelection());
                                break;
                            case 2:     // TXT format (space or tabulator separated)
                                break;
                            case 3:     // CSV format (not possible)
                                break;

                            case 4:     // cadwork node.dat from cadwork CAD program
                                break;

                            case 5:     // CSV format 'Basel Stadt' (semicolon separated)
                                break;
                        }
                        break;

                }
            }
        }

        if (counter > 0) {
            String message;

            if (counter == 1) {
                message = String.format(I18N.getMsgConvertSuccess(Main.TEXT_SINGULAR), counter);
            } else {
                message = String.format(I18N.getMsgConvertSuccess(Main.TEXT_PLURAL), counter);
            }

            GuiHelper.showMessageBox(innerShell, SWT.ICON_INFORMATION, I18N.getMsgBoxTitleSuccess(), message);

            // set the counter for status bar information
            Main.countFileOps = counter;
            success = true;
        } else {
            GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), I18N.getMsgConvertFailed());
            success = false;
        }

        return success;
    }

    private boolean writeFile(File file2read, ArrayList<String> writeFile, String suffix) {
        String fileName = file2read.toString().substring(0, file2read.toString().length() - 4) + "_CONV" + suffix;

        File file = new File(fileName);

        if (file.exists()) {
            int returnValue = GuiHelper.showMessageBox(innerShell, SWT.ICON_WARNING | SWT.YES | SWT.NO,
                    I18N.getMsgBoxTitleWarning(), String.format(I18N.getMsgFileExist(), fileName));

            if (returnValue == SWT.YES) {
                LineWriter lineWriter = new LineWriter(fileName);

                return lineWriter.writeFile(writeFile);
            } else {
                return false;
            }

        } else {
            return new LineWriter(fileName).writeFile(writeFile);
        }
    }

} // end of ConverterWidget
