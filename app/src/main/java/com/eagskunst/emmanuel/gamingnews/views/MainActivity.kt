package com.eagskunst.emmanuel.gamingnews.views

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.NewsListFragment
import com.eagskunst.emmanuel.gamingnews.fragments.releases.ReleasesFragment
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.objects.LoadUrls
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException
import java.util.Locale

class MainActivity : BaseActivity(), NewsListFragment.OnFragmentInteractionListener {

    private lateinit var newsFragments: Array<NewsListFragment?>

    private lateinit var currentFrag: String
    private lateinit var drawerLayout: DrawerLayout
    private var loadUrls: LoadUrls? = null
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesLoader.setCanLoadImages(getUserSharedPreferences())

        setContentView(R.layout.activity_main)

        val isNightActive = getUserSharedPreferences().getBoolean("night_mode", false)

        if (isNightActive) {
            window.decorView.setBackgroundColor(resources.getColor(R.color.colorBackgroundNightMode))
        }

        // AdMob integration is disabled for now (see AndroidManifest.xml comment near where
        // the com.google.android.gms.ads.APPLICATION_ID meta-data used to be).

        setFirebaseToken()

        // In first launch, create saved list
        if (getUserSharedPreferences().getBoolean("first_launch", true)) {
            callLog(TAG, "First launch of this app in this device.")
            if (Locale.getDefault().language == "es") {
                FirebaseMessaging.getInstance().subscribeToTopic(Locale.getDefault().language)
            } else {
                FirebaseMessaging.getInstance().subscribeToTopic("en")
            }
            val spEditor: SharedPreferences.Editor = getUserSharedPreferences().edit()
            val savedNewsList: List<NewsModel> = ArrayList()
            val topicList: List<String> = ArrayList()
            SharedPreferencesLoader.saveList(spEditor, savedNewsList)
            SharedPreferencesLoader.saveTopics(spEditor, topicList)
            spEditor.putBoolean("first_launch", false).apply()
            spEditor.putBoolean("night_mode", false).apply()
            spEditor.putBoolean("load_images", true).apply()
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.navigation_view)
        val progressBar: ProgressBar = findViewById(R.id.toolbarProgressBar)
        showToolbar(toolbar, R.string.app_name, false, progressBar)
        callLog(TAG, "Title: " + supportActionBar!!.title.toString())
        startDrawerLayout(toolbar)

        loadUrls = null
        try {
            val stream = assets.open("Urls.json")
            val urls = LoadUrls(Locale.getDefault().language, stream)
            urls.setUrls()
            loadUrls = urls
        } catch (e: IOException) {
            e.printStackTrace()
        }
        newsFragments = arrayOfNulls(FragmentTags.size)
        startNavigationView()
        initAllFragments()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, newsFragments[0]!!, FragmentTags[0])
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        currentFrag = FragmentTags[0]
        navigationView.setCheckedItem(R.id.all_news)
        setOnBackChangeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferencesLoader.saveCurrentTime(getUserSharedPreferences().edit())
    }

    private fun startDrawerLayout(toolbar: Toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout)
        val drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout,
            toolbar, R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.drawerlogo1)
    }

    private fun startNavigationView() {
        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            drawerLayout.closeDrawers()
            if (id == R.id.all_news) {
                makeFragmentTransaction(newsFragments[0]!!, id, FragmentTags[0])
            } else if (id == R.id.ps4_news) {
                makeFragmentTransaction(newsFragments[1]!!, id, FragmentTags[1])
            } else if (id == R.id.xboxo_news) {
                makeFragmentTransaction(newsFragments[2]!!, id, FragmentTags[2])
            } else if (id == R.id.switch_news) {
                makeFragmentTransaction(newsFragments[3]!!, id, FragmentTags[3])
            } else if (id == R.id.PC_news) {
                makeFragmentTransaction(newsFragments[4]!!, id, FragmentTags[4])
            } else if (id == R.id.saved_news) {
                makeFragmentTransaction(newsFragments[5]!!, id, FragmentTags[5])
            } else if (id == R.id.next_releases) {
                val fragment = ReleasesFragment.newInstance()
                supportActionBar!!.title = "Coming soon games"
                makeFragmentTransaction(fragment, id, FragmentTags[6])
            } else if (id == R.id.settings) {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
            true
        })
    }

    private fun initAllFragments() {
        for (i in newsFragments.indices) {
            newsFragments[i] = NewsListFragment.newInstance(getUrls(i))
        }
    }

    private fun setOnBackChangeListener() {
        supportFragmentManager.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                val currentFragment = getCurrentFragment()
                val tag = currentFragment!!.tag
                if (tag == FragmentTags[6]) {
                    supportActionBar!!.setTitle(R.string.coming_soon)
                } else {
                    supportActionBar!!.setTitle(R.string.app_name)
                }
                if (tag == FragmentTags[0]) {
                    navigationView.setCheckedItem(R.id.all_news)
                } else if (tag == FragmentTags[1]) {
                    navigationView.setCheckedItem(R.id.ps4_news)
                } else if (tag == FragmentTags[2]) {
                    navigationView.setCheckedItem(R.id.xboxo_news)
                } else if (tag == FragmentTags[3]) {
                    navigationView.setCheckedItem(R.id.switch_news)
                } else if (tag == FragmentTags[4]) {
                    navigationView.setCheckedItem(R.id.PC_news)
                } else if (tag == FragmentTags[5]) {
                    navigationView.setCheckedItem(R.id.saved_news)
                } else if (tag == FragmentTags[6]) {
                    navigationView.setCheckedItem(R.id.next_releases)
                }
            }
        })
    }

    private fun getCurrentFragment(): Fragment? {
        return this.supportFragmentManager.findFragmentById(R.id.container)
    }

    private fun getUrls(i: Int): Array<String> {
        return when (i) {
            0 -> loadUrls!!.allUrls!!
            1 -> loadUrls!!.ps4Urls!!
            2 -> loadUrls!!.xboxOUrls!!
            3 -> loadUrls!!.switchUrls!!
            4 -> loadUrls!!.pcUrls!!
            5 -> arrayOf("SAVEDLIST")
            else -> arrayOf()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun makeFragmentTransaction(fragment: Fragment, item: Int, _TAG: String) {
        // Handler for the fade animation on the new fragment doesn't seem so abrupt.
        val h = Handler()
        h.postDelayed({
            if (currentFrag != _TAG) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, _TAG)
                    .addToBackStack(_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
                currentFrag = _TAG
            }
            navigationView.setCheckedItem(item)
        }, 200)
    }

    override fun onFragmentInteraction(uri: Uri) {
    }

    companion object {
        private const val TAG = "MainActivity"
        private val FragmentTags = arrayOf(
            "NewsListFragment_All", "NewsListFragment_PS4", "NewsListFragment_XboxO",
            "NewsListFragment_Switch", "NewsListFragment_PC", "NewsListFragment_Saved",
            "ReleasesFragment"
        )
    }
}
