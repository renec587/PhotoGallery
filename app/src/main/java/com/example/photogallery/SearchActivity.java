package com.example.photogallery;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity {
    EditText etDate1;
    EditText etDate2;
    EditText etKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        etDate1 = (EditText) findViewById(R.id.etDate1);
        etDate2 = (EditText) findViewById(R.id.etDate2);
        etKeyword = (EditText) findViewById(R.id.etKeyword);
    }

    public void goButton(View view) {
        Intent resultIntent = new Intent();
        //TODO - Put some putExtra key/value pairs into the resultIntent
        resultIntent.putExtra("STARTDATE",etDate1.getText().toString());
        resultIntent.putExtra("ENDDATE",etDate2.getText().toString());
        resultIntent.putExtra("KEYWORD",etKeyword.getText().toString());
        setResult(Activity.RESULT_OK,resultIntent);
        finish();
    }
}
