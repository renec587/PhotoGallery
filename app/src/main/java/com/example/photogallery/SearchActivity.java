package com.example.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void goButton(View view) {
        Intent resultIntent = new Intent();
        //TODO - Put some putExtra key/value pairs into the resultIntent
        setResult(Activity.RESULT_OK,resultIntent);
        finish();
    }
}
