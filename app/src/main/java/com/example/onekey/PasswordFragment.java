package com.example.onekey;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import static com.example.onekey.EncryptDecryptString.decrypt;

public class PasswordFragment extends Fragment {

    private static final String LOG_TAG = "PasswordFragment";
    private ArrayList<Password> passwordArray = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private ProgressBar pb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("URL")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                                Password password = document.toObject(Password.class);
                                password.setUrl(decrypt(password.getUrl()));
                                password.setUsername(decrypt((password.getUsername())));
                                password.setPassword(decrypt((password.getPassword())));
                                passwordArray.add(password);
                            }
                            passwordArray.sort(Comparator.comparing(Password::getTimestamp));
                            updateUI();
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
//        Password p1 = new Password("www.gmail.com", "rohit23110", "12345678", new Timestamp(new Date()));
//        Password p2 = new Password("www.facebook.com", "sandesara.harsh", "216648386", new Timestamp(new Date()));
//        passwordArray.add(p1);
//        passwordArray.add(p2);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView: ");
        final View view = inflater.inflate(R.layout.fragment_password, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
        pb = view.findViewById(R.id.pbLoading);
        pb.setVisibility(ProgressBar.VISIBLE);
        return view;
    }

    private void updateUI() {
        pb.setVisibility(ProgressBar.INVISIBLE);
        PasswordRecyclerViewAdapter mAdapter = new PasswordRecyclerViewAdapter(passwordArray, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }
}
//    private void getPasswords() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
//                .collection("URL")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(LOG_TAG, document.getId() + " => " + document.getData());
//                                passwordArray.add(document.toObject(Password.class));
//                            }
//                        } else {
//                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
//    }



