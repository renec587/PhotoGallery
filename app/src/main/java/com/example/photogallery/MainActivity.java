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
import java.util.Date;

import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    Uri photoURI;
    private TextView mTextMessage;
    static final int REQUEST_IMAGE = 1;
    String mCurrentPhotoPath;
    ImageView imageView;
    TextView tvDateTime;
    TextView tvLatLong;

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
    }

    /* Pressed Search */
    public void searchMessage(View view) {
        Intent intent = new Intent(this,SearchActivity.class);
        startActivity(intent);
    }

    /* Advance through pictures */
    public void nextPicture(View view) {

    }

    public void previousPicture(View view) {

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
                photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageURI(photoURI);
            /* Display the date and location */
            String dateTime = new SimpleDateFormat("dd/yyyy/MM HH:mm:ss").format(new Date());
            tvDateTime = (TextView)findViewById(R.id.textDateTime);
            tvDateTime.setText(dateTime);
            tvLatLong = (TextView)findViewById(R.id.tvLatLong);
            tvLatLong.setText(getLatLong(photoURI));
        }
    }

    private String getLatLong(Uri filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath.getPath());
            String attrib = ExifInterface.TAG_GPS_LATITUDE;
            String latData = exif.getAttribute(attrib);
            attrib = ExifInterface.TAG_GPS_LONGITUDE;
            String longData = exif.getAttribute(attrib);
            return "LAT:" + latData + " LONG:" + longData;
        } catch(Exception e) {
            //Welp, that didn't work.
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
        //System.out.println(storageDir.toString());
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
