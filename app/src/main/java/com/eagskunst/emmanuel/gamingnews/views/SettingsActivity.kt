package com.eagskunst.emmanuel.gamingnews.views

import android.app.Fragment
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.fragments.SettingsFragment
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity

class SettingsActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar = findViewById(R.id.toolbar)
        showToolbar(toolbar, R.string.title_activity_settings, true, null)
        callLog(TAG, "Title: " + supportActionBar!!.title.toString())
        fragmentManager.beginTransaction()
            .replace(R.id.container_settings, SettingsFragment.newInstance())
            .commit()
        supportActionBar!!.setTitle(R.string.title_activity_settings)
    }

    fun replaceFragment(fragment: Fragment, title: Int) {
        val backStackName = fragment.javaClass.name
        val fm = fragmentManager
        val fragmentPopped = fm.popBackStackImmediate(backStackName, 0)
        if (!fragmentPopped) { // fragment not in back stack, create it.
            val ft = fm.beginTransaction()
            ft.replace(R.id.container_settings, fragment)
            ft.addToBackStack(backStackName)
            ft.commit()
            supportActionBar!!.setTitle(title)
        }
    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}
