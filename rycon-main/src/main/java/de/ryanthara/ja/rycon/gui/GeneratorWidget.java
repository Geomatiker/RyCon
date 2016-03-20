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

import de.ryanthara.ja.rycon.Main;
import de.ryanthara.ja.rycon.data.I18N;
import de.ryanthara.ja.rycon.data.PreferenceHandler;
import de.ryanthara.ja.rycon.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;

/**
 * This class implements a complete widget and it's functionality.
 * <p>
 * The GeneratorWidget of RyCON is used to generate folders and substructures
 * in a default path by a given point number.
 *
 * <h3>Changes:</h3>
 * <ul>
 *     <li>4: implementation of a new directory structure, code reformat, optimizations</li>
 *     <li>3: code improvements and clean up </li>
 *     <li>2: basic improvements </li>
 *     <li>1: basic implementation </li>
 * </ul>
 *
 * @author sebastian
 * @version 4
 * @since 1
 */
public class GeneratorWidget {

    private final int TYPE_PROJECT = 1;
    private final int TYPE_ADMIN = 2;
    private final int TYPE_BIG_DATA = 3;
    private Button chkBoxCreateAdminFolder;
    private Button chkBoxCreateBigDataFolder;
    private Button chkBoxCreateProjectFolder;
    private Text inputNumber = null;
    private Shell innerShell = null;

