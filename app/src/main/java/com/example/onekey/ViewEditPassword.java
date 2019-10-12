package com.example.onekey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ViewEditPassword extends AppCompatActivity {

    private static final String LOG_TAG = "ViewEditPassword";
    private EditText mEditURL;
    private EditText mEditUsername;
    private TextInputEditText mEditPassword;
    private ProgressDialog progressDialog;
    private String Id;
    private Timestamp timestamp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_password);
        mEditURL = findViewById(R.id.editText_url);
        mEditUsername = findViewById(R.id.editText_username);
        mEditPassword = findViewById(R.id.editText_password);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        Intent intent = getIntent();
        String URL = intent.getStringExtra("URL");
        String Username = intent.getStringExtra("Username");
        String Password = intent.getStringExtra("Password");
        Date d = new Date();
        d.setTime(intent.getLongExtra("Timestamp", -1));
        timestamp = new Timestamp(d);
        Id = intent.getStringExtra("Id");
        mEditURL.setText(URL);
        mEditUsername.setText(Username);
        mEditPassword.setText(Password);
    }

    public void onClickEditPassword(View view) {
        view.setVisibility(View.INVISIBLE);
        mEditURL.setEnabled(true);
        mEditUsername.setEnabled(true);
        mEditPassword.setEnabled(true);
        getSupportActionBar().setTitle("Edit Password");
    }

    public void onClickSavePassword(View view) {
        if (!validateForm()) {
            return;
        }
        String url = mEditURL.getText().toString();
        String username = mEditUsername.getText().toString();
        String password = mEditPassword.getText().toString();
        progressDialog.setMessage("Please wait while updating data");
        progressDialog.show();
        updateData(url, username, password);
    }

    private void updateData(String URL, String username, String password) {
        Password data = new Password(URL, username, password, timestamp);
        data.setId(Id);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("URL").document(Id);

        if (isNetworkConnected(ViewEditPassword.this)) {
            newPassRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                            Toast.makeText(ViewEditPassword.this, "Data updated successfully",
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
            Toast.makeText(ViewEditPassword.this, "Not connected to the internet",
                    Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
    }

    public void onClickDeletePassword(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("URL").document(Id);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage("Do you really want to delete this data? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.setMessage("Please wait while deleting data");
                        progressDialog.show();
                        if (isNetworkConnected(ViewEditPassword.this)) {
                            newPassRef.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                                            Toast.makeText(ViewEditPassword.this, "Data updated successfully",
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
                            Toast.makeText(ViewEditPassword.this, "Not connected to the internet",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(false);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean validateForm() {
        boolean valid = true;

        String url = mEditURL.getText().toString();
        if (TextUtils.isEmpty(url)) {
            mEditURL.setError("Required.");
            valid = false;
        } else {
            mEditURL.setError(null);
        }

        String username = mEditUsername.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mEditUsername.setError("Required.");
            valid = false;
        } else {
            mEditUsername.setError(null);
        }

        String password = mEditPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mEditPassword.setError("Required.");
            valid = false;
        } else {
            mEditPassword.setError(null);
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
