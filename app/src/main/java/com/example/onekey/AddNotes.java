package com.example.onekey;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class AddNotes extends AppCompatActivity {
    private static final String LOG_TAG = "AddNotes";
    private EditText mTitle;
    private EditText mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        Log.d(LOG_TAG, "onCreate: ");
        mTitle = findViewById(R.id.editText_notes_title);
        mContent = findViewById(R.id.editText_notes_content);
    }

    public void onClickAddNotes(View view) {
        String title = mTitle.getText().toString();
        String content = mContent.getText().toString();
        Toast.makeText(AddNotes.this, "Title: " + title + " Content: " + content, Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
