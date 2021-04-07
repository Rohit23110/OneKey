package com.example.onekey;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SET_DEFAULT = 1;
    private AutofillManager mAutofillManager;
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAutofillManager = getSystemService(AutofillManager.class);

        setupSettingsSwitch(R.id.settingsSetServiceContainer,
                R.id.settingsSetServiceLabel,
                R.id.settingsSetServiceSwitch,
                mAutofillManager.hasEnabledAutofillServices(),
                (compoundButton, serviceSet) -> setService(serviceSet));
    }

    private void setupSettingsSwitch(int containerId, int labelId, int switchId, boolean checked,
                                     CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        ViewGroup container = findViewById(containerId);
        String switchLabel = ((TextView) container.findViewById(labelId)).getText().toString();
        final Switch switchView = container.findViewById(switchId);
        switchView.setContentDescription(switchLabel);
        switchView.setChecked(checked);
        container.setOnClickListener((view) -> switchView.performClick());
        switchView.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setService(boolean enableService) {
        if (enableService) {
            startEnableService();
        } else {
            disableService();
        }
    }

    private void startEnableService() {
        if (mAutofillManager != null && !mAutofillManager.hasEnabledAutofillServices()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
            intent.setData(Uri.parse("package:com.example.onekey"));
            Log.d(TAG, "enableService(): ");
            //logd(TAG, "enableService(): intent=%s", intent);
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT);
        } else {
            Log.d(TAG, "Sample service already enabled.");
        }
    }

    private void disableService() {
        if (mAutofillManager != null && mAutofillManager.hasEnabledAutofillServices()) {
            mAutofillManager.disableAutofillServices();
            Snackbar.make(findViewById(R.id.settings_layout),
                    "Autofill service has been disabled.", Snackbar.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Sample service already disabled.");
        }
    }
}
