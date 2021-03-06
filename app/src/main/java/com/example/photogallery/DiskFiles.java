package com.example.photogallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.ExifInterface;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;


import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.boxes.apple.AppleNameBox;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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
        fileList = new ArrayList<>();
        filterIds = new ArrayList<>();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        fileList = new ArrayList<>(Arrays.asList(storageDir.listFiles()));
        date1 = date2 = coord1 = coord2 = "";
        keywords = new ArrayList<>();
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
        fileList.remove((int)filterIds.get(currentIndex));

        setFilters();
    }

    //Updates current entry with this file
    // FIXME - Not implemented
    public void update(File fileName) {

    }
    // Filters entries to fall between time1 and date2, returns # of entries which matched
    public void filterTime(String time1,String time2){
        if(time1 == null || time2 == null) return;
        if(time1.isEmpty() && time2.isEmpty()) return;
        this.date1 = time1;
        this.date2 = time2;
        return;
    }
    // Filters entries that match these keywords returns # of entries which matched
    public void filter(ArrayList<String> keywords) {
        if(keywords.size() == 0) {
            this.keywords.clear();
            return;
        }
        this.keywords = new ArrayList<>(keywords);
        return;
    }
    // Removes all active filters
    public void resetFilter() {
        keywords.clear();
        date1 = date2 = "";
        coord1 = coord2 = "";
        setFilters();
    }

    /* Returns type of current file. 1 is Image, 2 is video. Yes I hardcoded this. Sue me.*/
    private int getType(File currentFile) {
      if(currentFile == null) return -1;
      String fileName = currentFile.getName();
      if (fileName.endsWith("jpg")) {
          return 1;
      }
      return 2;
    }

    public int getType() {
        return this.getType(get());
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
    public int setFilters() {
        if(fileList.size() == 0) return fileList.size();
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
        return filterIds.size();
    }

    private boolean dateMatch(File file) {
        if(this.date1.isEmpty() || this.date2.isEmpty()) return true;
        String fileDateTime;
        String filename = file.getName();
        String test[] = filename.split("_");
        String fileDate[] = new String[3];
        fileDate[0] = test[1].substring(0,4);
        fileDate[1] = test[1].substring(4,6);
        fileDate[2] = test[1].substring(6,8);

        if(this.date1.isEmpty()) this.date1 = EARLY_DATE;
        if(this.date2.isEmpty()) this.date2 = LATE_DATE;
        String date1[] = this.date1.split("/"); //Index 0 is day
        String date2[] = this.date2.split("/"); //Index 1 is month, Index 2 is year.
        if (date1.length == 3 && date2.length == 3) {
            int day1,month1,year1;
            int day2,month2,year2;
            int day3,month3,year3;
            day1 = Integer.parseInt(date1[0]); month1 = Integer.parseInt(date1[1]); year1 = Integer.parseInt(date1[2]);
            day2 = Integer.parseInt(fileDate[2]); month2 = Integer.parseInt(fileDate[1]); year2 = Integer.parseInt(fileDate[0]);
            day3 = Integer.parseInt(date2[0]); month3 = Integer.parseInt(date2[1]); year3 = Integer.parseInt(date2[2]);
            int flag1 = compareDate(year1,month1,day1,year2,month2,day2);
            int flag2 = compareDate(year3,month3,day3,year2,month2,day2);
            if( (flag1 <= 0 && flag2 >= 0) ) {
                return true;
            }
        }
        return false;
    }

    //FIXME - Not implemented.
    private boolean locationMatch(File file) {
        return true;
    }

    private boolean keywordMatch(File file) {
        ExifInterface exif;
        String keyword;
        keyword = getCaption(file);
        if(keyword == null) return false; //No keyword stored in file, so it can't match.
        for(int i = 0; i < keywords.size(); i++) {
            if(keywords.get(i).equals("")) return true;
            if (!keyword.equalsIgnoreCase(keywords.get(i))) return false;
        }
        return true;
    }

    /* Returns -1 for less than, 0 for equal, 1 for greater than */
    private int compareDate(int year1,int month1,int day1,int year2,int month2,int day2) {
        if(year1 == year2) {
            if(month1 == month2) {
                if(day1 == day2) {
                    return 0; //Matching dates
                } else {
                    if(day1 < day2) return -1; // less than
                    return 1;  //greater than
                }
            } else {
                if(month1 < month2) return -1; // less than
                return 1; //greater than
            }
        } else {
            if(year1 < year2) return -1; // less than
            return 1; // greater than
        }
    }

    public String getCaption() {
        return getCaption(this.get());
    }

    private String getCaption(File imageFile) {
        String caption = "No Caption";
        int type = getType(imageFile);
        if(type == 2) {
            IsoFile isoFile;
            try {
                isoFile = new IsoFile(imageFile.getPath());
            } catch (IOException ex) {
                return "Caption Error";
            }
            AppleNameBox nam = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/ilst/©nam");
            caption = nam.getValue();
        } else if(type == 1){
            try {
                ExifInterface exif = new ExifInterface(imageFile.getPath());
                caption = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            } catch (Exception e) {
                return "Caption Error";
            }
        }
        return caption;
    }

}
