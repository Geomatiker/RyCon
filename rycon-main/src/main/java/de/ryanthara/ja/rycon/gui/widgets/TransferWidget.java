/*
 * License: GPL. Copyright 2017- (C) by Sebastian Aust (https://www.ryanthara.de/)
 *
 * This file is part of the package de.ryanthara.ja.rycon.gui.widget
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
package de.ryanthara.ja.rycon.gui.widgets;

import de.ryanthara.ja.rycon.Main;
import de.ryanthara.ja.rycon.check.PathCheck;
import de.ryanthara.ja.rycon.check.TextCheck;
import de.ryanthara.ja.rycon.data.PreferenceKeys;
import de.ryanthara.ja.rycon.file.filter.FileFileFilter;
import de.ryanthara.ja.rycon.file.filter.GSIFileFilter;
import de.ryanthara.ja.rycon.file.filter.TXTFileFilter;
import de.ryanthara.ja.rycon.gui.Sizes;
import de.ryanthara.ja.rycon.gui.custom.BottomButtonBar;
import de.ryanthara.ja.rycon.gui.custom.DirectoryDialogs;
import de.ryanthara.ja.rycon.gui.custom.MessageBoxes;
import de.ryanthara.ja.rycon.i18n.*;
import de.ryanthara.ja.rycon.io.FileUtils;
import de.ryanthara.ja.rycon.tools.ShellPositioner;
import de.ryanthara.ja.rycon.tools.StringUtils;
import de.ryanthara.ja.rycon.util.BoundedTreeSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.ryanthara.ja.rycon.gui.custom.Status.OK;
import static de.ryanthara.ja.rycon.i18n.ResourceBundles.*;

// TODO: 12.08.17  implement clean and elegant workflow and error handling path
// - no card available
// - insert empty card or one with wrong folder structure
// - read path and update lists
// - add project number with or without path
// - copy files
// - moveFile files

/**
 * Instances of this class implements a complete widgets and it's functionality.
 * <p>
 * With the TransferWidget of RyCON it is possible to transfer different files from a card reader or folder mounted
 * card reader into a given project structure in the file system. The source structure and the target structure can be
 * configured flexible.
 *
 * @author sebastian
 * @version 8
 * @since 1
 */
public class TransferWidget extends AbstractWidget {

    private final static Logger logger = Logger.getLogger(TransferWidget.class.getName());
    private ArrayList<File> allJobsFiles;
    private Button chkBoxMoveOption;
    private Shell innerShell;
    private Text cardReaderPath;
    private Text targetProjectPath;
    private TreeSet<File> allDatas;
    private TreeSet<File> allExports;
    private List dataList;
    private List exportList;
    private List jobList;
    private List lastUsedProjectsList;
    private BoundedTreeSet<String> lastUsedProjects;

    /**
     * Constructs the {@link TransferWidget} without parameters.
     * <p>
     * The user interface is initialized in a separate method, which is called from here.
     */
    public TransferWidget() {
        initUI();
    }

    void actionBtnCancel() {
        Main.setSubShellStatus(false);
        Main.statusBar.setStatus("", OK);

        innerShell.dispose();
    }

    private void actionBtnCardReaderPath() {
        String filterPath = Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER);

        // Set the initial filter path according to anything selected or typed in
        if (!TextCheck.isEmpty(cardReaderPath)) {
            if (TextCheck.isDirExists(cardReaderPath)) {
                filterPath = cardReaderPath.getText();
            }
        }

        DirectoryDialogs.showAdvancedDirectoryDialog(innerShell, cardReaderPath,
                ResourceBundleUtils.getLangString(FILECHOOSERS, FileChoosers.cardReaderTitle),
                ResourceBundleUtils.getLangString(FILECHOOSERS, FileChoosers.cardReaderMessage), filterPath);

