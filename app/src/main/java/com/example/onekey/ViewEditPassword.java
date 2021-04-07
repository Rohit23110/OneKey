package com.example.onekey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.example.onekey.EncryptDecryptString.decrypt;
import static com.example.onekey.EncryptDecryptString.encrypt;
import android.view.Menu;
import android.view.MenuItem;


public class ViewEditPassword extends AppCompatActivity {

    private static final String LOG_TAG = "ViewEditPassword";
    private EditText mEditURL;
    private EditText mEditUsername;
    private TextInputEditText mEditPassword;
    private ProgressDialog progressDialog;
    private String Id;
    private Timestamp timestamp;
    private FirebaseAuth mAuth;
    private ImageButton btn_generate;
    private SeekBar seekBar;
    private TextView progressValue;
    private MenuItem item2;
    private TextView reusedpassword;
    private ArrayList<Password> mPassword = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_password);
        mEditURL = findViewById(R.id.editText_url);
        mEditUsername = findViewById(R.id.editText_username);
        mEditPassword = findViewById(R.id.editText_password);
        mAuth = FirebaseAuth.getInstance();
        btn_generate = findViewById(R.id.imageButton);
        progressValue=findViewById(R.id.seekbar_value);
        reusedpassword=findViewById(R.id.password_reuse);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("URL")
                .get(Source.CACHE)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                                Password password;
                                password = document.toObject(Password.class);
                                password.setPassword(decrypt((password.getPassword())));
                                mPassword.add(password);
                            }
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

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
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        mEditPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int reusecounter=0;
                Log.d(LOG_TAG, "After text changed");
                for(int i = 0;i<mPassword.size();i++)
                {
                    if(mEditPassword.getText().toString().equals(mPassword.get(i).getPassword()))
                    {
                        if(!Id.equals(mPassword.get(i).getId()))
                        {
                            reusecounter++;
                        }
                    }
                }
                if(reusecounter==1)
                {
                    reusedpassword.setText("This password has previously been used "+reusecounter+" time.");
                }
                else if(reusecounter>1)
                {
                    reusedpassword.setText("This password has previously been used "+reusecounter+" times.");
                }
                else
                {
                    reusedpassword.setText("");
                }
            }
        });
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = 12;
                seekBar.setVisibility(v.VISIBLE);
                progressValue.setVisibility(v.VISIBLE);
                mEditPassword.setText(GetPassword(length));
                String value = Integer.toString(mEditPassword.length());
                progressValue.setText(value);
            }
        });
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMin(6);
        seekBar.setMax(50);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress,
                                          boolean fromUser) {
                /*Toast.makeText(getApplicationContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT).show();*/
                mEditPassword.setText(GetPassword(progress));
                String value = Integer.toString(progress);
                progressValue.setText(value);
                btn_generate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int length = progress;

                        mEditPassword.setText(GetPassword(length));

                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                /*Toast.makeText(getApplicationContext(),"seekbar touch started!", Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /*Toast.makeText(getApplicationContext(),"seekbar touch stopped!", Toast.LENGTH_SHORT).show();*/
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setTitle("View Password");
        getMenuInflater().inflate(R.menu.menu, menu);
        item2 = menu.findItem(R.id.save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit) {
            mEditURL.setEnabled(true);
            mEditUsername.setEnabled(true);
            mEditPassword.setEnabled(true);
            btn_generate.setEnabled(true);
            seekBar.setEnabled(true);
            item.setVisible(false);
            item2.setVisible(true);
            getSupportActionBar().setTitle("Edit Password");
        } else if (id == R.id.delete) {
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
        } else if(id== R.id.save) {
            if (!validateForm()) {
                return false;
            }
            String url = encrypt(mEditURL.getText().toString());
            String username = encrypt(mEditUsername.getText().toString());
            String password = encrypt(mEditPassword.getText().toString());
            progressDialog.setMessage("Please wait while updating data");
            progressDialog.show();
            updateData(url, username, password);
        }
        else {
            final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setCancelable(true);
            builder2.setMessage("All unsaved changes will be lost. Continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder2.create();
            alertDialog.show();
        }
        return true;
    }

    /*public void onClickBack(View view) {
        if (!validateForm()) {
            return;
        }
        String url = encrypt(mEditURL.getText().toString());
        String username = encrypt(mEditUsername.getText().toString());
        String password = encrypt(mEditPassword.getText().toString());
        progressDialog.setMessage("Please wait while updating data");
        progressDialog.show();
        updateData(url, username, password);
    }*/
    public void onBackPressed() {
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setCancelable(true);
        builder2.setMessage("All unsaved changes will be lost. Continue?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder2.create();
        alertDialog.show();
    }

    public String GetPassword(int length){
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();

        Random rand = new Random();

        for(int i = 0; i < length; i++){
            char c = chars[rand.nextInt(chars.length)];
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }

    /*public void onClickEditPassword(View view) {
        view.setVisibility(View.INVISIBLE);
        mEditURL.setEnabled(true);
        mEditUsername.setEnabled(true);
        mEditPassword.setEnabled(true);
        btn_generate.setEnabled(true);
        seekBar.setEnabled(true);
        getSupportActionBar().setTitle("Edit Password");
    }*/

    public void onClickSavePassword(View view) {
        if (!validateForm()) {
            return;
        }
        String url = encrypt(mEditURL.getText().toString());
        String username = encrypt(mEditUsername.getText().toString());
        String password = encrypt(mEditPassword.getText().toString());
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

    /*public void onClickDeletePassword(View view) {
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
    }*/

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
