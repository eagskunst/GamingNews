package com.eagskunst.emmanuel.gamingnews.utility

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

open class BaseActivity : AppCompatActivity() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val PREFERENCES_USER = "UserPreferences"
        sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE)
        val darkThemeActive = sharedPreferences!!.getBoolean("night_mode", false)
        val isSystemDarkThemeActive = isSystemDarkThemeActive()
        if (isSystemDarkThemeActive && !darkThemeActive) {
            sharedPreferences!!.edit().putBoolean("night_mode", true).commit()
        } else if (!isSystemDarkThemeActive && darkThemeActive) {
            sharedPreferences!!.edit().putBoolean("night_mode", false).commit()
        }
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences!!))
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferencesLoader.saveCurrentTime(getUserSharedPreferences().edit())
    }

    override fun onResume() {
        super.onResume()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.cancelAll()
            NotificationMaker.inboxStyle = NotificationCompat.InboxStyle()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    protected fun showToolbar(toolbar: Toolbar, title: Int, upButton: Boolean, progressBar: ProgressBar?) {
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(title)
        supportActionBar!!.setDisplayHomeAsUpEnabled(upButton)
        if (progressBar != null) {
            progressBar.visibility = View.GONE
            progressBar.isIndeterminate = false
        }
    }

    protected fun setFirebaseToken() {
        if (isPlayServicesAvailable()) {
            callLog(javaClass.simpleName, "Play services available!")
            val nm = NotificationMaker()
            nm.sharedPreferences = sharedPreferences
            nm.setToken()
        }
    }

    protected fun isPlayServicesAvailable(): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1)!!.show()
            return false
        }
        return true
    }

    protected fun isSystemDarkThemeActive(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    protected fun callLog(TAG: String, message: String) {
        Log.d(TAG, message)
    }

    fun setUserPreferences(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }

    fun getUserSharedPreferences(): SharedPreferences {
        return sharedPreferences!!
    }
}
