/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main;

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntryFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.CloseWindowAction;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.ProportionalSplitPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.menu.MainMenuBar;
import com.mucommander.ui.main.table.Columns;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableConfiguration;
import com.mucommander.ui.main.table.SortInfo;
import com.mucommander.ui.quicklist.QuickListFocusableComponent;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame extends JFrame implements LocationListener {
	
    private ProportionalSplitPane splitPane;

    private FolderPanel leftFolderPanel;
    private FolderPanel rightFolderPanel;
	
    private FileTable leftTable;
    private FileTable rightTable;
    
    /** Active table in the MainFrame */
    private FileTable activeTable;

    /** Tool bar instance */
    private ToolBar toolbar;

    /** Status bar instance */
    private StatusBar statusBar;
	
    /** Command bar instance */
    private CommandBar commandBar;
	
    /** Is no events mode enabled ? */
    private boolean noEventsMode;

    /** Is this MainFrame active in the foreground ? */
    private boolean foregroundActive;
    
    /** quick search mode */
    private boolean quickSearchActive = true;

    /** Contains all registered ActivePanelListener instances, stored as weak references */
    private WeakHashMap activePanelListeners = new WeakHashMap();

    /** Split pane orientation */
    private final static String SPLIT_ORIENTATION = MuConfiguration.SPLIT_ORIENTATION;

    private void init(FolderPanel leftFolderPanel, FolderPanel rightFolderPanel) {
        // Set frame icon fetched in an image inside the JAR file
        setIconImage(IconManager.getIcon("/icon16.gif").getImage());

        // Enable window resize
        setResizable(true);

        // Sets the content pane.
        JPanel contentPane = new JPanel(new BorderLayout()) {
                // Add an x=3,y=3 gap around content pane
                public Insets getInsets() {
                    return new Insets(3, 3, 3, 3);
                }
            };
        setContentPane(contentPane);

        // Initialises the folder panels and file tables.
        this.leftFolderPanel = leftFolderPanel;
        this.rightFolderPanel = rightFolderPanel;
        leftTable = leftFolderPanel.getFileTable();
        rightTable = rightFolderPanel.getFileTable();
        activeTable  = leftTable;

        // Create toolbar and show it only if it hasn't been disabled in the preferences
        // Note: Toolbar.setVisible() has to be called no matter if Toolbar is visible or not, in order for it to be properly initialized
        this.toolbar = new ToolBar(this);
        this.toolbar.setVisible(MuConfiguration.getVariable(MuConfiguration.TOOLBAR_VISIBLE, MuConfiguration.DEFAULT_TOOLBAR_VISIBLE));
        contentPane.add(toolbar, BorderLayout.NORTH);

        // Lister to location change events to display the current folder in the window's title
        leftFolderPanel.getLocationManager().addLocationListener(this);
        rightFolderPanel.getLocationManager().addLocationListener(this);

        // Create menu bar (has to be created after toolbar)
        MainMenuBar menuBar = new MainMenuBar(this);
        setJMenuBar(menuBar);

        // Create the split pane that separates folder panels and allows to resize how much space is allocated to the
        // both of them. The split orientation is loaded from and saved to the preferences.
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing.
        splitPane = new ProportionalSplitPane(this,
            MuConfiguration.getVariable(SPLIT_ORIENTATION, MuConfiguration.DEFAULT_SPLIT_ORIENTATION).equals(MuConfiguration.VERTICAL_SPLIT_ORIENTATION) ?
                                              JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT,
                                              false,
                MainFrame.this.leftFolderPanel,
                MainFrame.this.rightFolderPanel) {
                // We don't want any extra space around split pane
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };

        // Remove any default border the split pane has
        splitPane.setBorder(null);

        // Adds buttons that allow to collapse and expand the split pane in both directions
        splitPane.setOneTouchExpandable(true);

        // Disable all the JSPlitPane accessibility shortcuts that are registered by default, as some of them
        // conflict with default mucommander action shortcuts (e.g. F6 and F8) 
        splitPane.disableAccessibilityShortcuts();

        // Split pane will be given any extra space
        contentPane.add(splitPane, BorderLayout.CENTER);

        // Add a 2-pixel gap between the file table and status bar
        YBoxPanel southPanel = new YBoxPanel();
        southPanel.addSpace(2);

        // Add status bar
        this.statusBar = new StatusBar(this);
        southPanel.add(statusBar);
		
        // Show command bar only if it hasn't been disabled in the preferences
        this.commandBar = new CommandBar(this);
        // Note: CommandBar.setVisible() has to be called no matter if CommandBar is visible or not, in order for it to be properly initialized
        this.commandBar.setVisible(MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_VISIBLE, MuConfiguration.DEFAULT_COMMAND_BAR_VISIBLE));
        southPanel.add(commandBar);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        // Perform CloseAction when the user asked the window to close
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ActionManager.performAction(CloseWindowAction.class, MainFrame.this);
            }
        });

        ActionKeymap.registerActions(this);

        // Fire table change events on registered ActivePanelListener instances, to notify of the intial active table.
        fireActivePanelChanged(activeTable.getFolderPanel());

        // Set the custom FocusTraversalPolicy that manages focus for both FolderPanel and their subcomponents.
        setFocusTraversalPolicy(new CustomFocusTraversalPolicy());
        
        // QuickSearchStartAction in action-keymap.xml
        if( ActionKeymap.registedQuickSearchStartAction() ){
            quickSearchActive = false;
        }
    }

    private MainFrame() {
    }

    private FileTableConfiguration getFileTableConfiguration(boolean isLeft) {
        FileTableConfiguration conf;

        conf = new FileTableConfiguration();

        // Loop on columns
        for(int c=0; c<Columns.COLUMN_COUNT; c++) {
            if(c!=Columns.NAME) {       // Skip the special name column (always visible, width automatically calculated)
                // Sets the column's initial visibility.
                conf.setEnabled(c,
                    MuConfiguration.getVariable(
                            MuConfiguration.getShowColumnVariable(c, isLeft),
                            MuConfiguration.getShowColumnDefault(c)
                    )
                );

                // Sets the column's initial width.
                conf.setWidth(c, MuConfiguration.getIntegerVariable(MuConfiguration.getColumnWidthVariable(c, isLeft)));
            }

            // Sets the column's initial order
            conf.setPosition(c,
                    MuConfiguration.getVariable(
                            MuConfiguration.getColumnPositionVariable(c, isLeft),
                            c
                    )
            );
        }

        return conf;
    }

    /**
     * Creates a new main frame set to the given initial folders.
     *
     * @param leftInitialFolder the initial folder to display in the left panel
     * @param rightInitialFolder the initial folder to display in the right panel
     */
    public MainFrame(AbstractFile leftInitialFolder, AbstractFile rightInitialFolder) {
        init(new FolderPanel(this, leftInitialFolder, getFileTableConfiguration(true)), new FolderPanel(this, rightInitialFolder, getFileTableConfiguration(false)));

        leftTable.sortBy(columnNameToIndex(MuConfiguration.getVariable(MuConfiguration.LEFT_SORT_BY, MuConfiguration.DEFAULT_SORT_BY)),
                      !MuConfiguration.getVariable(MuConfiguration.LEFT_SORT_ORDER, MuConfiguration.DEFAULT_SORT_ORDER).equals(MuConfiguration.SORT_ORDER_DESCENDING));
        rightTable.sortBy(columnNameToIndex(MuConfiguration.getVariable(MuConfiguration.RIGHT_SORT_BY, MuConfiguration.DEFAULT_SORT_BY)),
                      !MuConfiguration.getVariable(MuConfiguration.RIGHT_SORT_ORDER, MuConfiguration.DEFAULT_SORT_ORDER).equals(MuConfiguration.SORT_ORDER_DESCENDING));
    	leftFolderPanel.setTreeWidth(MuConfiguration.getVariable(MuConfiguration.LEFT_TREE_WIDTH, 150));
        if (MuConfiguration.getVariable(MuConfiguration.LEFT_TREE_VISIBLE, false)) {
        	leftFolderPanel.setTreeVisible(true);
        }
    	rightFolderPanel.setTreeWidth(MuConfiguration.getVariable(MuConfiguration.RIGHT_TREE_WIDTH, 150));
        if (MuConfiguration.getVariable(MuConfiguration.RIGHT_TREE_VISIBLE, false)) {
        	rightFolderPanel.setTreeVisible(true);
        }

    }

    /**
     * Returns the index of the column designated by the given name.
     *
     * @param column the name of a column, see {@link com.mucommander.ui.main.table.Columns#getColumnName(int)} for possible values
     * @return the index of the column, see {@link com.mucommander.ui.main.table.Columns} for possible values
     */
    private static int columnNameToIndex(String column) {
        for(int c=0; c<Columns.COLUMN_COUNT; c++)
            if(Columns.getColumnName(c).equals(column))
                return c;

        return columnNameToIndex(MuConfiguration.DEFAULT_SORT_BY);
    }


    MainFrame cloneMainFrame() {
        MainFrame mainFrame;

        mainFrame = new MainFrame();
        mainFrame.init(new FolderPanel(mainFrame, leftFolderPanel.getCurrentFolder(), leftTable.getConfiguration()),
                       new FolderPanel(mainFrame, rightFolderPanel.getCurrentFolder(), rightTable.getConfiguration()));
        mainFrame.leftTable.sortBy(leftTable.getSortInfo());
        mainFrame.rightTable.sortBy(rightTable.getSortInfo());
        return mainFrame;
    }

    /**
     * Registers the given ActivePanelListener to receive events when the active table changes.
     *
     * @param activePanelListener the ActivePanelListener to add
     */
    public void addActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.put(activePanelListener, null);
    }

    /**
     * Unregisters the given ActivePanelListener so that it no longer receives events when the active table changes.
     *
     * @param activePanelListener the ActivePanelListener to remove
     */
    public void removeActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.remove(activePanelListener);
    }

    /**
     * Fires table change events on all registered ActivePanelListener instances.
     *
     * @param folderPanel the new active panel
     */
    private void fireActivePanelChanged(FolderPanel folderPanel) {
        Iterator iterator = activePanelListeners.keySet().iterator();
        while(iterator.hasNext())
            ((ActivePanelListener)iterator.next()).activePanelChanged(folderPanel);
    }


    /**
     * Returns <code>true</code> if 'no events mode' is currently enabled.
     *
     * @return <code>true</code> if 'no events mode' is currently enabled
     */
    public boolean getNoEventsMode() {
        return this.noEventsMode;
    }
	
    /**
     * Enables/disables the 'no events mode' which prevents mouse and keyboard events from being received
     * by the application (MainFrame, its subcomponents and the menu bar).
     *
     * @param enabled <code>true</code> to enable 'no events mode', <code>false</code> to disable it
     */
    public void setNoEventsMode(boolean enabled) {
        // Piece of code used in 0.8 beta1 and removed after because it's way too slow, kept here for the record 
        //		// Glass pane has empty mouse and key adapters (created in the constructor)
        //		// which will catch all mouse and keyboard events 
        //		getGlassPane().setVisible(enabled);
        //		getJMenuBar().setEnabled(!enabled);
        //		// Remove focus from whatever component in FolderPanel which had focus
        //		getGlassPane().requestFocus();

        this.noEventsMode = enabled;
    }


    /**
     * Returns the toolbar where shortcut buttons (go back, go forward, ...) are.
     * Note that a non-null instance of ToolBar is returned even if it is currently hidden.
     *
     * @return the toolbar component
     */
    public ToolBar getToolBar() {
        return toolbar;
    }


    /**
     * Returns the command bar, i.e. the component that contains shortcuts to certains actions such as View, Edit, Copy,
     * Move, etc...
     * Note that a non-null instance of CommandBar is returned even if it is currently hidden.
     *
     * @return the command bar component
     */
    public CommandBar getCommandBar() {
        return commandBar;
    }


    /**
     * Returns the status bar, where information about selected files and volume are displayed.
     *
     * @return the status bar
     */
    public StatusBar getStatusBar() {
        return this.statusBar;
    }


    /**
     * Returns the currently active table.
     *
     * <p>The returned table doesn't necessarily have focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if it is currently not in the foreground.</p>
     *
     * <p>Use {@link FileTable#hasFocus()} to test if the table currently has focus.</p>
     *
     * @return the currently active table
     * @see FileTable#isActiveTable()
     */
    public FileTable getActiveTable() {
        return activeTable;
    }

    /**
     * Returns the currently active panel.
     *
     * <p>The returned panel doesn't necessarily have focus, for example if the MainFrame is currently not in the
     * foreground.</p>
     *
     * @return the currently active panel
     */
    public FolderPanel getActivePanel() {
        return activeTable.getFolderPanel();
    }

    /**
     * Sets the currently active FileTable. This method is to be called by FolderPanel only.
     *
     * @param table the currently active FileTable
     */
    void setActiveTable(FileTable table) {
        boolean activeTableChanged = activeTable !=table;

        if(activeTableChanged) {
            this.activeTable = table;

            // Update window title to reflect new active table
            updateWindowTitle();

            // Fire table change events on registered ActivePanelListener instances.
            fireActivePanelChanged(table.getFolderPanel());
        }
    }

	
    /**
     * Returns the inactive table, i.e. the complement of {@link #getActiveTable()}.
     *
     * @return the inactive table
     */
    public FileTable getInactiveTable() {
        return activeTable == leftTable ? rightTable : leftTable;
    }
    
    /**
     * Returns the inactive panel, i.e. the complement of {@link #getActivePanel()}.
     *
     * @return the inactive panel
     */
    public FolderPanel getInactivePanel() {
        return getInactiveTable().getFolderPanel();
    }

    /**
     * Returns the FolderPanel instance corresponding to the left panel.
     *
     * @return the FolderPanel instance corresponding to the left panel
     */
    public FolderPanel getLeftPanel() {
        return leftFolderPanel;
    }

    /**
     * Returns the FolderPanel instance corresponding to the right panel.
     *
     * @return the FolderPanel instance corresponding to the right panel
     */
    public FolderPanel getRightPanel() {
        return rightFolderPanel;
    }


    /**
     * Returns the ProportionalSplitPane component that splits the two panels.
     *
     * @return the ProportionalSplitPane component that splits the two panels
     */
    public ProportionalSplitPane getSplitPane() {
        return splitPane;
    }

    /**
     * Specifies how folder panels are split: if true is passed, the folder panels will be split vertically
     * (default), horizontally otherwise.
     *
     * @param vertical if true, the folder panels will be split horizontally (default), vertically otherwise.
     */
    public void setSplitPaneOrientation(boolean vertical) {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        splitPane.setOrientation(vertical?JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT);
        // Save current split pane orientation to preferences
        saveSplitPaneOrientation();
    }

    /**
     * Returns how folder panels are currently split: if <code>true</code> is returned, panels are split vertically
     * (default), horizontally otherwise.
     *
     * @return <code>true</code> if folder panels are split vertically
     */
    public boolean getSplitPaneOrientation() {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        return splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
    }


    /**
     * Save current split pane orientation to preferences.
     */
    private void saveSplitPaneOrientation() {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        MuConfiguration.setVariable(SPLIT_ORIENTATION, splitPane.getOrientation()==JSplitPane.HORIZONTAL_SPLIT?MuConfiguration.VERTICAL_SPLIT_ORIENTATION:MuConfiguration.HORIZONTAL_SPLIT_ORIENTATION);
    }

    /**
     * Swaps the two FolderPanel instances: after a call to this method, the left FolderPanel will be the right one and
     * vice-versa.
     */
    public void swapFolders() {
        splitPane.remove(leftFolderPanel);
        splitPane.remove(rightFolderPanel);

        // Swaps the folder panels.
        FolderPanel tempPanel = leftFolderPanel;
        leftFolderPanel = rightFolderPanel;
        rightFolderPanel = tempPanel;

        // swaps folders trees
        int tempTreeWidth = leftFolderPanel.getTreeWidth();
        leftFolderPanel.setTreeWidth(rightFolderPanel.getTreeWidth());
        rightFolderPanel.setTreeWidth(tempTreeWidth);
        boolean tempTreeVisible = leftFolderPanel.isTreeVisible();
        leftFolderPanel.setTreeVisible(rightFolderPanel.isTreeVisible());
        rightFolderPanel.setTreeVisible(tempTreeVisible);
        

        // Resets the tables.
        FileTable tempTable = leftTable;
        leftTable = rightTable;
        rightTable = tempTable;

        // Preserve the sort order and columns visibility.
        TableColumnModel model = leftTable.getColumnModel();
        leftTable.setColumnModel(rightTable.getColumnModel());
        rightTable.setColumnModel(model);

        SortInfo sortInfo = (SortInfo)leftTable.getSortInfo().clone();

        leftTable.sortBy(rightTable.getSortInfo());
        leftTable.updateColumnsVisibility();

        rightTable.sortBy(sortInfo);
        rightTable.updateColumnsVisibility();

        // Do the swap and update the split pane
        splitPane.setLeftComponent(leftFolderPanel);
        splitPane.setRightComponent(rightFolderPanel);

        splitPane.doLayout();

        // Update split pane divider's location
        splitPane.updateDividerLocation();

        activeTable.requestFocus();
    }


    /**
     * Makes both folders the same, choosing the one which is currently active. 
     */
    public void setSameFolder() {
        (activeTable == leftTable ? rightTable : leftTable).getFolderPanel().tryChangeCurrentFolder(activeTable.getCurrentFolder());
    }
    /**
     * It makes it to the same directory as both. 
     */
    public void setSameFolderAsBoth() {
    	activeTable.getFolderPanel().tryChangeCurrentFolder(getInactivePanel().getCurrentFolder());
    }


    /**
     * Returns <code>true</code> if this MainFrame is currently active in the foreground.
     *
     * @return <code>true</code> if this MainFrame is currently active in the foreground
     */
    public boolean isForegroundActive() {
        return foregroundActive;
    }

    /**
     * Sets whether this MainFrame is currently active in the foreground. This method is to be called by WindowManager
     * only.
     *
     * @param foregroundActive true if this MainFrame is currently active in the foreground
     */
    void setForegroundActive(boolean foregroundActive) {
        this.foregroundActive = foregroundActive;
    }

    /**
     * Forces a refrehs of the frame's folder panel.
     */
    public void tryRefreshCurrentFolders() {
        leftFolderPanel.tryRefreshCurrentFolder();
        rightFolderPanel.tryRefreshCurrentFolder();
    }


    /**
     * Returns <code>true</code> if this MainFrame is active, or is an ancestor of a Window that is currently active.
     *
     * @return <code>true</code> if this MainFrame is active, or is an ancestor of a Window that is currently active
     */
    public boolean isAncestorOfActiveWindow() {
        if(isActive())
            return true;

        Window ownedWindows[] = getOwnedWindows();

        int nbWindows = ownedWindows.length;
        for(int i=0; i<nbWindows; i++)
            if(ownedWindows[i].isActive())
                return true;

        return false;
    }


    /**
     * Updates this window's title to show currently active folder and window number.
     * This method is called by this class and WindowManager.
     */
    public void updateWindowTitle() {
        // Update window title
        String title = activeTable.getCurrentFolder().getAbsolutePath();
        Vector mainFrames = WindowManager.getMainFrames();
        if(mainFrames.size()>1)
            title += " ["+(mainFrames.indexOf(this)+1)+"]";
        setTitle(title);

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard) with Java 1.5 and up
        if(OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
            // Displays the document icon in the window title bar, works only for local files
            AbstractFile currentFolder = activeTable.getCurrentFolder();
            Object javaIoFile;
            if(currentFolder.getURL().getScheme().equals(FileProtocols.FILE)) {
                // If the current folder is an archive entry, display the archive file, this is the closest we can get
                // with a java.io.File
                if(currentFolder.hasAncestor(ArchiveEntryFile.class))
                    javaIoFile = currentFolder.getParentArchive().getUnderlyingFileObject();
                else
                    javaIoFile = currentFolder.getUnderlyingFileObject();
            }
            else {
                // If the current folder is not a local file, use the special /Network directory which is sort of
                // 'Network Neighborhood'.
                javaIoFile = new java.io.File("/Network");
            }

            // Note that for some strange reason (looks like a bug), setting the property to null won't remove
            // the previous icon.
            getRootPane().putClientProperty("Window.documentFile", javaIoFile);
        }
    }
    

    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        // Update window title to reflect the new current folder
        updateWindowTitle();
    }

    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }

    public void locationFailed(LocationEvent e) {
    }


    ///////////////////////
    // Overriden methods //
    ///////////////////////

    /**
     * Overrides <code>java.awt.Window#dispose</code> to save last MainFrame's attributes in the preferences
     * before disposing this MainFrame.
     */
    public void dispose() {
        // Save last MainFrame's attributes (last folders, window position) in the preferences.

        // Save last folders
        MuConfiguration.setVariable(MuConfiguration.LAST_LEFT_FOLDER, 
                                         getLeftPanel().getFolderHistory().getLastRecallableFolder());
        MuConfiguration.setVariable(MuConfiguration.LAST_RIGHT_FOLDER, 
                                         getRightPanel().getFolderHistory().getLastRecallableFolder());

        // Save window position, size and screen resolution
        Rectangle bounds = getBounds();
        MuConfiguration.setVariable(MuConfiguration.LAST_X, (int)bounds.getX());
        MuConfiguration.setVariable(MuConfiguration.LAST_Y, (int)bounds.getY());
        MuConfiguration.setVariable(MuConfiguration.LAST_WIDTH, (int)bounds.getWidth());
        MuConfiguration.setVariable(MuConfiguration.LAST_HEIGHT, (int)bounds.getHeight());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        MuConfiguration.setVariable(MuConfiguration.SCREEN_WIDTH, screenSize.width);
        MuConfiguration.setVariable(MuConfiguration.SCREEN_HEIGHT, screenSize.height);

        // Saves left and right table positions.
        for(boolean isLeft=true; ; isLeft=false) {
            FileTable table = isLeft? leftTable : rightTable;
            // Loop on columns
            for(int c=0; c<Columns.COLUMN_COUNT; c++) {
                if(c!=Columns.NAME) {       // Skip the special name column (always enabled, width automatically calculated)
                    MuConfiguration.setVariable(
                        MuConfiguration.getShowColumnVariable(c, isLeft),
                        table.isColumnEnabled(c)
                    );

                    MuConfiguration.setVariable(
                        MuConfiguration.getColumnWidthVariable(c, isLeft),
                        table.getColumnWidth(c)
                    );
                }

                MuConfiguration.setVariable(
                    MuConfiguration.getColumnPositionVariable(c, isLeft),
                    table.getColumnPosition(c)
                );
            }

            if(!isLeft)
                break;
        }

        // Saves left and right table sort order.
        MuConfiguration.setVariable(MuConfiguration.LEFT_SORT_BY, Columns.getColumnName(leftTable.getSortInfo().getCriterion()));
        MuConfiguration.setVariable(MuConfiguration.LEFT_SORT_ORDER, leftTable.getSortInfo().getAscendingOrder() ? MuConfiguration.SORT_ORDER_ASCENDING : MuConfiguration.SORT_ORDER_DESCENDING);
        MuConfiguration.setVariable(MuConfiguration.RIGHT_SORT_BY, Columns.getColumnName(rightTable.getSortInfo().getCriterion()));
        MuConfiguration.setVariable(MuConfiguration.RIGHT_SORT_ORDER, rightTable.getSortInfo().getAscendingOrder() ? MuConfiguration.SORT_ORDER_ASCENDING : MuConfiguration.SORT_ORDER_DESCENDING);

        // Save split pane orientation
        saveSplitPaneOrientation();
        
        // Save tree folders preferences
        MuConfiguration.setVariable(MuConfiguration.LEFT_TREE_VISIBLE, leftFolderPanel.isTreeVisible());
        MuConfiguration.setVariable(MuConfiguration.RIGHT_TREE_VISIBLE, rightFolderPanel.isTreeVisible());
        MuConfiguration.setVariable(MuConfiguration.LEFT_TREE_WIDTH, leftFolderPanel.getTreeWidth());
        MuConfiguration.setVariable(MuConfiguration.RIGHT_TREE_WIDTH, rightFolderPanel.getTreeWidth());

        // Finally, dispose the frame
        super.dispose(); 
    }


    /**
     * Overrides <code>java.awt.Window#toFront</code> to have the window return to a normal state if it is minimized.
     */
    public void toFront() {
        if((getExtendedState()&Frame.ICONIFIED)!=0)
            setExtendedState(Frame.NORMAL);
        super.toFront();
    }



    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Manages focus for both FolderPanel and their subcomponents.
     *
     * @author Maxence Bernard
     */
    protected class CustomFocusTraversalPolicy extends FocusTraversalPolicy {

        public Component getComponentAfter(Container container, Component component) {
        	if (component instanceof QuickListFocusableComponent) {
        		return ((QuickListFocusableComponent) component).getInvokerFileTable();
        	} else if (component==leftFolderPanel.getFoldersTreePanel().getTree()) {
		        return leftTable;
		    } else if (component==rightFolderPanel.getFoldersTreePanel().getTree()) {
		        return rightTable;
		    } else if(component== leftFolderPanel.getLocationTextField())
                return leftTable;
            else if(component== leftTable)
                return rightTable;
            if(component== rightFolderPanel.getLocationTextField())
                return rightTable;
            else    // component==table2
                return leftTable;
        }

        public Component getComponentBefore(Container container, Component component) {
            // Completly symetrical with getComponentAfter
            return getComponentAfter(container, component);
       }

        public Component getFirstComponent(Container container) {
            return leftTable;
        }

        public Component getLastComponent(Container container) {
            return rightTable;
        }

        public Component getDefaultComponent(Container container) {
            return getActiveTable();
        }
    }

    public boolean isAutoSizeColumnsEnabled() {
        return leftTable.isAutoSizeColumnsEnabled();
    }

    public void setAutoSizeColumnsEnabled(boolean b) {
        leftTable.setAutoSizeColumnsEnabled(b);
        rightTable.setAutoSizeColumnsEnabled(b);
    }

    /**
     * quickSearchActive getter
     * @return quickSearchActive
     */
    public boolean isQuickSearchActive() {
        return quickSearchActive;
    }

    /**
     * quickSearchActive setter
     * @param quickSearchActive setting quickSearchActive
     */
    public void setQuickSearchActive(boolean quickSearchActive) {
        this.quickSearchActive = quickSearchActive;
    }
}
