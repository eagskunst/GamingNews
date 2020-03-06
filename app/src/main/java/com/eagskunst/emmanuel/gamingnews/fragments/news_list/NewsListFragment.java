package com.eagskunst.emmanuel.gamingnews.fragments.news_list;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.adapter.NewsAdapter;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.di.DaggerNewsComponent;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.di.NewsModule;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListPresenter;
import com.eagskunst.emmanuel.gamingnews.fragments.news_list.mvp.NewsListView;
import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader;
import com.eagskunst.emmanuel.gamingnews.receivers.SaveArticleReceiver;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class NewsListFragment extends Fragment implements NewsListView.View {

    private static final String TAG = "NewsListFragment";
    private static final int REQUEST_RESULT = 123;
    private OnFragmentInteractionListener mListener;

    //Collection on views and objects that would be used globally
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter newsAdapter;
    private List<NewsModel> newsList = new ArrayList<>();
    private FloatingActionButton fab;
    private Bitmap ic_star;
    private boolean webViewOpen = false;
    private String[] urls;

    @Inject
    NewsListPresenter presenter;

    public NewsListFragment() {
        // Required empty public constructor
    }

    public static NewsListFragment newInstance(String[] urls) {
        Bundle args = new Bundle();
        args.putStringArray("urls", urls);
        NewsListFragment fragment = new NewsListFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initComponent();
        return inflater.inflate(R.layout.fragment_news_list,container,false);
    }

    private void createdAd(View view) {
        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
    }


    private void manageRefreshLayout() {
        if(!getTag().equals("NewsListFragment_Saved")){
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getArticleList();
                }
            });
        }
        else{
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private void manageRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0 && fab.getVisibility() == View.VISIBLE){
                    fab.hide();
                    Log.d(TAG,"Entré para esconder"+fab.getVisibility());
                }
                else if(dy < 0 && fab.getVisibility() != View.VISIBLE){
                    Log.d(TAG,"Entré para mostrar"+fab.getVisibility());
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void initComponent() {
        DaggerNewsComponent.builder()
                .newsModule(new NewsModule(this))
                .build().inject(NewsListFragment.this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.onCreateView(this);

        createdAd(view);
        recyclerView = view.findViewById(R.id.recyclerview);
        refreshLayout = view.findViewById(R.id.refreshlayout);
        fab = view.findViewById(R.id.mainFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        newsAdapter = new NewsAdapter(newsList, clickListener());
        urls = getArguments().getStringArray("urls");

        manageRecyclerView();
        manageRefreshLayout();
        loadBitmaps();

        getArticleList();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(webViewOpen){
            if(getTag().equals("NewsListFragment_Saved")){
                loadListFromSharedPreferences();
                webViewOpen = false;
            }
        }
        changeAdViewVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        changeAdViewVisibility(View.GONE);
    }

    private void changeAdViewVisibility(final int visibility){
        Log.d(TAG, "Changing ad visibility to: "+visibility);
        final View fragmentView = getView();
        if(fragmentView != null){
            final AdView adView = fragmentView.findViewById(R.id.adView);
            adView.setVisibility(visibility);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getTag(),"Handling result...");
        if(requestCode == REQUEST_RESULT){
            Log.d(getTag(),"I entered!");
            if(resultCode == 1){
                Log.d(TAG, "onActivityResult: enter the resultcode");
                loadListFromSharedPreferences();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(newsList!=null){
            newsList.clear();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(getTag().equals("NewsListFragment_Saved") && !hidden)
            loadListFromSharedPreferences();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchMenu).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!refreshLayout.isRefreshing()){
                    newsAdapter.filter(s);
                    return true;
                }
                else{
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!refreshLayout.isRefreshing()){
                    newsAdapter.filter(s);
                    return true;
                }
                else{
                    return false;
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void getArticleList(){
        Log.d(TAG,"Enter articleList");
        if(getTag().equals("NewsListFragment_Saved")){
            loadListFromSharedPreferences();
        }
        else{
            presenter.getArticles(urls);
            refreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void updateList(List<NewsModel> newsList) {
        Log.d(TAG, "Updating list");
        this.newsList.addAll(newsList);
        newsAdapter.getNewsListCopy().addAll(newsList);
        getActivity().runOnUiThread(() -> {
                refreshLayout.setRefreshing(false);
                newsAdapter.notifyDataSetChanged();
            }
        );
    }

    @Override
    public boolean checkInternetConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void createAlertDialog(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void showToastError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToastError(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadListFromSharedPreferences() {
        newsList.clear();
        newsAdapter.getNewsListCopy().clear();
        List<NewsModel> savedList = SharedPreferencesLoader.retrieveList(getActivity()
                .getSharedPreferences("UserPreferences",0));
        try{
            newsList.addAll(savedList);
            newsAdapter.getNewsListCopy().addAll(savedList);
            newsAdapter.notifyDataSetChanged();
        }catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.error_retrieving, Toast.LENGTH_SHORT).show();
        }
    }

    private NewsAdapter.NewsViewHolder.OnItemClickListener clickListener() {
        return new NewsAdapter.NewsViewHolder.OnItemClickListener() {
            @Override
            public void OnItemClick(NewsModel item) {
                //SaveArticle is a BroadcastReceiver that will be notified if the start button on the CustomTab is tapped
                //It is made this way because CustomTabs does not have an 'startTabForResult' or something like that.
                Intent i = new Intent(getActivity(),SaveArticleReceiver.class);
                i.putExtra("url",item.getLink());
                i.setExtrasClassLoader(NewsModel.class.getClassLoader());//For the BroadcastReceiver. Without it, it is not posible to use custom Objects
                Bundle bundle = new Bundle();
                bundle.putParcelable("Article",item);
                i.putExtra("Bundle",bundle);
                i.putExtra(Intent.EXTRA_REFERRER,
                        Uri.parse("android-app://" + getContext().getPackageName()));
                Bitmap b;
                if(ic_star != null){
                    b = ic_star;
                }
                else{
                    b = BitmapFactory.decodeResource(getResources(),R.drawable.ic_star_on);
                }
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                        .addDefaultShareMenuItem();
                if(SharedPreferencesLoader.currentTheme(sharedPreferences) == R.style.AppTheme)
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                else{
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryText));
                }
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,i,PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setActionButton(b,"Save",pendingIntent,true);
                builder.setStartAnimations(getActivity(),R.anim.slide_in_right,R.anim.slide_out_left);
                builder.setExitAnimations(getActivity(),android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                CustomTabsIntent customTab = builder.build();
                customTab.launchUrl(getActivity(),Uri.parse(item.getLink()));
                webViewOpen = true;
            }
        };
    }

    private void loadBitmaps(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ic_star = BitmapFactory.decodeResource(getResources(),R.drawable.ic_star_on);
            }
        });
        t.run();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
