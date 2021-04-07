package com.example.onekey;

import android.app.assist.AssistStructure;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.AUTOFILL_HINT_PASSWORD;
import static android.view.View.AUTOFILL_HINT_USERNAME;
import static com.example.onekey.EncryptDecryptString.decrypt;

public class MyAutofillService extends AutofillService {
    private static final String TAG = "MyAutofillService";
    private AutofillId usernameAutofillId;
    private AutofillId passwordAutofillId;
    private ArrayList<Password> passwordArray = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Override
    public void onFillRequest(@NonNull FillRequest fillRequest, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback fillCallback) {
        Log.d(TAG, "onFillRequest:");
        List<FillContext> context = fillRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        traverseStructure(structure);

        if (usernameAutofillId != null && passwordAutofillId != null) {
            Source source = Source.CACHE;
            mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                        .collection("URL")
                        .get(source)
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        Password password = document.toObject(Password.class);
                                        password.setUrl(decrypt(password.getUrl()));
                                        password.setUsername(decrypt((password.getUsername())));
                                        password.setPassword(decrypt((password.getPassword())));
                                        passwordArray.add(password);
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                Log.d(TAG, "onFillRequest: Autofill");
                ParsedStructure parsedStructure = new ParsedStructure(usernameAutofillId, passwordAutofillId);

                //UserData userData = fetchUserData(parsedStructure);

                if (!passwordArray.isEmpty()) {
                    Log.d(TAG, "onFillRequest: Hello");
                    FillResponse.Builder fillResponse = new FillResponse.Builder();

                    for (int i = 0; i < passwordArray.size(); i++) {
                        RemoteViews usernamePresentation = new RemoteViews(getPackageName(), R.layout.test_autofill_menu_item);
                        usernamePresentation.setImageViewResource(R.id.autofill_icon, R.drawable.password_icon_black);
                        usernamePresentation.setTextViewText(R.id.autofill_text,
                                " " + passwordArray.get(i).getUrl() + "\n" + " " + passwordArray.get(i).getUsername());

                        fillResponse.addDataset(new Dataset.Builder()
                                .setValue(parsedStructure.usernameId,
                                        AutofillValue.forText(passwordArray.get(i).getUsername()), usernamePresentation)
                                .setValue(parsedStructure.passwordId,
                                        AutofillValue.forText(passwordArray.get(i).getPassword()), usernamePresentation)
                                .build());
                    }

                    AutofillId[] autofillIds = new AutofillId[]{usernameAutofillId, passwordAutofillId};
                    SaveInfo saveInfo = new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC, autofillIds).build();

                    if(saveInfo != null) {
                        Log.d(TAG, "onFillRequest: SaveInfo");
                        fillResponse.setSaveInfo(saveInfo);
                        Log.d(TAG, "onFillRequest: " + usernameAutofillId.toString() + " " + passwordAutofillId.toString() + " " + saveInfo.toString());
                        Log.d(TAG, "onFillRequest: " + autofillIds[0].toString() + " " + autofillIds[1].toString());
                    }

                    fillCallback.onSuccess(fillResponse.build());
                }
            }

        }
    }

    class ParsedStructure {
        AutofillId usernameId;
        AutofillId passwordId;

        public ParsedStructure(AutofillId usernameId, AutofillId passwordId) {
            this.usernameId = usernameId;
            this.passwordId = passwordId;
        }
    }

    class UserData {
        String username;
        String password;
    }

    public void traverseStructure(AssistStructure structure) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            traverseNode(viewNode);
        }
    }

    public void traverseNode(AssistStructure.ViewNode viewNode) {
        if (viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
            String[] autofill = viewNode.getAutofillHints();
            for (String s : autofill) {
                if (s.equals(AUTOFILL_HINT_USERNAME)) {
                    usernameAutofillId = viewNode.getAutofillId();
                }
                if (s.equals(AUTOFILL_HINT_PASSWORD)) {
                    passwordAutofillId = viewNode.getAutofillId();
                }
            }
        } else {
            String s = viewNode.getHint();
            if (s != null) {
                if (s.contains("Email") || s.contains("email") || s.contains("Username") || s.contains("username")) {
                    usernameAutofillId = viewNode.getAutofillId();
                }
                if (s.contains("Password") || s.contains("password")) {
                    passwordAutofillId = viewNode.getAutofillId();
                }
            }
        }

        for (int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            traverseNode(childNode);
        }
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest saveRequest, @NonNull SaveCallback saveCallback) {
        Log.d(TAG, "onSaveRequest: ");
        List<FillContext> context = saveRequest.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        traverseStructure(structure);

        saveCallback.onSuccess();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onFillRequest: onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onFillRequest: onDisconnected");
    }
}
