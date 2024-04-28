package com.kdt.pickafile;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileNameSorter {

    public static void main(String[] args) {
        File directory = new File("path/to/directory");
        List<File> files = Arrays.asList(directory.listFiles());
        files.sort(new SortFileName());
        for (File file : files) {
            System.out.println(file.getName());
        }
    }
}

class SortFileName implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
    }
}
