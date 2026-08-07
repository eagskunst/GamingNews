package com.eagskunst.emmanuel.gamingnews.fragments.releases

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.browser.customtabs.CustomTabsIntent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.adapter.ReleasesAdapter
import com.eagskunst.emmanuel.gamingnews.fragments.releases.di.DaggerReleasesComponent
import com.eagskunst.emmanuel.gamingnews.fragments.releases.di.ReleasesModule
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleasePresenter
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleaseView
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.eagskunst.emmanuel.gamingnews.utility.di.DaggerIgdbComponent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import javax.inject.Inject

class ReleasesFragment : Fragment(), ReleaseView.View, ReleasesAdapter.OnReleaseClickListener {

    private lateinit var rootLayout: CoordinatorLayout
    private lateinit var recyclerViewWrapper: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressWrapper: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var fab: FloatingActionButton

    private var releasesModelList: MutableList<ReleasesModel>? = null
    private lateinit var releasesAdapter: ReleasesAdapter

    private var tryAgain = false

    @Inject
    lateinit var presenter: ReleasePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        presenter.createView(this)
        releasesModelList = ArrayList()
    }

    private fun injectComponent() {
        DaggerReleasesComponent.builder()
            .igdbComponent(DaggerIgdbComponent.create())
            .releasesModule(ReleasesModule(this))
            .build().inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasesModelList = null
        presenter.destroyView()
    }

    override fun onCreateView(
        @NonNull inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_releases, container, false)

        rootLayout = v.findViewById(R.id.root_releases)
        recyclerViewWrapper = v.findViewById(R.id.recyclerview_wrapper)
        recyclerView = v.findViewById(R.id.rv_releases)
        progressWrapper = v.findViewById(R.id.progress_wrapper)
        progressBar = v.findViewById(R.id.progressBar)
        progressText = v.findViewById(R.id.progressText)
        fab = v.findViewById(R.id.releases_fab)

        releasesAdapter = ReleasesAdapter(releasesModelList!!, this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = releasesAdapter
        recyclerView.setHasFixedSize(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        fab.setOnClickListener { recyclerView.smoothScrollToPosition(0) }

        fillList()
        return v
    }

    override fun showToastError(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showToastError(message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun updateList(releasesList: List<ReleasesModel>) {
        if (tryAgain) {
            tryAgain = false
            (presenter.getReleasesList() as MutableList<ReleasesModel>).clear()
            getNewReleases()
        } else {
            releasesModelList!!.addAll(releasesList)
            releasesAdapter.notifyDataSetChanged()
            progressWrapper.visibility = View.GONE
            recyclerViewWrapper.visibility = View.VISIBLE
            Snackbar.make(rootLayout, R.string.success_releases, Snackbar.LENGTH_SHORT).show()
            saveReleasesInPreferences()
        }
    }

    override fun getNewReleases() {
        recyclerViewWrapper.visibility = View.GONE
        progressWrapper.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        presenter.getReleasesByPlatform()
    }

    override fun changeTextMessage(message: Int) {
        progressText.setText(message)
    }

    override fun setTryAgain() {
        tryAgain = true
    }

    private fun fillList() {
        val list = getReleasesFromPreferences()
        val savedMonth = SharedPreferencesLoader.getSavedMonth((activity as BaseActivity).getUserSharedPreferences())
        val actualMonth = Calendar.getInstance().get(Calendar.MONTH)
        if (list == null || savedMonth == -1 || actualMonth > savedMonth || (actualMonth == 0 && savedMonth == 11) || list.isEmpty()) {
            getNewReleases()
        } else {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            presenter.erasePassedDate(list, day)
            releasesModelList!!.addAll(list)
            releasesAdapter.notifyDataSetChanged()
            saveReleasesInPreferences()
        }
    }

    private fun saveReleasesInPreferences() {
        val editor = (activity as BaseActivity).getUserSharedPreferences().edit()
        SharedPreferencesLoader.saveReleasesList(editor, releasesModelList!!)
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        SharedPreferencesLoader.saveCurrentMonth(editor, month)
    }

    private fun getReleasesFromPreferences(): MutableList<ReleasesModel>? {
        val preferences = (activity as BaseActivity).getUserSharedPreferences()
        return SharedPreferencesLoader.retrieveReleasesList(preferences) as MutableList<ReleasesModel>?
    }

    override fun OnItemClick(release: ReleasesModel) {
        if (release.gameUrl == null) {
            showToastError(R.string.no_game_url)
        } else {
            val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

            val builder = CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
            if (SharedPreferencesLoader.currentTheme(sharedPreferences) == R.style.AppTheme)
                builder.setToolbarColor(resources.getColor(R.color.colorPrimaryDark))
            else {
                builder.setToolbarColor(resources.getColor(R.color.colorPrimaryText))
            }
            builder.setStartAnimations(requireActivity(), R.anim.slide_in_right, R.anim.slide_out_left)
            builder.setExitAnimations(requireActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            val customTab = builder.build()
            customTab.launchUrl(requireActivity(), Uri.parse(release.gameUrl))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ReleasesFragment {
            val fragment = ReleasesFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
