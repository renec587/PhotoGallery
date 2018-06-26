package com.example.photogallery;

import java.io.File;
import java.util.ArrayList;

public interface IFileManager {

    // Save given file
    void save(File fileName);
    // Removes current entry, if any exists
    void remove();
    //Updates current entry with this file
    void update(File fileName);
    // Filters entries to fall between time1 and time2
    void filterTime(String time1,String time2);
    // Filters entries that match these keywords
    void filter(ArrayList<String> keywords);
    // Removes all active filters
    void resetFilter();
    //Gets currently selected entry
    File get();
    // Gets number of entries
    int size();
    // Gets all entries, even if no filter applied
    int allSize();
    // Moves marker to next entry, returns > 0 if changed, false otherwise
    int next();
    // Moves marker to previous entry, returns >= 0 if changed, false otherwise
    int previous();
}
