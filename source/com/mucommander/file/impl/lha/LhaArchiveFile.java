package com.mucommander.file.impl.lha;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import jp.gr.java_conf.dangan.util.lha.LhaFile;
import jp.gr.java_conf.dangan.util.lha.LhaHeader;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;


/**
 * Lha Archive
 */
public class LhaArchiveFile extends AbstractROArchiveFile {

    /** The LhaFile object that actually reads the entries in the Lha file */
    private LhaFile lhaFile;
   
    /** The date at which the current LhaFile object was created */
    private long lastLhaFileDate;   
   
   
    public LhaArchiveFile(AbstractFile file) throws IOException {       
        super(file);
    }
   
    /**
     * Checks if the underlying Lha file is up-to-date, i.e. exists and has not changed without this archive file
     * being aware of it. If one of those 2 conditions are not met, (re)load the RipFile instance (parse the entries)
     * and declare the Lha file as up-to-date.
     *
     * @throws IOException if an error occurred while reloading
     */
    private void checkLhaFile() throws IOException {
        long currentDate = file.getDate();
        if (lhaFile==null || currentDate != lastLhaFileDate) {
            lhaFile = new LhaFile(file.getAbsolutePath());
            declareLhaFileUpToDate(currentDate);
        }
    }
   
    /**
     * Declare the underlying Lha file as up-to-date. Calling this method after the Lha file has been
     * modified prevents {@link #checkLhaFile()} from being reloaded.
     */
    private void declareLhaFileUpToDate(long currentFileDate) {
        lastLhaFileDate = currentFileDate;
    }

    /**
     * Creates and return an {@link ArchiveEntry()} whose attributes are fetched from the given {@link com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile.FileHeader}
     *
     * @param FileHeader the object that serves to initialize the attributes of the returned ArchiveEntry
     * @return an ArchiveEntry whose attributes are fetched from the given FileHeader
     */
    private ArchiveEntry createArchiveEntry(LhaHeader header) {
        String path = header.getPath().replace('\\', '/');
        return new ArchiveEntry(
                path,
                path.endsWith("/"),
                header.getLastModified().getTime(),
                header.getOriginalSize()
        );
    }
   
    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////
   
    public synchronized Vector getEntries() throws IOException {
        checkLhaFile();
        Vector entries = new Vector();
        Enumeration iter = lhaFile.entries();
        while(iter.hasMoreElements())
            entries.add(createArchiveEntry((LhaHeader ) iter.nextElement()));
       
        return entries;
    }   
   
    public synchronized InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        checkLhaFile();
       
        return lhaFile.getInputStream(entry.getPath().replace('/', '\\'));
    }
}

