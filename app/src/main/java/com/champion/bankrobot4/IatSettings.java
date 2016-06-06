package com.champion.bankrobot4;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.champion.bankrobot4.utils.SettingTextWatcher;

import butterknife.ButterKnife;

public class IatSettings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    public static final String PREFER_NAME = "com.iflytek.setting";
    private EditTextPreference mVadbosPreference;
    private EditTextPreference mVadeosPreference;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
        addPreferencesFromResource(R.xml.iat_setting);

        mVadbosPreference = (EditTextPreference)findPreference("iat_vadbos_preference");
        mVadbosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(IatSettings.this,mVadbosPreference,0,10000));

        mVadeosPreference = (EditTextPreference)findPreference("iat_vadeos_preference");
        mVadeosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(IatSettings.this,mVadeosPreference,0,10000));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
