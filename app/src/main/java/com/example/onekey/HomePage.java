package com.example.onekey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String LOG_TAG = "HomePage";
    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    public static final int ADD_PASSWORD = 1;
    private static final int ADD_NOTES = 2;
    public static final int EDIT_PASSWORD = 3;
    public static final int EDIT_NOTES = 4;
    private AutofillManager autofillManager;
    private FirebaseUser user;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView email = headerView.findViewById(R.id.text_email);
        email.setText(mAuth.getCurrentUser().getEmail());

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                final Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(currentFragment instanceof PasswordFragment) {
                    Intent intent = new Intent(HomePage.this, AddPassword.class);
                    startActivityForResult(intent, ADD_PASSWORD);
                } else if (currentFragment instanceof NotesFragment) {
                    Intent intent = new Intent(HomePage.this, AddNotes.class);
                    startActivityForResult(intent, ADD_NOTES);
                }
                //Toast.makeText(HomePage.this, "FAB Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PasswordFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_password);
            getSupportActionBar().setTitle("Passwords");
        }

        //Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .set(data, SetOptions.merge());

        snackbar = Snackbar.make(findViewById(android.R.id.content),
                "Default Autofill Service Not Set Up", Snackbar.LENGTH_INDEFINITE);
    }

//    public void onClickSignOut(View view) {
//        signOut();
//    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    public void updateUI(FirebaseUser user) {
        Intent intent = new Intent(this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage("Do you want to quit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_password:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PasswordFragment()).commit();
                getSupportActionBar().setTitle("Passwords");
                break;

            case R.id.nav_notes:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NotesFragment()).commit();
                getSupportActionBar().setTitle("Notes");
                break;

            case R.id.nav_account:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AccountFragment()).commit();
                getSupportActionBar().setTitle("Account");
                break;

            case R.id.nav_autofill_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_log_out:
                signOut();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        autofillManager = getSystemService(android.view.autofill.AutofillManager.class);
        if(!autofillManager.hasEnabledAutofillServices()) {
            Log.d(LOG_TAG, "onStart: Hi");
            snackbar.show();
        } else {
            Log.d(LOG_TAG, "onStart: Bye");
            snackbar.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PASSWORD || requestCode == EDIT_PASSWORD) {
            if (resultCode == RESULT_OK) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PasswordFragment()).commit();
            }
        } else if (requestCode == ADD_NOTES || requestCode == EDIT_NOTES) {
            if (resultCode == RESULT_OK) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new NotesFragment()).commit();
            }
        }
    }

    public void onClickResetPassword(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        String useremail = user.getEmail();
        builder.setMessage("Do you want to reset password for "+useremail+"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendPasswordReset();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void sendPasswordReset() {
        // [START send_password_reset]
        String email = user.getEmail();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Email sent.");
                            Toast.makeText(HomePage.this, "Please check your email for resetting password",
                                    Toast.LENGTH_SHORT).show();
                            updateUI2(user);
                        }
                    }
                });
    }

    public void updateUI2(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, SignedIn.class);
            startActivity(intent);
            finish();
        }
    }
}
