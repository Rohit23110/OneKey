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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private static final String LOG_TAG = SignUp.class.getSimpleName();
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mReenterPasswordField;
    private ProgressDialog progressDialog;
    private TextView tvPasswordStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.editText_email_id);
        mPasswordField = findViewById(R.id.editText_password);
        mPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Calculate password strength
                calculateStrength(editable.toString());
            }
        });
        tvPasswordStrength = (TextView) findViewById(R.id.text_show_strength);
        mReenterPasswordField = findViewById(R.id.editText_reenter_password);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while signing up");
    }
    private void calculateStrength(String passwordText) {
        int upperChars = 0, lowerChars = 0, numbers = 0, specialChars = 0, otherChars = 0, strengthPoints = 0;
        char c;

        int passwordLength = passwordText.length();

        if (passwordLength ==0)
        {
            tvPasswordStrength.setText(R.string.invalid);
            return;
        }

        //If password length is <= 5 set strengthPoints=1
        if (passwordLength <= 5) {
            strengthPoints =1;
        }
        //If password length is >5 and <= 10 set strengthPoints=2
        else if (passwordLength <= 10) {
            strengthPoints = 2;
        }
        //If password length is >10 set strengthPoints=3
        else
            strengthPoints = 3;
        // Loop through the characters of the password
        for (int i = 0; i < passwordLength; i++) {
            c = passwordText.charAt(i);
            // If password contains lowercase letters
            // then increase strengthPoints by 1
            if (c >= 'a' && c <= 'z') {
                if (lowerChars == 0) strengthPoints+= 1;
                lowerChars = 1;
            }
            // If password contains uppercase letters
            // then increase strengthPoints by 1
            else if (c >= 'A' && c <= 'Z') {
                if (upperChars == 0) strengthPoints+= 1;
                upperChars = 1;
            }
            // If password contains numbers
            // then increase strengthPoints by 1
            else if (c >= '0' && c <= '9') {
                if (numbers == 0) strengthPoints+= 1;
                numbers = 1;
            }
            // If password contains _ or @
            // then increase strengthPoints by 1
            else if (c == '_' || c == '@') {
                if (specialChars == 0) strengthPoints += 1;
                specialChars = 1;
            }
            // If password contains any other special chars
            // then increase strengthPoints by 1
            else {
                if (otherChars == 0) strengthPoints += 2;
                otherChars = 1;
            }
        }

        if (strengthPoints <= 3)
        {
            tvPasswordStrength.setText(R.string.weak);
        }
        else if (strengthPoints <= 6) {
            tvPasswordStrength.setText(R.string.medium);
        }
        else if (strengthPoints <= 9){
            tvPasswordStrength.setText(R.string.strong);
        }
    }

    public void signUp(View view) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        String re_enter_password = mReenterPasswordField.getText().toString();
        if (!validateForm()) {
            return;
        } else if(!password.equals(re_enter_password)) {
            Toast.makeText(SignUp.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            mPasswordField.getText().clear();
            mReenterPasswordField.getText().clear();
            return;
        }
        progressDialog.show();
        createAccount(email, password);
    }

    private void createAccount(String email, String password) {
        Log.d(LOG_TAG, "createAccount:" + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(isNetworkConnected(SignUp.this)) {
                            if (task.isSuccessful()) {
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task1) {
                                        if(task1.isSuccessful()) {
                                            Toast.makeText(SignUp.this, "Registered Succesfully. Please check your email for verification.",
                                                    Toast.LENGTH_SHORT).show();
                                            Log.d(LOG_TAG, "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user);
                                        }
                                    }
                                });
                                // Sign in success, update UI with the signed-in user's information


                            } //else {
//                                // If sign in fails, display a message to the user.
//                                Log.w(LOG_TAG, "createUserWithEmail:failure", task.getException());
//                                Toast.makeText(SignUp.this, "Authentication failed",
//                                        Toast.LENGTH_SHORT).show();
//                                updateUI(null);
//                          }
                        } else {
                            Toast.makeText(SignUp.this, "Not connected to the internet",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof FirebaseAuthWeakPasswordException) {
                    Toast.makeText(SignUp.this, "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                } else if(e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(SignUp.this, "Email address already in use",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                } else if(e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(SignUp.this, "Invalid Email Address",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String re_enter_password = mReenterPasswordField.getText().toString();
        if (TextUtils.isEmpty(re_enter_password)) {
            mReenterPasswordField.setError("Required.");
            valid = false;
        } else {
            mReenterPasswordField.setError(null);
        }

        return valid;
    }

    public void updateUI(FirebaseUser user) {
        progressDialog.dismiss();
        if (user != null) {
            finishAffinity();
            Intent intent = new Intent(this, Welcome.class);
            startActivity(intent);
        } else {
            mEmailField.getText().clear();
            mPasswordField.getText().clear();
            mReenterPasswordField.getText().clear();
        }
    }

    public boolean isNetworkConnected(Context context){
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
