package com.example.photogallery;

import android.content.Context;
import android.media.ExifInterface;
import android.os.Environment;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DiskFiles implements IFileManager {

    private static final String EARLY_DATE = "01/01/1900";
    private static final String LATE_DATE = "01/01/9999";
    private ArrayList<File> fileList;
    private ArrayList<Integer> filterIds;
    private int currentIndex = -1;
    private ArrayList<String> keywords;
    private String date1, date2;
    private String coord1,coord2;

    public DiskFiles(Context context) {
        fileList = new ArrayList<File>();
        filterIds = new ArrayList<Integer>();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        fileList = new ArrayList<File>(Arrays.asList(storageDir.listFiles()));
        date1 = date2 = coord1 = coord2 = "";
        keywords = new ArrayList<String>();
        setFilters();
    }

    // Save given file
    public void save(File fileName){
        fileList.add(fileName);
        setFilters();
    }
    // Removes current entry, if any exists
    public void remove() {
        if(fileList.size() == 0) return;
        fileList.remove(filterIds.get(currentIndex));
        setFilters();
    }

    //Updates current entry with this file
    // FIXME - Not implemented
    public void update(File fileName) {
        return;
    }
    // Filters entries to fall between time1 and date2, returns # of entries which matched
    public int filterTime(String time1,String time2){
        if(time1.isEmpty() && time2.isEmpty()) return filterIds.size();
        this.date1 = time1;
        this.date2 = time2;
        setFilters();
        return filterIds.size();
    }
    // Filters entries that fall between coord1 and coord2, returns # of matching entries (all filters applied)
    public int filterLocation(String coord1,String coord2) {
        this.coord1 = coord1;
        this.coord2 = coord2;
        setFilters();
        return filterIds.size();
    }
    // Filters entries that match these keywords returns # of entries which matched
    public int filter(ArrayList<String> keywords) {
        if(keywords.size() == 0) return filterIds.size();
        keywords.clear();
        setFilters();
        return filterIds.size();
    }
    // Removes all active filters
    public void resetFilter() {
        keywords.clear();
        date1 = date2 = "";
        coord1 = coord2 = "";
        setFilters();
    }
    //Gets currently selected entry
    public File get() {
        if(filterIds.size() == 0) return null;
        return fileList.get(filterIds.get(currentIndex));
    }
    // Gets number of entries
    public int size() {
        return filterIds.size();
    }
    // Gets all entries, even if no filter applied
    public int allSize() {
        return fileList.size();
    }
    // Moves marker to next entry, returns > 0 if changed, -1 if no change
    public int next() {
        if(filterIds.size() == 0 || filterIds.size() == currentIndex+1) return -1;
        return ++currentIndex;
    }

    // Moves marker to previous entry, returns >= 0 if changed, -1
    public int previous() {
        if(filterIds.size() <= 1 || currentIndex == 0) return -1;
        return --currentIndex;
    }

    // Applies the set filters to the dataset
    private void setFilters() {
        filterIds.clear();
        if(keywords.size() == 0 && coord1.isEmpty() && date1.isEmpty()) {
            for(int i = 0; i < fileList.size(); i++) filterIds.add(i);
        } else {
            for (int i = 0; i < fileList.size(); i++) {
                if(keywordMatch(fileList.get(i)) && locationMatch(fileList.get(i)) && dateMatch(fileList.get(i))) {
                    filterIds.add(i);
                }
            }
        }
        currentIndex = filterIds.size() -1;
    }

    private boolean dateMatch(File file) {
        if(this.date1.isEmpty() && this.date2.isEmpty()) return true;
        String fileDateTime;
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            fileDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch(Exception e) {
            System.out.println("getDateTime:" + e);
            return true; // We default to true if we can't prove false.
        }
        String fileDate[] = fileDateTime.split(":");
        if(this.date1.isEmpty()) this.date1 = EARLY_DATE;
        if(this.date2.isEmpty()) this.date2 = LATE_DATE;
        String date1[] = this.date1.split("/"); //Index 0 is day
        String date2[] = this.date2.split("/"); //Index 1 is month, Index 2 is year.
        if (date1.length == 3 && date2.length == 3) {
            fileDate[2] = fileDate[2].substring(0, 2);
            if (Integer.parseInt(date1[2]) <= Integer.parseInt(fileDate[0]) && Integer.parseInt(date2[2]) >= Integer.parseInt(fileDate[0])) { //Within Year
                if (Integer.parseInt(date1[1]) <= Integer.parseInt(fileDate[1]) && Integer.parseInt(date2[1]) >= Integer.parseInt(fileDate[1])) { //Within Month
                    if (Integer.parseInt(date1[0]) <= Integer.parseInt(fileDate[2]) && Integer.parseInt(date2[0]) >= Integer.parseInt(fileDate[2])) { //Within Month
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //FIXME - Not implemented.
    private boolean locationMatch(File file) {
        return true;
    }

    private boolean keywordMatch(File file) {
        ExifInterface exif;
        String keyword;
        try {
            exif = new ExifInterface(file.getPath());
            keyword = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        } catch (Exception e) {
            return true; //I guess we match if there is nothing there!
        }
        for(int i = 0; i < keywords.size(); i++) {
            if (!keyword.equalsIgnoreCase(keywords.get(i))) return false;
        }
        return true;
    }

}
