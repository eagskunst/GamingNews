package com.eagskunst.emmanuel.gamingnews.Fragments;

import android.annotation.TargetApi;
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
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.Adapter.NewsAdapter;
import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.ParserMaker;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
import com.eagskunst.emmanuel.gamingnews.receivers.SaveArticleReceiver;
import com.eagskunst.emmanuel.gamingnews.views.WebViewActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;


public class NewsListFragment extends Fragment{

    private static final String TAG = "NewsListFragment";
    private static final int REQUEST_RESULT = 123;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter newsAdapter;
    private List<NewsModel> newsList = new ArrayList<>();
    private ParserMaker parserMaker;
    private FloatingActionButton fab;
    private OnFragmentInteractionListener mListener;
    private Bitmap ic_star;
    private boolean webViewOpen = false;

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
        View view = inflater.inflate(R.layout.fragment_news_list,container,false);

        AdView adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);

        String[] urls;
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
        manageRecyclerView(true,true);

        ParserMaker.OnNewsFinishListener newsFinishListener = new ParserMaker.OnNewsFinishListener() {
            @Override
            public void onNewsFinish(final boolean newItems) {
                if(getActivity() != null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(newItems){
                                newsAdapter.notifyDataSetChanged();
                                newsAdapter.getNewsListCopy().addAll(newsList);
                            }
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
                parserMaker.setRunning(false);
            }
        };

        parserMaker = new ParserMaker(newsFinishListener,urls,
                Toast.makeText(getActivity(), R.string.cant_get_articles, Toast.LENGTH_SHORT),
                this.newsAdapter,this.newsList);

        //if want to load the saved list
        Log.d(TAG,"TAG: "+getTag());
        if(getTag().equals("NewsListFragment_Saved")){
            loadListFromSharedPreferences();
        }
        else if(newsList.isEmpty()){
            parserMaker.create();
            refreshLayout.setRefreshing(true);
        }

        manageRefreshLayout(parserMaker);
        loadBitmaps();
        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
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

    @Override
    public void onResume() {
        super.onResume();
        if(webViewOpen){
            if(getTag().equals("NewsListFragment_Saved")){
                loadListFromSharedPreferences();
            }
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
        //super.onActivityResult(requestCode,resultCode,data);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchMenu).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!parserMaker.isRunning()){
                    newsAdapter.filter(s);
                    return true;
                }
                else{
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!parserMaker.isRunning()){
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

    private void manageRecyclerView(boolean autoMeasure, boolean fixedSize) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(autoMeasure);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setHasFixedSize(fixedSize);

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
            /*
            hide() and show() are two methods provided by the FAB to hide/show the FAB button with a smooth animation.
            dy is a value that changes when you scroll vertically, when the user scrolls down the value is positive
            and when the user scrolls up the value is negative.
            So we check if the FAB is visible and the value is positive(i.e. user is scrolling down)
            we will hide it and if the FAB is hidden and the value is negative(i.e. user is scrolling up) we will show the FAB.
             */
        });
    }

    private void loadListFromSharedPreferences() {
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

    private void manageRefreshLayout(final ParserMaker parserMaker) {
        if(!getTag().equals("NewsListFragment_Saved")){
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(isNetworkAvailable() && !parserMaker.isRunning()) {
                        refreshLayout.setRefreshing(true);
                        parserMaker.create();
                    }
                    else if(!isNetworkAvailable()){
                        AlertDialog dialog = generateDialog(R.string.check_your_connection);
                        dialog.show();
                        refreshLayout.setRefreshing(false);
                    }
                    else if(parserMaker.isRunning()){
                        AlertDialog dialog = generateDialog(R.string.still_refreshing);
                        dialog.show();
                        refreshLayout.setRefreshing(false);
                    }
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

    private void loadBitmaps(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ic_star = BitmapFactory.decodeResource(getResources(),R.drawable.ic_star_on);
            }
        });
        t.run();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private AlertDialog generateDialog(int message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }


    private NewsAdapter.NewsViewHolder.OnItemClickListener clickListener() {
        return new NewsAdapter.NewsViewHolder.OnItemClickListener() {
            @Override
            public void OnItemClick(NewsModel item) {
                Intent i = new Intent(getActivity(),SaveArticleReceiver.class);
                i.putExtra("url",item.getLink());
                i.setExtrasClassLoader(NewsModel.class.getClassLoader());
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



    public ParserMaker getParserMaker() {
        return parserMaker;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
