package com.example.onekey;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import android.util.Log;

public class Splash extends AppCompatActivity {

    private static final String LOG_TAG = Splash.class.getSimpleName();
    private Handler mHandler;
    private Runnable mRunnable;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView text = findViewById(R.id.text_one_key);
        ImageView image = findViewById(R.id.one_key_logo);
        Animation animation1 =
                AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade);
        image.startAnimation(animation1);
        text.startAnimation(animation1);
        final Intent intent = new Intent(this, Welcome.class);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        };

        mHandler = new Handler();

        mHandler.postDelayed(mRunnable, 2000);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart: ");
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void updateUI(FirebaseUser user) {
        Log.d(LOG_TAG, "updateUI: ");
        if(user != null) {
            if(user.isEmailVerified()) {
                Intent intent = new Intent(this, SignedIn.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, Welcome.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop: ");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        if(mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

}
