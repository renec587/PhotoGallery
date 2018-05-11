package com.example.photogallery;

import android.content.Context;
import android.media.ExifInterface;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private DiskFiles testFiles;
    private String[] expectedCaptions = {"Home","Kitchen","BookCase","Cat","Home"}; //From last to first
    private String[] expectedFilenames = {"JPEG_20180503_200341_2047183590.jpg","JPEG_20180503_200115_1410467293.jpg","JPEG_20180502_233559_1645060181.jpg","JPEG_20180502_233500_470527435.jpg","JPEG_20180502_233449_1898546846.jpg",null};

    private static final int PREV_NEXT_STEPS = 10;

    @Test
    @Before
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.example.photogallery", appContext.getPackageName());
        testFiles = new DiskFiles(appContext);
    }

    /* Test if hitting previous multiple times will crash */
    @Test
    public void testPrevious() {
        for(int i = 0; i < PREV_NEXT_STEPS;i++) {
            testFiles.previous();
        }
    }

    /* Test if hitting next multiple times will crash */
    @Test
    public void testNext() {
        for(int i =0; i < PREV_NEXT_STEPS;i++) {
            testFiles.next();
        }
    }

    @Test
    public void testCaptions() {
        File testFile;
        for (String caption:expectedCaptions) {
            testFile = testFiles.get();
            assertEquals(caption,getCaption(testFile));
            testFiles.previous();
        }
    }

    @Test
    public void testKeywords() {
        File file;
        ArrayList<String> keywords = new ArrayList<>();
        // Special test of "Home" keyword, it should have two results
        keywords.add(expectedCaptions[0]);
        testFiles.filter(keywords);
        file = testFiles.get();
        assertEquals(expectedFilenames[0],file.getName());
        testFiles.previous();
        file = testFiles.get();
        assertEquals(expectedFilenames[4],file.getName());
        keywords.clear();
        testFiles.resetFilter();
        // Test remainder keywords
        for(int i = 1; i < expectedCaptions.length - 1;i++) {
            keywords.add(expectedCaptions[i]);
            testFiles.filter(keywords);
            file = testFiles.get();
            assertEquals(expectedFilenames[i], file.getName());
            keywords.clear();
            testFiles.resetFilter();
        }
        // Test for none found
        keywords.add("NOMATCH");
        testFiles.filter(keywords);
        assertEquals(null,testFiles.get());
        // Test blank keyword
        keywords.clear();
        keywords.add("");
        testFiles.filter(keywords);
        assertEquals(null,testFiles.get());
        //Test completely empty keywords array
        keywords.clear();
        testFiles.filter(keywords);
        assertEquals(null,testFiles.get());
    }

    @Test
    public void testDates() {
        /* The tested method assumes and expects to receive dates in valid format. */
        //tryDates are in date pairs
        String[] tryFirst = {"03/05/2018","02/05/2018","01/01/1000","01/01/2017","01/01/2019"};
        String[] trySecond ={"03/05/2018","02/05/2018","01/01/9999","31/12/2017","31/12/2019"};
        File file;
        Integer[][] expectedResults = { {0,1},{2,3,4},{0,1,2,3,4},{5},{5}};
        for(int i = 0; i < tryFirst.length;i++) {
            testFiles.resetFilter();
            testFiles.filterTime(tryFirst[i],trySecond[i]);
            for(int index:expectedResults[i]) {
                file = testFiles.get();
                Log.d("Values:",tryFirst[i] + ":" + trySecond[i] + ":" + expectedFilenames[index]);
                if(expectedFilenames[index] == null) {
                    assertNull(file);
                } else {
                    assertNotNull(file);
                    assertEquals(expectedFilenames[index],file.getName());
                }
                testFiles.previous();
            }
        }
        // Call with blank strings, check for crashing only
        testFiles.resetFilter();
        testFiles.filterTime("","");
        assertNotNull(testFiles.get());
        // Call with null strings, check for crashing only
        testFiles.resetFilter();
        testFiles.filterTime(null,null);
        assertNotNull(testFiles.get());
    }


    private String getCaption(File thisFile) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(thisFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
    }


}
