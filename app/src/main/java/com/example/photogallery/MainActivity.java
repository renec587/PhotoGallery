package com.example.photogallery;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static java.lang.Boolean.TRUE;

//TODO - Use Location services to insert EXIF data into each picture after it is taken.
public class MainActivity extends AppCompatActivity {

    Uri photoURI;
    private TextView mTextMessage;
    static final int REQUEST_IMAGE = 1;
    static final int SEARCH_ACTIVITY = 2;
    String mCurrentPhotoPath;
    ImageView imageView;
    TextView tvDateTime;
    TextView tvLatLong;
    ArrayList<File> fileList;
    int fileIndex = 0;

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        imageView = (ImageView)findViewById(R.id.imageView);
        tvLatLong = (TextView)findViewById(R.id.tvLatLong);
        tvDateTime = (TextView)findViewById(R.id.textDateTime);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        fileList = new ArrayList<File>(Arrays.asList(storageDir.listFiles()));
        if(fileList.size() >= 1) imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",fileList.get(0)));
        showImageAttribs();
    }

    /* Pressed Search */
    public void searchMessage(View view) {
        Intent intent = new Intent(this,SearchActivity.class);
        startActivityForResult(intent,SEARCH_ACTIVITY);
    }

    /* Advance through pictures */
    public void nextPicture(View view) {
        if(fileList.size() == fileIndex+1) return;
        fileIndex++;
        imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",fileList.get(fileIndex)));
        showImageAttribs();
    }

    public void previousPicture(View view) {
        if(fileIndex == 0) return;
        fileIndex--;
        imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",fileList.get(fileIndex)));
        showImageAttribs();
    }

    public void snapButton(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //Error, do nothing.
            }
            if(photoFile != null) {
                fileList.add(photoFile);
                fileIndex = fileList.size()-1;
                photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {

            imageView.setImageURI(photoURI);
            showImageAttribs();
        } else if(requestCode == SEARCH_ACTIVITY && resultCode == RESULT_OK) {
            tvLatLong = (TextView)findViewById(R.id.tvLatLong);
            tvLatLong.setText("SEARCH OK");
        }
    }

    private void showImageAttribs() {
        if(fileList.size() > 0) {
            tvLatLong.setText(getLatLong(fileList.get(fileIndex)));
            tvDateTime.setText(getDateTime(fileList.get(fileIndex)));
        }
    }

    private String getDateTime(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch(Exception e) {
            System.out.println("getDateTime Exception:" + e);
        }
        return "No Date Time";
    }

    private String getLatLong(File file) {
        float[] output = new float[2];
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            exif.getLatLong(output);
            String attrib = ExifInterface.TAG_GPS_LATITUDE;
            String latData = exif.getAttribute(attrib);
            attrib = ExifInterface.TAG_GPS_LONGITUDE;
            String longData = exif.getAttribute(attrib);
            return "LAT:" + latData + " LONG:" + longData;
        } catch(Exception e) {
            System.out.println("Exception:" + e);
        }
        return "Picture Taken"; //Just a placeholder until I get this to work
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        image.getParentFile().mkdirs();
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