        if (!TextCheck.isEmpty(cardReaderPath)) {
            if (TextCheck.isDirExists(cardReaderPath)) {
                Main.pref.setUserPreference(PreferenceKeys.DIR_CARD_READER, cardReaderPath.getText());
            }
        }
    }

    private void actionBtnChooseProjectPath() {
        String filterPath = Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT);

        // Set the initial filter path according to anything selected or typed in
        if (!TextCheck.isEmpty(targetProjectPath)) {
            if (TextCheck.isDirExists(targetProjectPath)) {
                filterPath = targetProjectPath.getText();
            }
        }

        DirectoryDialogs.showAdvancedDirectoryDialog(innerShell, targetProjectPath,
                ResourceBundleUtils.getLangString(FILECHOOSERS, FileChoosers.dirProjectTitle),
                ResourceBundleUtils.getLangString(FILECHOOSERS, FileChoosers.dirProjectMessage), filterPath);
    }

    boolean actionBtnOk() {
        boolean success;

        // one or more items selected in a list
        final int number = dataList.getSelectionCount() + exportList.getSelectionCount() + jobList.getSelectionCount();

        if (number > 0) {
            if (checkIsTargetPathValid()) {
                success = copyMoveAction();

                if (success) {
                    updateLastUsedProjectsListAndPreferences();

                    Main.statusBar.setStatus(ResourceBundleUtils.getLangString(MESSAGES, Messages.cardReaderFilesCopySuccessful), OK);
                }

                String helper, message;
                if (chkBoxMoveOption.getSelection()) {
                    helper = ResourceBundleUtils.getLangString(MESSAGES, Messages.transferMoveMessage);
                } else {
                    helper = ResourceBundleUtils.getLangString(MESSAGES, Messages.transferCopyMessage);
                }

                if (number == 1) {
                    message = String.format(StringUtils.singularPluralMessage(helper, Main.TEXT_SINGULAR), number);
                } else {
                    message = String.format(StringUtils.singularPluralMessage(helper, Main.TEXT_PLURAL), number);
                }

                MessageBoxes.showMessageBox(Main.shell, SWT.ICON_INFORMATION,
                        ResourceBundleUtils.getLangString(MESSAGES, Messages.transferText), message);

                return success;
            } else {
                actionBtnChooseProjectPath();
            }
        } else {
            MessageBoxes.showMessageBox(Main.shell, SWT.ICON_INFORMATION,
                    ResourceBundleUtils.getLangString(ERRORS, Errors.transferNoDataSelectedText),
                    ResourceBundleUtils.getLangString(ERRORS, Errors.transferNoDataSelected));
        }

        return false;
    }

    /*
     * This method is used from the class BottomButtonBar!
     */
    void actionBtnOkAndExit() {
        if (actionBtnOk()) {
            Main.setSubShellStatus(false);
            Main.statusBar.setStatus("", OK);

            innerShell.dispose();
        }
    }

    private boolean checkIsTargetPathValid() {
        // last used project list contains items and one of them is selected
        if ((lastUsedProjectsList.getItemCount() > 0) & (lastUsedProjectsList.getSelectionCount() > 0)) {
            final String projectPath = Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT);
            final String lastUsedProject = projectPath + File.separator + lastUsedProjectsList.getSelection()[0];
            targetProjectPath.setText(lastUsedProject);

            return true;
        } else {
            // list is empty or no element selected -> use text field
            if (!TextCheck.isEmpty(targetProjectPath)) {
                if (TextCheck.isDirExists(targetProjectPath)) {
                    return true;
                } else {
                    // checks if user entered only a valid project number without path
                    final String s = Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT) + File.separator + targetProjectPath.getText();

                    if (PathCheck.isDirectory(s)) {
                        targetProjectPath.setText(s);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean copyMoveAction() {
        final boolean overWriteExisting = Boolean.parseBoolean(Main.pref.getUserPreference(PreferenceKeys.OVERWRITE_EXISTING));

        return copyMoveExportFiles(overWriteExisting) | copyMoveJobFiles(overWriteExisting) | copyMoveDataFiles(overWriteExisting);
    }

    /*
     * The file 'logfile.txt' can not be chosen and is copied automatically if it is present.
     */
    private boolean copyMoveDataFiles(boolean overWriteExisting) {
        final String dir = Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_DATA_FILES);

        final String[] selectedDataFiles = dataList.getSelection();

        if (PathCheck.isDirectory(dir)) {
            // no file is selected -> copy 'logfile.txt'
            if ((selectedDataFiles.length == 0)) {
                final String logfile = dir + File.separator + "logfile.txt";

                if (PathCheck.isFile(logfile)) {
                    final LocalDate localDate = LocalDate.now();
                    final Path source = Paths.get(logfile);
                    final String dest = targetProjectPath.getText() + File.separator + Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT_LOG_FILES);
                    final Path target = Paths.get(dest + File.separator + localDate.toString() + "_logfile.txt");

                    try {
                        if (chkBoxMoveOption.getSelection()) {
                            FileUtils.move(source, target, overWriteExisting);
                        } else {
                            FileUtils.copy(source, target, overWriteExisting);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "error while copying 'logfile.txt' from " + source.getFileName() +
                                " to " + target.getFileName(), e);
                    }

                    return true;
                }
            } else {
                boolean success = false;

                for (String dataFileName : selectedDataFiles) {
                    success = false;

                    for (File file : allDatas) {
                        if (file.getName().startsWith(dataFileName)) {
                            if (PathCheck.isFile(file)) {
                                final Path source = Paths.get(file.getPath());

                                // insert current date into logfile name
                                if (file.getName().endsWith("logfile.txt")) {
                                    final LocalDate localDate = LocalDate.now();
                                    final String logfileName = file.getPath();
                                    String newFileName = logfileName.replaceAll("logfile.txt", localDate.toString() + "_logfile.txt");
                                    file = new File(newFileName);
                                }

                                final String dest = targetProjectPath.getText() + File.separator + Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT_LOG_FILES);
                                final Path target = Paths.get(dest + File.separator + file.getName());

                                try {
                                    if (chkBoxMoveOption.getSelection()) {
                                        FileUtils.move(source, target, overWriteExisting);
                                    } else {
                                        FileUtils.copy(source, target, overWriteExisting);
                                    }
                                    success = true;
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "error while copying data files from " + source.getFileName() +
                                            " to " + target.getFileName(), e);
                                }
                            }
                        }
                    }

                }

                return success;
            }
        }

        return false;
    }

    /*
     * mostly the same code as copyMoveJobFiles
     */
    private boolean copyMoveExportFiles(boolean overWriteExisting) {
        final String dir = Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_EXPORT_FILES);

        final String[] selectedExports = exportList.getSelection();

        if (PathCheck.isDirectory(dir)) {
            boolean success = false;

            for (String exportedJobName : selectedExports) {
                success = false;

                for (File file : allExports) {
                    if (file.getName().startsWith(exportedJobName)) {
                        if (PathCheck.isFile(file)) {
                            final Path source = Paths.get(file.getPath());
                            final String dest = targetProjectPath.getText() + File.separator + Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT_MEASUREMENT_FILES);
                            final Path target = Paths.get(dest + File.separator + file.getName());

                            try {
                                if (chkBoxMoveOption.getSelection()) {
                                    FileUtils.move(source, target, overWriteExisting);
                                } else {
                                    FileUtils.copy(source, target, overWriteExisting);
                                }
                                success = true;
                            } catch (IOException e) {
                                logger.log(Level.SEVERE, "error while copying export file from " + source.getFileName() +
                                        " to " + target.getFileName(), e);
                            }
                        }
                    }
                }
            }

            return success;

        }

        return false;
    }

    /*
     * mostly the same code as copyMoveExportFiles
     */
    private boolean copyMoveJobFiles(boolean overWriteExisting) {
        final String dir = Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_JOB_FILES);

        final String[] selectedJobs = jobList.getSelection();

        if (PathCheck.isDirectory(dir)) {
            boolean success = false;

            for (String jobName : selectedJobs) {
                success = false;

                for (File file : allJobsFiles) {
                    if (file.getName().startsWith(jobName)) {
                        if (PathCheck.isFile(file)) {
                            final Path source = Paths.get(file.getPath());
                            final String dest = targetProjectPath.getText() + File.separator + Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT_JOB_FILES);
                            final Path target = Paths.get(dest + File.separator + file.getName());

                            try {
                                if (chkBoxMoveOption.getSelection()) {
                                    FileUtils.move(source, target, overWriteExisting);
                                } else {
                                    FileUtils.copy(source, target, overWriteExisting);
                                }
                                success = true;
                            } catch (IOException e) {
                                logger.log(Level.SEVERE, "error while copying job file from " + source.getFileName() +
                                        " to " + target.getFileName(), e);
                            }
                        }
                    }
                }
            }

            return success;

        }

        return false;
    }

    private void createGroupCardReader() {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(ResourceBundleUtils.getLangString(LABELS, Labels.transferCardReaderText));

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        gridLayout.numColumns = 3;

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = Sizes.RyCON_WIDGET_WIDTH.getValue();

        group.setLayout(gridLayout);
        group.setLayoutData(gridData);

        Label cardReaderLabel = new Label(group, SWT.NONE);
        cardReaderLabel.setText(ResourceBundleUtils.getLangString(LABELS, Labels.cardReaderPath));

        cardReaderPath = new Text(group, SWT.SINGLE | SWT.BORDER);
        cardReaderPath.setText(Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER));

        // platform independent key handling for ENTER, TAB, ...
        // TODO change bad listener with a better one
        cardReaderPath.addListener(SWT.Traverse, event -> {
            if (!cardReaderPath.getText().trim().equals("")) {
                if ((event.stateMask & SWT.SHIFT) == SWT.SHIFT && event.detail == SWT.TRAVERSE_RETURN) {
                    actionBtnOkAndExit();
                } else if (event.detail == SWT.TRAVERSE_RETURN) {
                    actionBtnOk();
                }
            }

            if (event.detail == SWT.TRAVERSE_TAB_NEXT || event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                event.doit = true;
            }
        });

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        cardReaderPath.setLayoutData(gridData);

        Button btnCardReaderPath = new Button(group, SWT.NONE);
        btnCardReaderPath.setText(ResourceBundleUtils.getLangString(BUTTONS, Buttons.choosePathText));
        btnCardReaderPath.setToolTipText(ResourceBundleUtils.getLangString(BUTTONS, Buttons.choosePathText));
        btnCardReaderPath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnCardReaderPath();
            }
        });

        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnCardReaderPath.setLayoutData(gridData);

        Control[] tabulatorKeyOrder = new Control[]{
                cardReaderPath, btnCardReaderPath
        };

        group.setTabList(tabulatorKeyOrder);
    }

    private void createGroupChooseData(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(ResourceBundleUtils.getLangString(LABELS, Labels.jobProjectText));

        GridLayout gridLayout = new GridLayout(4, true);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;

        group.setLayout(gridLayout);
        group.setLayoutData(gridData);

        final int groupHeight = 145;
        final int groupWidth = gridData.widthHint / 4 - 46;

        // DBX folder
        Group jobGroup = new Group(group, SWT.NONE);
        jobGroup.setText(ResourceBundleUtils.getLangString(LABELS, Labels.jobGroupText));

        gridLayout = new GridLayout(1, true);
        jobGroup.setLayout(gridLayout);

        jobList = new List(jobGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

        gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.heightHint = groupHeight;
        gridData.widthHint = groupWidth;

        jobList.setLayoutData(gridData);

        // export folder, here GSI folder
        Group exportGroup = new Group(group, SWT.NONE);
        exportGroup.setText(ResourceBundleUtils.getLangString(LABELS, Labels.exportGroupText));

        gridLayout = new GridLayout(1, true);
        exportGroup.setLayout(gridLayout);

        exportList = new List(exportGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

        gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.heightHint = groupHeight;
        gridData.widthHint = groupWidth;

        exportList.setLayoutData(gridData);

        // data folder
        Group dataGroup = new Group(group, SWT.NONE);
        dataGroup.setText(ResourceBundleUtils.getLangString(LABELS, Labels.dataGroupText));

        gridLayout = new GridLayout(1, true);
        dataGroup.setLayout(gridLayout);

        dataList = new List(dataGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

        gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.heightHint = groupHeight;
        gridData.widthHint = groupWidth;

        dataList.setLayoutData(gridData);

        // last used projects
        Group projectGroup = new Group(group, SWT.NONE);
        projectGroup.setText(ResourceBundleUtils.getLangString(LABELS, Labels.lastUsedProjects));

        gridLayout = new GridLayout(1, true);
        projectGroup.setLayout(gridLayout);

        lastUsedProjectsList = new List(projectGroup, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        lastUsedProjectsList.addListener(SWT.Selection, event -> {
            final String project = Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT)
                    + File.separator + lastUsedProjectsList.getSelection()[0];

            targetProjectPath.setText(project);
        });

        loadProjectListFromPreferences();

        gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.heightHint = groupHeight;
        gridData.widthHint = groupWidth;

        lastUsedProjectsList.setLayoutData(gridData);
    }

    private void createGroupChooseTarget() {
        Group group = new Group(innerShell, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        gridLayout.numColumns = 3;

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = Sizes.RyCON_WIDGET_WIDTH.getValue();

        group.setLayout(gridLayout);
        group.setLayoutData(gridData);

        Label targetProjectLabel = new Label(group, SWT.NONE);
        targetProjectLabel.setText(ResourceBundleUtils.getLangString(LABELS, Labels.targetProject));

        targetProjectPath = new Text(group, SWT.SINGLE | SWT.BORDER);

        final String baseInput = Main.pref.getUserPreference(PreferenceKeys.DIR_PROJECT) + File.separator;

        if (lastUsedProjectsList.getItemCount() == 0) {
            targetProjectPath.setText(baseInput);
        } else {
            targetProjectPath.setText(baseInput + lastUsedProjectsList.getItem(0));
            // lastUsedProjectsList.select(0);
        }

        targetProjectPath.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseDown(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseUp(MouseEvent mouseEvent) {
                Text text = (Text) mouseEvent.widget;
                String s = text.getText();

                // select a part of the text for project number input
                text.setSelection(s.lastIndexOf(File.separator) + 1, s.length());
            }
        });

        // platform independent key handling for ENTER, TAB, ...
        // TODO change bad listener with a better one
        targetProjectPath.addListener(SWT.Traverse, event -> {
            if (!targetProjectPath.getText().trim().equals("")) {
                if ((event.stateMask & SWT.SHIFT) == SWT.SHIFT && event.detail == SWT.TRAVERSE_RETURN) {
                    actionBtnOkAndExit();
                } else if (event.detail == SWT.TRAVERSE_RETURN) {
                    actionBtnOk();
                }
            }

            if (event.detail == SWT.TRAVERSE_TAB_NEXT || event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                event.doit = true;
            }
        });

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        targetProjectPath.setLayoutData(gridData);

        Button btnChooseProjectPath = new Button(group, SWT.NONE);
        btnChooseProjectPath.setText(ResourceBundleUtils.getLangString(BUTTONS, Buttons.choosePathText));
        btnChooseProjectPath.setToolTipText(ResourceBundleUtils.getLangString(BUTTONS, Buttons.choosePathText));
        btnChooseProjectPath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtnChooseProjectPath();
            }
        });

        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnChooseProjectPath.setLayoutData(gridData);

        Control[] tabulatorKeyOrder = new Control[]{
                targetProjectPath, btnChooseProjectPath
        };

        group.setTabList(tabulatorKeyOrder);
    }

    private void createGroupDescription(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(ResourceBundleUtils.getLangString(LABELS, Labels.adviceText));

        GridLayout gridLayout = new GridLayout(1, true);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;

        group.setLayout(gridLayout);
        group.setLayoutData(gridData);

        Label tip = new Label(group, SWT.WRAP | SWT.BORDER | SWT.LEFT);
        tip.setText(ResourceBundleUtils.getLangString(LABELS, Labels.tipTransferWidget));
        tip.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1));
    }

    private void createGroupOptions(int width) {
        Group group = new Group(innerShell, SWT.NONE);
        group.setText(ResourceBundleUtils.getLangString(LABELS, Labels.optionsText));

        GridLayout gridLayout = new GridLayout(1, true);

        GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, true);
        gridData.widthHint = width - 24;

        group.setLayout(gridLayout);
        group.setLayoutData(gridData);

        chkBoxMoveOption = new Button(group, SWT.CHECK);
        chkBoxMoveOption.setSelection(false);
        chkBoxMoveOption.setText(ResourceBundleUtils.getLangString(CHECKBOXES, CheckBoxes.moveTransferWidget));
    }

    void initUI() {
        final int height = Sizes.RyCON_WIDGET_HEIGHT.getValue();
        final int width = Sizes.RyCON_WIDGET_WIDTH.getValue() + 205;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        gridData.heightHint = height;
        gridData.widthHint = width;

        innerShell = new Shell(Main.shell, SWT.CLOSE | SWT.DIALOG_TRIM | SWT.MAX | SWT.TITLE | SWT.APPLICATION_MODAL);
        innerShell.addListener(SWT.Close, event -> actionBtnCancel());
        innerShell.setText(ResourceBundleUtils.getLangString(LABELS, Labels.generatorText));
        innerShell.setSize(width, height);

        innerShell.setLayout(gridLayout);
        innerShell.setLayoutData(gridData);

        createGroupCardReader();
        createGroupChooseData(width);
        createGroupChooseTarget();
        createGroupOptions(width);
        createGroupDescription(width);

        new BottomButtonBar(this, innerShell, BottomButtonBar.OK_AND_EXIT_BUTTON);

        innerShell.setLocation(ShellPositioner.centerShellOnPrimaryMonitor(innerShell));

        Main.setSubShellStatus(true);

        innerShell.pack();
        innerShell.open();

        // TODO how to activate the card reading??
        if (PathCheck.isDirectory(Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER))) {
            readCardFolders();

            logger.log(Level.INFO, "card reader path successful read");
        } else {
            logger.log(Level.SEVERE, "could not read card reader path");
        }
    }

    private void loadProjectListFromPreferences() {
        lastUsedProjects = new BoundedTreeSet<>(10);

        final String s = Main.pref.getUserPreference(PreferenceKeys.LAST_USED_PROJECTS);
        final String t = s.substring(1, s.length() - 1);

        final String[] strings = t.split(", ");

        for (String projectNumber : strings) {
            if (!projectNumber.trim().equalsIgnoreCase("")) {
                lastUsedProjectsList.add(projectNumber);
            }
        }
    }

    private void readCardFolderData() {
        final File dataFilesDir = new File(Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_DATA_FILES));

        allDatas = new TreeSet<>();

        File[] dataFileDirContent = dataFilesDir.listFiles(new TXTFileFilter());

        if (dataFileDirContent != null) {
            for (File file : dataFileDirContent) {
                if (file.exists() & file.isFile()) {
                    allDatas.add(file);
                }
            }

            if (allDatas.size() > 0) {
                for (File jobFile : allDatas) {
                    dataList.add(jobFile.getName());
                }
            }
        }
    }

    private void readCardFolderExport() {
        final File exportFilesDir = new File(Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_EXPORT_FILES));

        allExports = new TreeSet<>();

        File[] exportFileDirContentGSI = exportFilesDir.listFiles(new GSIFileFilter());

        if (exportFileDirContentGSI != null) {
            for (File file : exportFileDirContentGSI) {
                if (file.exists() & file.isFile()) {
                    allExports.add(file);
                }
            }
        }

        File[] exportFileDirContentTXT = exportFilesDir.listFiles(new TXTFileFilter());

        if (exportFileDirContentTXT != null) {
            for (File file : exportFileDirContentTXT) {
                if (file.exists() & file.isFile()) {
                    allExports.add(file);
                }
            }
        }

        if (allExports.size() > 0) {
            for (File exportFile : allExports) {
                exportList.add(exportFile.getName());
            }
        }
    }

    private void readCardFolderJob() {
        final File jobFilesDir = new File(Main.pref.getUserPreference(PreferenceKeys.DIR_CARD_READER_JOB_FILES));

        TreeSet<String> allJobs = new TreeSet<>();

        allJobsFiles = new ArrayList<>();

        File[] jobFileDirContent = jobFilesDir.listFiles(new FileFileFilter());

        if (jobFileDirContent != null) {
            for (File file : jobFileDirContent) {
                PathCheck.isFile(file.toString());

                if (file.exists() & file.isFile()) {
                    int length = file.getName().length();
                    allJobs.add(file.getName().substring(0, length - 21));
                    allJobsFiles.add(file);
                }
            }

            if (allJobs.size() > 0) {
                for (String job : allJobs) {
                    jobList.add(job);
                }
            }
        }
    }

    private void readCardFolders() {
        readCardFolderData();
        readCardFolderExport();
        readCardFolderJob();
    }

    /*
     * Update last used project list after copying the selected files. The values are stored in
     * the user preferences with the key 'LAST_USED_PROJECTS'.
     */
    private void updateLastUsedProjectsListAndPreferences() {
        // get last used project elements from list
        lastUsedProjects.addAll(Arrays.asList(lastUsedProjectsList.getItems()));

        // get last used projects from text field and add to bounded tree set
        final String text = targetProjectPath.getText();
        final String lastUsedProject = text.substring(text.lastIndexOf(File.separator) + 1, text.length());

        lastUsedProjects.add(lastUsedProject);

        // update list
        Object[] projectsHelper = lastUsedProjects.toArray();

        lastUsedProjectsList.removeAll();

        for (Object object : projectsHelper) {
            lastUsedProjectsList.add((String) object);
        }

        // store last used projects to user preferences
        Main.pref.setUserPreference(PreferenceKeys.LAST_USED_PROJECTS, Arrays.toString(lastUsedProjectsList.getItems()));
    }

} // end of TransferWidget
