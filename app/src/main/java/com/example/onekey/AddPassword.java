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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.onekey.EncryptDecryptString.decrypt;
import static com.example.onekey.EncryptDecryptString.encrypt;

public class AddPassword extends AppCompatActivity {

    private static final String LOG_TAG = "AddPassword";
    private FirebaseAuth mAuth;
    private EditText mURLField;
    private EditText mUsernameField;
    private EditText mPasswordField;
    private ProgressDialog progressDialog;
    private ImageButton btn_generate;
    private SeekBar seekBar;
    private ArrayList<Password> mPassword = new ArrayList<>();
    private TextView progressValue;
    private MenuItem itemsave;
    private MenuItem itemedit;
    private TextView reusedpassword;
    private AutofillManager autofillManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);
        Log.d(LOG_TAG, "onCreate: ");

        mAuth = FirebaseAuth.getInstance();
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
        mURLField = findViewById(R.id.editText_url);
        mUsernameField = findViewById(R.id.editText_username);
        mPasswordField = findViewById(R.id.editText_password);
        btn_generate = findViewById(R.id.imageButton);
        progressValue=findViewById(R.id.seekbar_value);
        reusedpassword=findViewById(R.id.password_reuse);
        autofillManager = getSystemService(android.view.autofill.AutofillManager.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Adding password...");

        mPasswordField.addTextChangedListener(new TextWatcher() {
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
                    if(mPasswordField.getText().toString().equals(mPassword.get(i).getPassword()))
                    {
                        reusecounter++;
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
                mPasswordField.setText(GetPassword(length));
                String value = Integer.toString(mPasswordField.length());
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
                mPasswordField.setText(GetPassword(progress));
                String value = Integer.toString(progress);
                progressValue.setText(value);
                btn_generate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int length = progress;

                        mPasswordField.setText(GetPassword(length));

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
        setTitle("Add Password");
        getMenuInflater().inflate(R.menu.menu, menu);
        itemsave = menu.findItem(R.id.save);
        itemsave.setVisible(true);
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
        } else if(id== R.id.save) {
           if (!validateForm()) {
               return false;
           }
            String url = encrypt(mURLField.getText().toString());
            String username = encrypt(mUsernameField.getText().toString());
            String password = encrypt(mPasswordField.getText().toString());
            progressDialog.setMessage("Please wait while updating data");
            progressDialog.show();
            addPassword(url, username, password);
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
