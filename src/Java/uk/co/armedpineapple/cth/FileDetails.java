package uk.co.armedpineapple.cth;

import java.util.Date;

public class FileDetails implements Comparable<FileDetails> {

    private final Date   lastModified;
    private final String fileName;


    private final String directory;

    public FileDetails(String filename, String directory, Date lastModified) {
        this.fileName = filename;
        this.lastModified = lastModified;
        this.directory = directory;
    }


    public String getDirectory() {
        return directory;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int compareTo(FileDetails another) {
        if (lastModified.equals(another.getLastModified())) {
            return 0;
        }

        return lastModified.after(another.getLastModified()) ? 1 : -1;

    }
}
