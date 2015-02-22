/*
 * License: GPL. Copyright 2014- (C) by Sebastian Aust (http://www.ryanthara.de/)
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
import de.ryanthara.ja.rycon.gui.notifier.NotificationPopupWidget;
import de.ryanthara.ja.rycon.gui.notifier.NotificationType;
import de.ryanthara.ja.rycon.tools.ImageConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * This class is the main application of RyCON.
 * <p>
 * This class initializes the main window of RyCON and setup the
 * background functionality which is done by the extension of the
 * {@code Main} class.
 *
 * @author sebastian
 * @version 2
 * @since 1
 * @see de.ryanthara.ja.rycon.Main
 */
public class MainApplication extends Main {

    /**
     * Member for holding the first start status.
     */
    private boolean firstStart = true;

    /**
     * Class constructor without parameters.
     * <p>
     * The user interface is initialized in a separate method, which is called from here.
     */
    public MainApplication() {

        initUI();

    }

    /**
     * Main application startup
     *
     * @param args command line arguments (not used yet)
     */
    public static void main(String[] args) {

        checkJavaVersion();

        checkLicense();

        initApplicationPreferences();

        // to provide illegal thread access -> https://github.com/udoprog/c10t-swt/issues/1
        // add -XstartOnFirstThread as an java option on VM parameter
        new MainApplication();
//        new MainApplication_KOPIE();

    }

    /**
     * Does all the things after the application has started.
     * <p>
     * It is called from a listener after the shell is displayed.
     *
     */
    public void applicationStarted() {

        // TODO popup widget

        if (LICENSE) {
            NotificationPopupWidget.notify(I18N.getLicenseTitleFull(), I18N.getLicenseMsgFull(), NotificationType.values()[1], 4500);
        } else {
            NotificationPopupWidget.notify(I18N.getLicenseTitleDemo(), I18N.getLicenseMsgDemo(), NotificationType.values()[0], Integer.MAX_VALUE);
        }


    }

    /**
     * Does all the things when hitting button #1.
     */
    private void actionBtn1() {
        new TidyUpWidget();
        statusBar.setStatus(I18N.getStatus1CleanInitialized(), StatusBar.OK);
    }

    /**
     * Does all the things when hitting button #2.
     */
    private void actionBtn2() {
        new CodeSplitterWidget();
        statusBar.setStatus(I18N.getStatus2SplitterInitialized(), StatusBar.OK);
    }

    /**
     * Does all the things when hitting button #3.
     */
    private void actionBtn3() {
        new LevellingWidget();
        statusBar.setStatus(I18N.getStatus3LevelInitialized(), StatusBar.OK);
    }

    /**
     * Does all the things when hitting button #4.
     */
    private void actionBtn4() {
        new ConverterWidget();
        statusBar.setStatus(I18N.getStatus4ConverterInitialized(), StatusBar.OK);
    }

    /**
     * Does all the things when hitting button #5.
     */
    private void actionBtn5() {
        new GeneratorWidget();
        statusBar.setStatus(I18N.getStatus5GeneratorInitialized(), StatusBar.OK);
    }

    /**
     * Does all the things when hitting button #6.
     */
    private void actionBtn6() {
        statusBar.setStatus(I18N.getStatus6ExitInitialized(), StatusBar.OK);

        shell.getDisplay().dispose();

    }

