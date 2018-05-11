package com.example.photogallery;

import android.content.Context;
import android.media.ExifInterface;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private DiskFiles testFiles;

    @Test
    @Before
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
//Changes to see if git works.
        assertEquals("com.example.photogallery", appContext.getPackageName());
        testFiles = new DiskFiles(appContext);
    }

    @Test
    public void testCaptions() {
        File testFile;
        testFile = testFiles.get();
        assertEquals("Home",getCaption(testFile));
        testFiles.previous();
        testFile = testFiles.get();
        assertEquals("Kitchen",getCaption(testFile));
        testFiles.previous();
        testFile = testFiles.get();
        assertEquals("BookCase",getCaption(testFile));
        testFiles.previous();
        testFile = testFiles.get();
        assertEquals("Cat",getCaption(testFile));
        testFiles.previous();
        testFile = testFiles.get();
        assertEquals("Home",getCaption(testFile));
    }

    public String getCaption(File thisFile) {
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
