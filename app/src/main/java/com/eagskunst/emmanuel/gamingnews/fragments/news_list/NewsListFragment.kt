package com.eagskunst.emmanuel.gamingnews.fragments.news_list

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.adapter.NewsAdapter
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.di.DaggerNewsComponent
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.di.NewsModule
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListPresenter
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListView
import com.eagskunst.emmanuel.gamingnews.models.NewsModel
import com.eagskunst.emmanuel.gamingnews.receivers.SaveArticleReceiver
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import javax.inject.Inject

class NewsListFragment : Fragment(), NewsListView.View {

    private var mListener: OnFragmentInteractionListener? = null

    // Collection on views and objects that would be used globally
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var newsAdapter: NewsAdapter
    private val newsList: MutableList<NewsModel> = ArrayList()
    private lateinit var fab: FloatingActionButton
    private var ic_star: Bitmap? = null
    private var webViewOpen = false
    private lateinit var urls: Array<String>

    @Inject
    lateinit var presenter: NewsListPresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        initComponent()
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    private fun manageRefreshLayout() {
        if (tag != "NewsListFragment_Saved") {
            refreshLayout.setOnRefreshListener {
                getArticleList()
            }
        } else {
            refreshLayout.setOnRefreshListener {
                refreshLayout.isRefreshing = false
            }
        }
    }

    private fun manageRecyclerView() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = newsAdapter
        recyclerView.setHasFixedSize(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                    Log.d(TAG, "Entré para esconder" + fab.visibility)
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    Log.d(TAG, "Entré para mostrar" + fab.visibility)
                    fab.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private fun initComponent() {
        DaggerNewsComponent.builder()
            .newsModule(NewsModule(this))
            .build().inject(this@NewsListFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onCreateView(this)

        recyclerView = view.findViewById(R.id.recyclerview)
        refreshLayout = view.findViewById(R.id.refreshlayout)
        fab = view.findViewById(R.id.mainFAB)
        fab.setOnClickListener {
            recyclerView.smoothScrollToPosition(0)
        }

        newsAdapter = NewsAdapter(newsList, clickListener())
        urls = requireArguments().getStringArray("urls")!!

        manageRecyclerView()
        manageRefreshLayout()
        loadBitmaps()

        getArticleList()
    }

    override fun onResume() {
        super.onResume()
        if (webViewOpen) {
            if (tag == "NewsListFragment_Saved") {
                loadListFromSharedPreferences()
                webViewOpen = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(tag, "Handling result...")
        if (requestCode == REQUEST_RESULT) {
            Log.d(tag, "I entered!")
            if (resultCode == 1) {
                Log.d(TAG, "onActivityResult: enter the resultcode")
                loadListFromSharedPreferences()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString() + " must implement OnFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        newsList.clear()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (tag == "NewsListFragment_Saved" && !hidden) loadListFromSharedPreferences()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchView = menu.findItem(R.id.searchMenu).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return if (!refreshLayout.isRefreshing) {
                    newsAdapter.filter(s)
                    true
                } else {
                    false
                }
            }

            override fun onQueryTextChange(s: String): Boolean {
                return if (!refreshLayout.isRefreshing) {
                    newsAdapter.filter(s)
                    true
                } else {
                    false
                }
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getArticleList() {
        Log.d(TAG, "Enter articleList")
        if (tag == "NewsListFragment_Saved") {
            loadListFromSharedPreferences()
        } else {
            presenter.getArticles(urls)
            refreshLayout.isRefreshing = true
        }
    }

    override fun updateList(newsList: List<NewsModel>) {
        Log.d(TAG, "Updating list")
        this.newsList.addAll(newsList)
        newsAdapter.getNewsListCopy().addAll(newsList)
        requireActivity().runOnUiThread {
            refreshLayout.isRefreshing = false
            newsAdapter.notifyDataSetChanged()
        }
    }

    override fun checkInternetConnection(): Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }

    override fun createAlertDialog(message: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setNeutralButton("OK") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    override fun showToastError(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showToastError(message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun loadListFromSharedPreferences() {
        newsList.clear()
        newsAdapter.getNewsListCopy().clear()
        val savedList = SharedPreferencesLoader.retrieveList(
            requireActivity().getSharedPreferences("UserPreferences", 0)
        )
        try {
            newsList.addAll(savedList)
            newsAdapter.getNewsListCopy().addAll(savedList)
            newsAdapter.notifyDataSetChanged()
        } catch (e: NullPointerException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.error_retrieving, Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickListener(): NewsAdapter.NewsViewHolder.OnItemClickListener {
        return object : NewsAdapter.NewsViewHolder.OnItemClickListener {
            override fun OnItemClick(item: NewsModel) {
                // SaveArticle is a BroadcastReceiver that will be notified if the start button on the CustomTab is tapped
                // It is made this way because CustomTabs does not have an 'startTabForResult' or something like that.
                val i = Intent(activity, SaveArticleReceiver::class.java)
                i.putExtra("url", item.link)
                i.setExtrasClassLoader(NewsModel::class.java.classLoader) // For the BroadcastReceiver. Without it, it is not posible to use custom Objects
                val bundle = Bundle()
                bundle.putParcelable("Article", item)
                i.putExtra("Bundle", bundle)
                i.putExtra(
                    Intent.EXTRA_REFERRER,
                    Uri.parse("android-app://" + requireContext().packageName)
                )
                val b: Bitmap = ic_star ?: BitmapFactory.decodeResource(resources, R.drawable.ic_star_on)
                val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

                val builder = CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                if (SharedPreferencesLoader.currentTheme(sharedPreferences) == R.style.AppTheme)
                    builder.setToolbarColor(resources.getColor(R.color.colorPrimaryDark))
                else {
                    builder.setToolbarColor(resources.getColor(R.color.colorPrimaryText))
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    activity, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.setActionButton(b, "Save", pendingIntent, true)
                builder.setStartAnimations(requireActivity(), R.anim.slide_in_right, R.anim.slide_out_left)
                builder.setExitAnimations(requireActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                val customTab = builder.build()
                customTab.launchUrl(requireActivity(), Uri.parse(item.link))
                webViewOpen = true
            }
        }
    }

    private fun loadBitmaps() {
        val t = Thread {
            ic_star = BitmapFactory.decodeResource(resources, R.drawable.ic_star_on)
        }
        t.run()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private const val TAG = "NewsListFragment"
        private const val REQUEST_RESULT = 123

        @JvmStatic
        fun newInstance(urls: Array<String>): NewsListFragment {
            val args = Bundle()
            args.putStringArray("urls", urls)
            val fragment = NewsListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