    /**
     * Implements the user interface (UI) and all its components.
     * <p>
     * Drag'n drop is implemented on the buttons of the following modules.
     * <ul>
     *     <li>Clean files...</li>
     *     <li>Split files by code...</li>
     *     <li>Levelling to cad-import...</li>
     * </ul>*
     */
    private void initUI() {

        Display.setAppName(Main.getRyCONAppName());
        Display display = new Display();

        // initialize a shell and make it global
        Shell shell = new Shell(display, SWT.DIALOG_TRIM);
        Main.shell = shell;
        
        // Tray icon with functionality
        final Tray tray = display.getSystemTray();

        if (tray == null) {
            System.out.println("System tray functionality is not available on your system.");
        } else {
            final TrayItem item = new TrayItem(tray, SWT.NONE);
            item.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/RyCON_TrayIcon64x64.png"));
            item.setToolTipText("RyCON: " + Main.getRyCONBuild());
            
            final Menu menu = new Menu(shell, SWT.POP_UP);
            
            MenuItem webItem = new MenuItem(menu, SWT.PUSH);
            webItem.setText(I18N.getTrayMenuItemWebsite());
            webItem.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    Main.openURI(Main.RyCON_WEBSITE);
                }
            });
            
            MenuItem helpItem = new MenuItem(menu, SWT.PUSH);
            helpItem.setText(I18N.getTrayMenuItemHelp());
            helpItem.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    Main.openURI(Main.RyCON_WEBSITE_HELP);
                }
            });

            MenuItem exitItem = new MenuItem(menu, SWT.PUSH);
            exitItem.setText(I18N.getTrayMenuItemExit());
            exitItem.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    actionBtn6();   
                }
            });

            item.addListener (SWT.MenuDetect, new Listener () {
                public void handleEvent (Event event) {
                    menu.setVisible (true);
                }
            });

        }

        // Dock icon for OS X and Windows task bar
        shell.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/RyCON_blank256x256.png"));

        shell.setText(I18N.getApplicationTitle());

        FormLayout formLayout = new FormLayout();
        shell.setLayout(formLayout);

        // 3 x 2 grid for the buttons
        Composite compositeGrid = new Composite(shell, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.makeColumnsEqualWidth = true;

        compositeGrid.setLayout(gridLayout);

        // listen to keyboard inputs. There is no modifier key used!
        display.addFilter(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {

                if (!getSubShellStatus()) {

                    switch (event.keyCode) {
                        case '1':
                            actionBtn1();
                            break;
                        case '2':
                            actionBtn2();
                            break;
                        case '3':
                            actionBtn3();
                            break;
                        case '4':
                            actionBtn4();
                            break;
                        case '5':
                            actionBtn5();
                            break;
                        case '6':
                            actionBtn6();
                            break;
                        case 'p':
                            new SettingsWidget();
                            break;
                    }
                }

            }
        });

        // Drag and drop support for buttons - general 
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
        
        final FileTransfer fileTransfer = FileTransfer.getInstance();
        Transfer[] types = new Transfer[] { fileTransfer };
        
        // button #1 for cleaner tool
        Button btnToolboxClean = new Button(compositeGrid, SWT.PUSH);
        btnToolboxClean.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/1-clean.png"));
        btnToolboxClean.setText(I18N.getBtnCleanLabel());
        btnToolboxClean.setToolTipText(I18N.getBtnCleanLabelToolTip());

        //register listener for the selection event
        btnToolboxClean.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn1();
            }
        });
        
        // Drag and drop for clean files tool
        DropTarget targetClean = new DropTarget(btnToolboxClean, operations);
        targetClean.setTransfer(types);

        targetClean.addDropListener(new DropTargetAdapter() {

            public void dragEnter(DropTargetEvent event) {

                System.out.println("drag enter");

                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                // will accept text but prefer to have files dropped
                for (int i = 0; i < event.dataTypes.length; i++) {
                    if (fileTransfer.isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                        // files should only be copied
                        if (event.detail != DND.DROP_COPY) {
                            event.detail = DND.DROP_NONE;
                        }
                        break;
                    }
                }

            }

            public void drop(DropTargetEvent event) {

                System.out.println("drop");

                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    String[] files = (String[]) event.data;
                    statusBar.setStatus(files[0], 0);
                }

            }
        });

        // button #2 for splitter tool
        Button btnToolboxSplitter = new Button(compositeGrid, SWT.PUSH);
        btnToolboxSplitter.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/2-code.png"));
        btnToolboxSplitter.setText(I18N.getBtnSplitterLabel());
        btnToolboxSplitter.setToolTipText(I18N.getBtnSplitterLabelToolTip());

        //register listener for the selection event
        btnToolboxSplitter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn2();
            }
        });

        // Drag and drop for splitter tool
        DropTarget targetSplitter = new DropTarget(btnToolboxSplitter, operations);
        targetSplitter.setTransfer(types);

        targetSplitter.addDropListener(new DropTargetAdapter() {

            public void dragEnter(DropTargetEvent event) {

                System.out.println("drag enter 2");

                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                // will accept text but prefer to have files dropped
                for (int i = 0; i < event.dataTypes.length; i++) {
                    if (fileTransfer.isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                        // files should only be copied
                        if (event.detail != DND.DROP_COPY) {
                            event.detail = DND.DROP_NONE;
                        }
                        break;
                    }
                }

            }

            public void drop(DropTargetEvent event) {

                System.out.println("drop 2");

                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    String[] files = (String[]) event.data;
                    statusBar.setStatus(files[0], 0);
                }

            }
        });


        // button #3 for leveling tool
        Button btnToolboxLeveling = new Button(compositeGrid, SWT.PUSH);
        btnToolboxLeveling.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/3-level.png"));
        btnToolboxLeveling.setText(I18N.getBtnLevelingLabel());
        btnToolboxLeveling.setToolTipText(I18N.getBtnLevelingLabelToolTip());

        //register listener for the selection event
        btnToolboxLeveling.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn3();
            }
        });

        // Drag and drop for levelling tool
        DropTarget targetLevelling = new DropTarget(btnToolboxLeveling, operations);
        targetLevelling.setTransfer(types);

        targetLevelling.addDropListener(new DropTargetAdapter() {

            public void dragEnter(DropTargetEvent event) {

                System.out.println("drag enter 3");

                if (event.detail == DND.DROP_DEFAULT) {
                    if ((event.operations & DND.DROP_COPY) != 0) {
                        event.detail = DND.DROP_COPY;
                    } else {
                        event.detail = DND.DROP_NONE;
                    }
                }
                // will accept text but prefer to have files dropped
                for (int i = 0; i < event.dataTypes.length; i++) {
                    if (fileTransfer.isSupportedType(event.dataTypes[i])) {
                        event.currentDataType = event.dataTypes[i];
                        // files should only be copied
                        if (event.detail != DND.DROP_COPY) {
                            event.detail = DND.DROP_NONE;
                        }
                        break;
                    }
                }

            }

            public void drop(DropTargetEvent event) {

                System.out.println("drop 3");

                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    String[] files = (String[]) event.data;
                    statusBar.setStatus(files[0], 0);
                }

            }
        });


        // button #4 for converter tool
        Button btnToolboxConvert = new Button(compositeGrid, SWT.PUSH);
        btnToolboxConvert.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/4-convert.png"));
        btnToolboxConvert.setText(I18N.getBtnConvertLabel());
        btnToolboxConvert.setToolTipText(I18N.getBtnConvertLabelToolTip());

        //register listener for the selection event
        btnToolboxConvert.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn4();
            }
        });

        // button #5 for project generation tool
        Button btnToolboxGenerator = new Button(compositeGrid, SWT.PUSH);
        btnToolboxGenerator.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/5-project.png"));
        btnToolboxGenerator.setText(I18N.getBtnGeneratorLabel());
        btnToolboxGenerator.setToolTipText(I18N.getBtnGeneratorLabelToolTip());

        //register listener for the selection event
        btnToolboxGenerator.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn5();
            }
        });

        // button #6 for exit from the program
        Button btnExit = new Button(compositeGrid, SWT.PUSH);
        btnExit.setImage(new ImageConverter().convertToImage(display, "/de/ryanthara/ja/rycon/gui/icons/6-exit.png"));
        btnExit.setText(I18N.getBtnExitLabel());
        btnExit.setToolTipText(I18N.getBtnExitLabelToolTip());

        //register listener for the selection event
        btnExit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                actionBtn6();
            }
        });

        // defines the size of the buttons
        GridData gridData = new GridData();
        gridData.widthHint = getRyCON_GRID_WIDTH();
        gridData.heightHint = getRyCON_GRID_HEIGHT();

        btnToolboxClean.setLayoutData(gridData);
        btnToolboxConvert.setLayoutData(gridData);
        btnToolboxGenerator.setLayoutData(gridData);
        btnToolboxLeveling.setLayoutData(gridData);
        btnToolboxSplitter.setLayoutData(gridData);
        btnExit.setLayoutData(gridData);


        // status bar
        StatusBar statusBar = new StatusBar(shell, SWT.NONE);
        statusBar.setStatus(I18N.getStatusRyCONInitialized(), StatusBar.OK);
        Main.statusBar = statusBar;


        FormData formDataStatus = new FormData();
        formDataStatus.width = 3 * getRyCON_GRID_WIDTH() + 2; // width of the status bar!
        formDataStatus.bottom = new FormAttachment(100, -8);
        formDataStatus.left = new FormAttachment(0, 8);

        statusBar.setLayoutData(formDataStatus);

        // show information on status bar e.g. when a new config file was generated
        if (pref.isDefaultSettingsGenerated()) {
            statusBar.setStatus(I18N.getMsgNewConfigFileGenerated(), StatusBar.WARNING);
        }

        shell.pack();

