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


package com.mucommander.ui.main.menu;

import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import sun.reflect.Reflection;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.action.CommandAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Contextual popup menu invoked by FileTable when call OpenWithPopupMenuAction
 *
 * @author Lei
 */
public class OpenWithPopupMenu extends JPopupMenu {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** Parent MainFrame instance */
    private MainFrame mainFrame;


    /**
     * Creates a new OpenWithPopupMenu.
     *
     * @param mainFrame parent MainFrame instance
     * @param currentFolder current folder in table
     * @param clickedFile right-clicked file, can be null if user clicked on the folder table background
     * @param markedFiles list of marked files, can be empty but never null
     */
    public OpenWithPopupMenu(MainFrame mainFrame, AbstractFile currentFolder, AbstractFile clickedFile, FileSet markedFiles) {
        this.mainFrame = mainFrame;

        Iterator iterator = CommandManager.getCommandsForFile(clickedFile);
        Command  command;

        while(iterator.hasNext()) {
            command = (Command)iterator.next();
            if(command.getType() == Command.NORMAL_COMMAND){
                JMenuItem item = add(new CommandAction(this.mainFrame, new Hashtable(), command));
                // set key nemonic
                item.setMnemonic( command.getDisplayName().charAt(0) );
            }
        }
    }

}
