package net.kdt.pojavlaunch.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

    /**
     * Gets an InputStream for a given ZIP entry, throwing an IOException if the ZIP entry does not
     * exist.
     *
     * @param zipFile    The ZipFile to get the entry from
     * @param entryPath The full path inside of the ZipFile
     * @return The InputStream provided by the ZipFile
     * @throws IOException if the entry was not found
     */
    public static InputStream getEntryStream(ZipFile zipFile, String entryPath) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryPath);
        if (entry == null) {
            throw new IOException("No entry in ZIP file: " + entryPath);
        }
        return zipFile.getInputStream(entry);
    }

    /**
     * Extracts all files in a ZipFile inside of a given directory to a given destination directory
     *
     * @param zipFile      The ZipFile to extract files from
     * @param directory   The directory to extract the files from
     * @param destination The destination directory to extract the files into
     * @throws IOException if it was not possible to create a directory or file extraction failed
     */
    public static void extractZipFile(ZipFile zipFile, String directory, File destination) throws IOException {
        if (zipFile == null || destination == null || directory == null) {
            throw new NullPointerException("zipFile, directory, and destination cannot be null");
        }

        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        int dirLength = directory.length();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            String entryName = zipEntry.getName();
            if (!entryName.startsWith(directory) || zipEntry.isDirectory()) {
                continue;
            }

            File entryDestination = new File(destination, entryName.substring(dirLength));
            FileUtils.ensureParentDirectory(entryDestination);

            if (zipEntry.isDirectory()) {
                continue;
            }

            try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                 OutputStream outputStream = new FileOutputStream(entryDestination)) {
                IOUtils.copy(inputStream, outputStream);
            }
        }
    }
}
