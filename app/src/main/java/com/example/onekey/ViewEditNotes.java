package com.example.onekey;

import android.app.ActionBar;
import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import static com.example.onekey.EncryptDecryptString.encrypt;

public class ViewEditNotes extends AppCompatActivity {

    private static final String LOG_TAG = "ViewEditNotes";
    private EditText mEditTitle;
    private EditText mEditContent;
    private ProgressDialog progressDialog;
    private String Id;
    private Timestamp timestamp;
    private FirebaseAuth mAuth;
    MenuItem itemedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_notes);
        mEditTitle = findViewById(R.id.editText_editnotes_title);
        mEditContent = findViewById(R.id.editText_editnotes_content);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        Intent intent = getIntent();
        String Title = intent.getStringExtra("Title");
        String Content = intent.getStringExtra("Content");
        Date d = new Date();
        d.setTime(intent.getLongExtra("Timestamp", -1));
        timestamp = new Timestamp(d);
        Id = intent.getStringExtra("Id");
        mEditTitle.setText(Title);
        mEditContent.setText(Content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setTitle("Edit Note");
        getMenuInflater().inflate(R.menu.menu, menu);
        itemedit = menu.findItem(R.id.edit);
        itemedit.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                    .collection("Notes").document(Id);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage("Do you really want to delete this data? This cannot be undone.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog.setMessage("Please wait while deleting data");
                            progressDialog.show();
                            if (isNetworkConnected(ViewEditNotes.this)) {
                                newPassRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                                                Toast.makeText(ViewEditNotes.this, "Data updated successfully",
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
                                Toast.makeText(ViewEditNotes.this, "Not connected to the internet",
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
        } else {
            String title = encrypt(mEditTitle.getText().toString());
            String content = encrypt(mEditContent.getText().toString());

            progressDialog.setMessage("Please wait while updating data");
            progressDialog.show();
            updateData(title, content);
        }
        return true;
    }

    public void onClickSaveNotes(View view) {
        String title = encrypt(mEditTitle.getText().toString());
        String content = encrypt(mEditContent.getText().toString());

        progressDialog.setMessage("Please wait while updating data");
        progressDialog.show();
        updateData(title, content);
    }

    private void updateData(String title, String content) {
        Notes data = new Notes(title, content, timestamp);
        data.setId(Id);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("Notes").document(Id);

        if (isNetworkConnected(ViewEditNotes.this)) {
            newPassRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                            Toast.makeText(ViewEditNotes.this, "Data updated successfully",
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
            Toast.makeText(ViewEditNotes.this, "Not connected to the internet",
                    Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
    }

//    public void onClickDeleteNotes(View view) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
//                .collection("Notes").document(Id);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(true);
//        builder.setMessage("Do you really want to delete this data? This cannot be undone.")
//                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        progressDialog.setMessage("Please wait while deleting data");
//                        progressDialog.show();
//                        if (isNetworkConnected(ViewEditNotes.this)) {
//                            newPassRef.delete()
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
//                                            Toast.makeText(ViewEditNotes.this, "Data updated successfully",
//                                                    Toast.LENGTH_SHORT).show();
//                                            updateUI(true);
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.w(LOG_TAG, "Error writing document", e);
//                                        }
//                                    });
//                        } else {
//                            Toast.makeText(ViewEditNotes.this, "Not connected to the internet",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(false);
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }

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

    @Override
    public void onBackPressed() {
        String title = encrypt(mEditTitle.getText().toString());
        String content = encrypt(mEditContent.getText().toString());
        progressDialog.setMessage("Please wait while updating data");
        progressDialog.show();
        updateData(title, content);
    }
}

