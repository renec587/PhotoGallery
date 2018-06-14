package com.example.photogallery;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static java.lang.Boolean.TRUE;

//TODO - Use Location services to insert EXIF data into each picture after it is taken.
//TODO - Search page has to return proper data (check for valid data), and we have to filter on those results.
public class MainActivity extends AppCompatActivity {

    Uri photoURI;
    Uri videoURI;
    private TextView mTextMessage;
    static final int REQUEST_IMAGE = 1;
    static final int SEARCH_ACTIVITY = 2;
    String mCurrentPhotoPath;
    ImageView imageView;
    TextView tvDateTime;
    TextView tvLatLong;
    EditText etCaption;
    DiskFiles fileManager;

    String DEFAULT_CAPTION = "Caption";
    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);

        fileManager = new DiskFiles(mContext);

        mTextMessage = findViewById(R.id.message);
        imageView = findViewById(R.id.imageView);
        tvLatLong = findViewById(R.id.tvLatLong);
        tvDateTime = findViewById(R.id.textDateTime);
        etCaption = findViewById(R.id.textCaption);
        etCaption.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                setCaption(etCaption.getText().toString(),fileManager.get());
            }
        });
        if(fileManager.size() >= 1) imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",fileManager.get()));
        showImageAttribs();
    }

    /* Pressed Search */
    public void searchMessage(View view) {
        Intent intent = new Intent(this,SearchActivity.class);
        startActivityForResult(intent,SEARCH_ACTIVITY);
    }

    /* Advance through pictures */
    public void nextPicture(View view) {
        if(fileManager.next() == 0) return;
        File nextFile = fileManager.get();
        if(nextFile == null) return;
        //TODO - I have to find out the type of the file (Look at the extension), and do a
        // Probably need to put the below image testing block into a separate function, because I call this]
        //Repeatedly throughout the application.
        if(false) { //FIXME - Change to if(typis=video) or something
          Bitmap foo =   ThumbnailUtils.createVideoThumbnail(nextFile.getPath(),MINI_KIND);
          imageView.setImageBitmap(foo);
        }
        System.out.println(nextFile.toString());
        imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",nextFile));
        showImageAttribs();
    }

    public void previousPicture(View view) {
        if(fileManager.previous() < 0) return;
        File file = fileManager.get();
        if (file == null) return;
        imageView.setImageURI(FileProvider.getUriForFile(this,"com.example.android.fileprovider",file));
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
                fileManager.save(photoFile);
                photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        }
    }

    public void videoButton(View view) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                //Do nothing
            }
            if(videoFile != null) {
                fileManager.save(videoFile);
                photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",videoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,videoURI);
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        }
    }

    public void playButton(View view) {
        //TODO - Get the file Uri we are viewing, and call playMedia
    }

    public void playMedia(Uri file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
       intent.setData(file);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        image.getParentFile().mkdirs();
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            imageView.setImageURI(photoURI);
            setCaption(DEFAULT_CAPTION,fileManager.get());
            showImageAttribs();
        } else if(requestCode == SEARCH_ACTIVITY && resultCode == RESULT_OK) {
            tvLatLong.setText("SEARCH OK");
            String firstDate = data.getStringExtra("STARTDATE");
            String secondDate = data.getStringExtra("ENDDATE");
            String keyWord = data.getStringExtra("KEYWORD");
            ArrayList keywords = new ArrayList<String>();
            if(!keyWord.isEmpty()) keywords.add(keyWord);
            fileManager.filter(keywords);
            fileManager.filterTime(firstDate,secondDate);
            File file = fileManager.get();
            if(file != null) {
                imageView.setImageURI(FileProvider.getUriForFile(this, "com.example.android.fileprovider", file));
            }
            showImageAttribs();
        }
    }

    private String getCaption(File imageFile) {
        try {
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            return exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        } catch (Exception e) {
            return "Caption Error";
        }
    }

    private void setCaption(String caption,File imageFile) {
        try {
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            String attrib = ExifInterface.TAG_IMAGE_DESCRIPTION;
            exif.setAttribute(attrib,caption);
            exif.saveAttributes();
        } catch (Exception e) {
            //Do nothing
        }
    }

    private void showImageAttribs() {
        if(fileManager.get() != null) {
            tvLatLong.setText(getLatLong(fileManager.get()));
            tvDateTime.setText(getDateTime(fileManager.get()));
            etCaption.setText(getCaption(fileManager.get()));
        }
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


    private String getDateTime(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch(Exception e) {
            System.out.println("getDateTime:" + e);
        }
        return "No Date Time";
    }
}