package com.example.onekey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.onekey.EncryptDecryptString.encrypt;

public class AddPassword extends AppCompatActivity {

    private static final String LOG_TAG = "AddPassword";
    private FirebaseAuth mAuth;
    private EditText mURLField;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);
        Log.d(LOG_TAG, "onCreate: ");

        mAuth = FirebaseAuth.getInstance();
        mURLField = findViewById(R.id.editText_url);
        mUsernameField = findViewById(R.id.editText_username);
        mPasswordField = findViewById(R.id.editText_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Adding password...");
    }

    public void onClickAddPassword(View view) {
        if (!validateForm()) {
            return;
        }
        String URL = encrypt(mURLField.getText().toString());
        String username = encrypt(mUsernameField.getText().toString());
        String password = encrypt(mPasswordField.getText().toString());
//        Toast.makeText(AddPassword.this, "URL: "+URL+" Username: "+username+
//                " Password: "+password, Toast.LENGTH_SHORT).show();
        progressDialog.show();
        addPassword(URL, username, password);
    }

    private void addPassword(String URL, String username, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp date = new Timestamp(new Date());

        Password data = new Password(URL, username, password, date);
        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("URL").document();

        data.setId(newPassRef.getId());
        if (isNetworkConnected(AddPassword.this)) {
            newPassRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                            Toast.makeText(AddPassword.this, "Password added successfully",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(true);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error writing document", e);
                        }
                    });
        } else {
            Toast.makeText(AddPassword.this, "Not connected to the internet",
                    Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String url = mURLField.getText().toString();
        if (TextUtils.isEmpty(url)) {
            mURLField.setError("Required.");
            valid = false;
        } else {
            mURLField.setError(null);
        }

        String username = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mUsernameField.setError("Required.");
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    public void updateUI(boolean check) {
        progressDialog.dismiss();
        if(check) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public boolean isNetworkConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();

                if (ni != null) {
                    return (ni.isConnected() && (ni.getType() == ConnectivityManager.TYPE_WIFI || ni.getType() == ConnectivityManager.TYPE_MOBILE));
                }
            } else {
                final Network n = cm.getActiveNetwork();

                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

                    return (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                }
            }
        }

        return false;
    }
}
