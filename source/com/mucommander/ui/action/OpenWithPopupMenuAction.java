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

package com.mucommander.ui.action;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.OpenWithPopupMenu;
import com.mucommander.ui.main.table.FileTable;

/**
 * This action pops up the file Properties dialog.
 *
 */
public class OpenWithPopupMenuAction extends SelectedFilesAction {

    public OpenWithPopupMenuAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        FileSet files = fileTable.getSelectedFiles();
        if( files.size() > 0 ){
            int currentRow = fileTable.getSelectedRow();
            Rectangle cellRect = fileTable.getCellRect(currentRow, 0, true);
            OpenWithPopupMenu menu = new OpenWithPopupMenu(mainFrame, fileTable.getCurrentFolder(), files.fileAt(0), files);
            menu.show(fileTable, cellRect.x+10, cellRect.y+30);

            // selected first item
            menu.dispatchEvent(new KeyEvent(menu, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED ));
        }
    }
}