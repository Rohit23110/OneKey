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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

import static com.example.onekey.EncryptDecryptString.encrypt;

public class AddNotes extends AppCompatActivity {
    private static final String LOG_TAG = "AddNotes";
    private FirebaseAuth mAuth;
    private EditText mTitle;
    private EditText mContent;
    private ProgressDialog progressDialog;
    MenuItem itemedit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        Log.d(LOG_TAG, "onCreate: ");

        mAuth = FirebaseAuth.getInstance();
        mTitle = findViewById(R.id.editText_notes_title);
        mContent = findViewById(R.id.editText_notes_content);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Adding note...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setTitle("Add Note");
        getMenuInflater().inflate(R.menu.menu, menu);
        itemedit = menu.findItem(R.id.edit);
        itemedit.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage("Do you really want to delete this data? This cannot be undone.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
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
            String title = encrypt(mTitle.getText().toString());
            String content = encrypt(mContent.getText().toString());
            if(TextUtils.isEmpty(mTitle.getText().toString()) && TextUtils.isEmpty(mContent.getText().toString())) {
                Toast.makeText(AddNotes.this, "Empty Note Discarded", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            } else {
                progressDialog.setMessage("Please wait while updating data");
                progressDialog.show();
                addNotes(title, content);
            }
        }
        return true;
    }

    public void onClickAddNotes(View view) {
        String title = encrypt(mTitle.getText().toString());
        String content = encrypt(mContent.getText().toString());
//        Toast.makeText(AddPassword.this, "URL: "+URL+" Username: "+username+
//                " Password: "+password, Toast.LENGTH_SHORT).show();
        progressDialog.show();
        addNotes(title, content);
    }

    private void addNotes(String title, String content) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp date = new Timestamp(new Date());

        Notes data = new Notes(title, content, date);
        DocumentReference newPassRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("Notes").document();

        data.setId(newPassRef.getId());
        if (isNetworkConnected(AddNotes.this)) {
            newPassRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                            Toast.makeText(AddNotes.this, "Note added successfully",
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
            Toast.makeText(AddNotes.this, "Not connected to the internet",
                    Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
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

    @Override
    public void onBackPressed() {
        String title = encrypt(mTitle.getText().toString());
        String content = encrypt(mContent.getText().toString());
        if(TextUtils.isEmpty(mTitle.getText().toString()) && TextUtils.isEmpty(mContent.getText().toString())) {
            Toast.makeText(AddNotes.this, "Empty Note Discarded", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        } else {
            progressDialog.setMessage("Please wait while updating data");
            progressDialog.show();
            addNotes(title, content);
        }
    }
}
