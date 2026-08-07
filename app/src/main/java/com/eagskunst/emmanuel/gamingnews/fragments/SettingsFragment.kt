package com.eagskunst.emmanuel.gamingnews.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.util.Log
import android.widget.Toast
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.eagskunst.emmanuel.gamingnews.views.MainActivity
import com.eagskunst.emmanuel.gamingnews.views.SettingsActivity
import com.google.firebase.messaging.FirebaseMessaging

class SettingsFragment : PreferenceFragment() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val PREFERENCES_USER = "UserPreferences"
        sharedPreferences = activity.getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE)

        addPreferencesFromResource(R.xml.preferences)
        val nightmodePreference = findPreference("pref_nightmode") as SwitchPreference
        nightmodePreference.onPreferenceClickListener = preferenceClickListener("night_mode", nightmodePreference.isChecked)
        val disableImagesPreference = findPreference("pref_loadimages") as SwitchPreference
        disableImagesPreference.onPreferenceClickListener = preferenceClickListener("load_images", disableImagesPreference.isChecked)
        val dailyReminderPreference = findPreference("pref_dailynotf") as CheckBoxPreference
        dailyReminderPreference.onPreferenceClickListener = preferenceClickListener("daily_notf", dailyReminderPreference.isChecked)
        val manageTopics = findPreference("pref_managetopics")

        manageTopics.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            (activity as SettingsActivity).replaceFragment(TopicListFragment.newInstance(), R.string.manage_topics)
            true
        }

        val deleteSavedArticles = findPreference("pref_deletesaved")
        deleteSavedArticles.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sp = activity.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
            SharedPreferencesLoader.saveList(sp.edit(), ArrayList())
            Toast.makeText(activity, R.string.saved_list_deleted, Toast.LENGTH_LONG).show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as SettingsActivity).supportActionBar!!.setTitle(R.string.title_activity_settings)
    }

    private fun reloadApp() {
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun preferenceClickListener(prefKey: String, isChecked: Boolean): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            if (prefKey == "daily_notf") {
                val dailyNotf = sharedPreferences.getBoolean(prefKey, false)
                if (!dailyNotf) {
                    Log.d(this.javaClass.simpleName, "onPreferenceClick: subscribed")
                    FirebaseMessaging.getInstance().subscribeToTopic("all")
                    sharedPreferences.edit().putBoolean(prefKey, true).commit()
                } else {
                    Log.d(this.javaClass.simpleName, "onPreferenceClick: unsubscribed")
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("all")
                    sharedPreferences.edit().putBoolean(prefKey, false).commit()
                }
            } else {
                if (isChecked) sharedPreferences.edit().putBoolean(prefKey, false).apply()
                else sharedPreferences.edit().putBoolean(prefKey, true).apply()
                if (prefKey == "night_mode") reloadApp()
                else if (prefKey == "load_images") {
                    SharedPreferencesLoader.setCanLoadImages(sharedPreferences)
                }
            }
            true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