    /**
     * Class constructor without parameters.
     * <p>
     * The user interface is initialized in a separate method, which is called from here.
     */
    GeneratorWidget() {
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
        innerShell.setText(I18N.getWidgetTitleGenerator());
        innerShell.setSize(width, height);
        innerShell.setLayout(gridLayout);
        innerShell.setLayoutData(gridData);

        gridLayout = new GridLayout(1, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;

        createGroupInputField();
        createGroupOptions(width);
        createDescription(width);
        createBottomButtons();

        innerShell.setLocation(ShellPositioner.centerShellOnPrimaryMonitor(innerShell));

        Main.setSubShellStatus(true);

        innerShell.pack();
        innerShell.open();
    }

    private void createGroupInputField() {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(I18N.getGroupTitleGeneratorNumberInput());

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        gridLayout.numColumns = 3;
        group.setLayout(gridLayout);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = Main.getRyCONWidgetWidth();
        group.setLayoutData(gridData);

        Label projectNumberLabel = new Label(group, SWT.NONE);
        projectNumberLabel.setText(I18N.getLabelTextProjectNumber());

        inputNumber = new Text(group, SWT.SINGLE | SWT.BORDER);

        // platform independent key handling for ENTER, TAB, ...
        // TODO change bad listener with a better one
        inputNumber.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (!inputNumber.getText().trim().equals("")) {
                    if ((event.stateMask & SWT.SHIFT) == SWT.SHIFT && event.detail == SWT.TRAVERSE_RETURN) {
                        actionBtnOkAndExit();
                    } else if (event.detail == SWT.TRAVERSE_RETURN) {
                        actionBtnOk();
                    }
                }
            }
        });

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        inputNumber.setLayoutData(gridData);
    }

    private void createGroupOptions(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(I18N.getGroupTitleOptions());

        GridLayout gridLayout = new GridLayout(1, true);
        group.setLayout(gridLayout);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;
        group.setLayoutData(gridData);

        chkBoxCreateProjectFolder = new Button(group, SWT.CHECK);
        chkBoxCreateProjectFolder.setSelection(false);
        chkBoxCreateProjectFolder.setText(I18N.getBtnChkBoxCreateProjectFolder());

        chkBoxCreateAdminFolder = new Button(group, SWT.CHECK);
        chkBoxCreateAdminFolder.setSelection(true);
        chkBoxCreateAdminFolder.setText(I18N.getBtnChkBoxCreateAdminFolder());

        chkBoxCreateBigDataFolder = new Button(group, SWT.CHECK);
        chkBoxCreateBigDataFolder.setSelection(false);
        chkBoxCreateBigDataFolder.setText(I18N.getBtnChkBoxCreateBigDataFolder());
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
        tip.setText(I18N.getLabelTipGeneratorWidget());
        tip.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1));
    }

    private void createBottomButtons() {
        Composite composite = new Composite(innerShell, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        Composite compositeLeft = new Composite(composite, SWT.NONE);
        compositeLeft.setLayout(new FillLayout());

        Button btnSettings = new Button(compositeLeft, SWT.NONE);
        btnSettings.setText(I18N.getBtnSettingsLabel());
        btnSettings.setToolTipText(I18N.getBtnSettingsLabelToolTip());
        btnSettings.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnSettings();
            }
        });

        Composite compositeRight = new Composite(composite, SWT.NONE);
        compositeRight.setLayout(new FillLayout());

        Button btnCancel = new Button(compositeRight, SWT.NONE);
        btnCancel.setText(I18N.getBtnCancelLabel());
        btnCancel.setToolTipText(I18N.getBtnCancelLabelToolTip());
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnCancel();
            }
        });

        Button btnOK = new Button(compositeRight, SWT.NONE);
        btnOK.setText(I18N.getBtnOKAndOpenLabel());
        btnOK.setToolTipText(I18N.getBtnOKAndOpenLabelToolTip());
        btnOK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnOk();
            }
        });

        Button btnOKAndExit = new Button(compositeRight, SWT.NONE);
        btnOKAndExit.setText(I18N.getBtnOKAndExitLabel());
        btnOKAndExit.setToolTipText(I18N.getBtnOKAndExitLabelToolTip());
        btnOKAndExit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnOkAndExit();
            }
        });
    }

    private void actionBtnCancel() {
        Main.setSubShellStatus(false);
        Main.statusBar.setStatus("", StatusBar.OK);
        innerShell.dispose();
    }

    private int actionBtnOk() {
        String number = inputNumber.getText();

        if (number.trim().equals("")) {
            GuiHelper.showMessageBox(innerShell, SWT.ICON_WARNING, I18N.getMsgBoxTitleWarning(), I18N.getMsgEmptyTextFieldWarning());

            return 0;
        } else {
            if (generateFolders(number)) {
                if (chkBoxCreateAdminFolder.getSelection() && chkBoxCreateBigDataFolder.getSelection() && chkBoxCreateProjectFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFoldersAdminAndBigDataAndProjectGenerated(), number, number, number), StatusBar.OK);
                } else if (chkBoxCreateAdminFolder.getSelection() && chkBoxCreateBigDataFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFoldersAdminAndBigDataGenerated(), number, number), StatusBar.OK);
                } else if (chkBoxCreateAdminFolder.getSelection() && chkBoxCreateProjectFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFoldersAdminAndProjectGenerated(), number, number), StatusBar.OK);
                } else if (chkBoxCreateBigDataFolder.getSelection() && chkBoxCreateProjectFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFoldersBigDataAndProjectGenerated(), number, number), StatusBar.OK);
                } else if (chkBoxCreateAdminFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFolderAdminGenerated(), number), StatusBar.OK);
                } else if (chkBoxCreateBigDataFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFolderBigDataGenerated(), number), StatusBar.OK);
                } else if (chkBoxCreateProjectFolder.getSelection()) {
                    Main.statusBar.setStatus(String.format(I18N.getStatusFolderProjectGenerated(), number), StatusBar.OK);
                }
            }

            return 1;
        }
    }

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

    private void actionBtnSettings() {
        new GeneratorSettingsWidget(innerShell);
    }

    private boolean generateFolders(String number) {
        boolean isAdminFolderGenerated = false;
        boolean isBigDataFolderGenerated = false;
        boolean isProjectFolderGenerated = false;

        if (chkBoxCreateAdminFolder.getSelection()) {
           isAdminFolderGenerated = generateAdminFolder(number, TYPE_ADMIN);
        }
        if (chkBoxCreateBigDataFolder.getSelection()) {
            isBigDataFolderGenerated = generateBigDataFolder(number, TYPE_BIG_DATA);
        }
        if (chkBoxCreateProjectFolder.getSelection()) {
            isProjectFolderGenerated = generateProjectFolder(number, TYPE_PROJECT);
        }

        if (isAdminFolderGenerated && isBigDataFolderGenerated && isProjectFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirAdminAndBigDataAndProjectGenerated(), number),
                    String.format(I18N.getMsgDirAdminAndBigDataAndProjectGenerated(), number));
        } else if (isAdminFolderGenerated && isBigDataFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirAdminAndBigDataGenerated(), number),
                    String.format(I18N.getMsgDirAdminAndBigDataGenerated(), number));
        } else if (isAdminFolderGenerated && isProjectFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirAdminAndProjectGenerated(), number),
                    String.format(I18N.getMsgDirAdminAndProjectGenerated(), number));
        } else if (isBigDataFolderGenerated && isProjectFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirBigDataAndProjectGenerated(), number),
                    String.format(I18N.getMsgDirBigDataAndProjectGenerated(), number));
        } else if (isAdminFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirAdminGenerated(), number),
                    String.format(I18N.getMsgDirAdminGenerated(), number));
        } else if (isBigDataFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirBigDataGenerated(), number),
                    String.format(I18N.getMsgDirBigDataGenerated(), number));
        } else if (isProjectFolderGenerated) {
            GuiHelper.showMessageBox(innerShell,SWT.ICON_INFORMATION,
                    String.format(I18N.getTextDirProjectGenerated(), number),
                    String.format(I18N.getMsgDirProjectGenerated(), number));
        }

        return isAdminFolderGenerated & isBigDataFolderGenerated & isProjectFolderGenerated;
    }

    private boolean generateFoldersHelper(String number, String directory, String directoryTemplate, int type) {
        boolean success = false;

        File copyDestinationPath = new File(directory + File.separator + number);

        if (copyDestinationPath.exists()) {
            Main.statusBar.setStatus("", StatusBar.OK);

            String message = "";

            switch (type) {
                case TYPE_PROJECT:
                    message = String.format(I18N.getMsgCreateDirProjectExist(), number);
                    break;
                case TYPE_ADMIN:
                    message = String.format(I18N.getMsgCreateDirAdminExist(), number);
                    break;
                case TYPE_BIG_DATA:
                    message = String.format(I18N.getMsgCreateDirBigDataExist(), number);
                    break;

            }

            GuiHelper.showMessageBox(innerShell, SWT.ICON_WARNING, I18N.getMsgBoxTitleWarning(), message);
        } else {
            /* maybe later on with java 8 support in the office
            Path copySourcePath = Paths.get(directoryTemplate);

            Path copyDestinationPath = Paths.get(directory + File.separator + number);
            */

            File copySourcePath = new File(directoryTemplate);

            try {
                //Files.copy(copySourcePath, copyDestinationPath);

                FileUtils fileUtils = new FileUtils();
                fileUtils.copy(copySourcePath, copyDestinationPath);

                success = true;
            } catch (IOException e) {
                System.err.println(e.getMessage());

                String message = "";

                switch (type) {
                    case TYPE_PROJECT:
                        message = String.format(I18N.getMsgCreateDirProjectWarning(), number);
                        break;
                    case TYPE_ADMIN:
                        message = String.format(I18N.getMsgCreateDirAdminWarning(), number);
                        break;
                    case TYPE_BIG_DATA:
                        message = String.format(I18N.getMsgCreateDirBigDataWarning(), number);
                        break;

                }

                GuiHelper.showMessageBox(innerShell, SWT.ICON_ERROR, I18N.getMsgBoxTitleError(), message);
                success = false;
            }
        }
        return success;
    }

    private boolean generateAdminFolder(String number, int type) {
        String dir = Main.pref.getUserPref(PreferenceHandler.DIR_ADMIN);
        String dirTemplate = Main.pref.getUserPref(PreferenceHandler.DIR_ADMIN_TEMPLATE);

        return generateFoldersHelper(number, dir, dirTemplate, type);
    }

    private boolean generateBigDataFolder(String number, int type) {
        String dir = Main.pref.getUserPref(PreferenceHandler.DIR_BIG_DATA);
        String dirTemplate = Main.pref.getUserPref(PreferenceHandler.DIR_BIG_DATA_TEMPLATE);

        return generateFoldersHelper(number, dir, dirTemplate, type);
    }

    private boolean generateProjectFolder(String number, int type) {
        String dir = Main.pref.getUserPref(PreferenceHandler.DIR_PROJECT);
        String dirTemplate = Main.pref.getUserPref(PreferenceHandler.DIR_PROJECT_TEMPLATE);

        return generateFoldersHelper(number, dir, dirTemplate, type);
    }

}  // end of GeneratorWidget