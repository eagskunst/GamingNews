package com.eagskunst.emmanuel.gamingnews.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.ListView;


import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
import com.eagskunst.emmanuel.gamingnews.views.MainActivity;
import com.eagskunst.emmanuel.gamingnews.views.SettingsActivity;


public class SettingsFragment extends PreferenceFragment {

    SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        sharedPreferences = getActivity().getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.preferences);
        final SwitchPreference nightmodePreference = (SwitchPreference) findPreference("pref_nightmode");
        nightmodePreference.setOnPreferenceClickListener(preferenceClickListener("night_mode",nightmodePreference.isChecked()));
        SwitchPreference disableImagesPreference = (SwitchPreference) findPreference("pref_loadimages");
        disableImagesPreference.setOnPreferenceClickListener(preferenceClickListener("load_images",disableImagesPreference.isChecked()));
        Preference manageTopics = findPreference("pref_managetopics");

        manageTopics.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((SettingsActivity)getActivity()).replaceFragment(TopicListFragment.newInstance(),R.string.manage_topics);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SettingsActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_activity_settings);
    }

    private void reloadApp() {
        Intent i = new Intent(getActivity(),MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private Preference.OnPreferenceClickListener preferenceClickListener(final String prefKey, final boolean isChecked){
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(isChecked)
                    sharedPreferences.edit().putBoolean(prefKey,false).apply();
                else
                    sharedPreferences.edit().putBoolean(prefKey,true).apply();
                if(prefKey.equals("night_mode"))
                    reloadApp();
                else if(prefKey.equals("load_images")){
                    SharedPreferencesLoader.setCanLoadImages(sharedPreferences);
                }
                return true;
            }
        };
    }


}
