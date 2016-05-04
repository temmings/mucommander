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


package com.mucommander.ui.dialog.file;

import java.io.IOException;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog that allows the user to choose the destination to unpack files to.
 *
 * @author Maxence Bernard
 */
public class UnpackDialog extends TransferDestinationDialog {

    /**
     * Creates and displays a new UnpackDialog.
     *
     * @param mainFrame the main frame this dialog is attached to
     * @param files the set of files to unpack
     * @param isShiftDown true if shift key was pressed when invoking this dialog
     */
    public UnpackDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
        this(mainFrame, files, isShiftDown, null );
    }

    /**
     * Creates and displays a new UnpackDialog.
     *
     * @param mainFrame the main frame this dialog is attached to
     * @param files the set of files to unpack
     * @param isShiftDown true if shift key was pressed when invoking this dialog
     * @param postFixFolderName append unpack target folder name
     */
    public UnpackDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown, String postFixFolderName ) {
        super(mainFrame, files,
              MuAction.getStandardLabel(com.mucommander.ui.action.UnpackAction.class),
              Translator.get("unpack_dialog.destination"),
              Translator.get("unpack_dialog.unpack"),
              Translator.get("unpack_dialog.error_title"));
        
        AbstractFile destFolder = mainFrame.getInactiveTable().getCurrentFolder();
        String fieldText;
        if(isShiftDown)
            fieldText = ".";
        else{
            if( postFixFolderName == null ){
                fieldText = destFolder.getAbsolutePath(true);
            }else{
                fieldText = PathUtils.resolveDestination(postFixFolderName, destFolder).getDestinationFile().getAbsolutePath(true);
            }
        }
        
        setTextField(fieldText);
        
        showDialog();
    }

    protected void startJob(PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction, boolean verifyIntegrity) {
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("unpack_dialog.unpacking"));
        AbstractFile destFolder = FileFactory.getFile(pathField.getText());
        if( !destFolder.exists() ){
            try {
                destFolder.mkdir();
            }catch(IOException e) {
                // Unable to create folder
                showErrorDialog(errorDialogTitle, Translator.get("cannot_create_folder", destFolder.getName()) );
                return;
            }
        }
        resolvedDest = PathUtils.resolveDestination(destFolder.getAbsolutePath(), resolvedDest.getDestinationFolder());
        /*
        String newDir = files.fileAt(0).getNameWithoutExtension();
        try{
            resolvedDest.getDestinationFolder().mkdir(newDir);
            resolvedDest = PathUtils.resolveDestination(newDir, resolvedDest.getDestinationFolder());
        }catch (IOException io) {
            // output log ...
        }
        */
        CopyJob job = new CopyJob(
                progressDialog,
                mainFrame,
                files,
                resolvedDest.getDestinationFolder(),
                resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER?null:resolvedDest.getDestinationFile().getName(),
                CopyJob.UNPACK_MODE,
                defaultFileExistsAction);

        job.setIntegrityCheckEnabled(verifyIntegrity);

        progressDialog.start(job);
    }

}