//        SplashScreen splashScreen = new SplashScreen(display);

        // size depends on the grid size
        shell.setSize(3 * getRyCON_GRID_WIDTH() + 20, 2 * getRyCON_GRID_HEIGHT() + 100);

        // center the shell on the primary monitor
        ShellCenter shellCenter = new ShellCenter(shell);
        shell.setLocation(shellCenter.centeredShellLocation());

        shell.addShellListener(new ShellAdapter() {
            /**
             * Sent when a shell becomes the active window.
             * The default behavior is to do nothing.
             *
             * @param e an event containing information about the activation
             */
            @Override
            public void shellActivated(ShellEvent e) {
                super.shellActivated(e);

                // do a couple of things only when RyCON is started
                if (firstStart) {
                    applicationStarted();
                    firstStart = false;
                }
            }
        });

        shell.open();

        // show settings widget when necessary
        if (pref.isDefaultSettingsGenerated()) {
            new SettingsWidget();
        }

        // run the event loop as long as the window is open
        while (!shell.isDisposed()) {

            // read the next OS event queue and transfer it to a SWT event
            if (!display.readAndDispatch()) {

                // if there are currently no other OS event to process
                // sleep until the next OS event is available
                display.sleep();
            }
        }

        // disposes all associated windows and their components
        display.dispose();

    }

} // end of MainApplication
