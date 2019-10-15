package com.example.onekey;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;

import static com.example.onekey.EncryptDecryptString.decrypt;

public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";
    private ArrayList<Notes> notesArray = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("Notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Notes notes;
                                notes = document.toObject(Notes.class);
                                notes.setTitle(decrypt(notes.getTitle()));
                                notes.setContent(decrypt((notes.getContent())));
                                notesArray.add(notes);
                            }
                            notesArray.sort(Comparator.comparing(Notes::getTimestamp).reversed());
                            updateUI();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
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
        Log.d(TAG, "onCreateView: ");
        final View view = inflater.inflate(R.layout.fragment_notes, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview2);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
        return view;
    }

    private void updateUI() {
        NotesRecyclerViewAdapter mAdapter = new NotesRecyclerViewAdapter(notesArray, getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

}
