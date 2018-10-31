package com.eagskunst.emmanuel.gamingnews.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Locale;


public class TopicListFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private OnFragmentInteractionListener mListener;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> topicsAdapter;
    private List<String> topicList;
    private TextView noTopics;
    private ListView listView;
    private SharedPreferences sharedPreferences;
    private boolean emptyList;

    public TopicListFragment() {
        // Required empty public constructor
    }

    //TODO: Add button to clean the list
    //TODO: Add illegalargumentexception for subscribeToTopic/unsuscribe. Add a Toast with "no add special characters" messages


    public static TopicListFragment newInstance() {
        return new TopicListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_topic_list, container, false);
        sharedPreferences = getActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        setHasOptionsMenu(true);
        topicList = SharedPreferencesLoader.retrieveTopics(sharedPreferences);
        for(String t:topicList)
            Log.d(TAG,"Topic: "+t);
        noTopics = v.findViewById(R.id.tv_topiclist);
        listView = v.findViewById(R.id.topiclist_lv);
        setupListView();
        applyLayoutChanges(topicList.isEmpty());
        return v;
    }

    private void setupListView() {
        topicsAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,topicList);
        listView.setAdapter(topicsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView t = view.findViewById(android.R.id.text1);
                createTopicEditionDialog(t.getText().toString(),i);
                alertDialog.show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //Adding the '+' button
        inflater.inflate(R.menu.image_button_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*
            Providing homeUpButton for this upper fragment so it can return to SettingsFragment
             */
            case android.R.id.home:
                if (getActivity().getFragmentManager().getBackStackEntryCount() >= 1) {
                    addTopicsToFirebase();
                    subscribeToTopics();
                    SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
                    getFragmentManager().popBackStack();
                    return true;
                }/*
                Providing topic addition
                 */
            case R.id.ig_button_add:
                createTopicEditionDialog("create",0);
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createTopicEditionDialog(final String text,final int position) {
        final EditText editText = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(editText);
        builder.setTitle(R.string.add_topic);

        if(!text.isEmpty() && !text.equals("create")){
            editText.setText(text);
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(text.equals("create")){
                    topicList.add(editText.getText().toString());
                }
                else if(!editText.getText().toString().isEmpty()){
                    topicList.set(position,editText.getText().toString());
                }
                else{
                    topicList.remove(position);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(text.replace(" ","_"));
                }
                topicsAdapter.notifyDataSetChanged();
                dialogInterface.dismiss();
                applyLayoutChanges(topicList.isEmpty());
            }
        });
        alertDialog = builder.create();
    }

    private void addTopicsToFirebase(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("topics").child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences)).removeValue();
        database.child("topics").child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences))
                .child("lang").setValue(Locale.getDefault().getLanguage());
        for(int i = 0;i<topicList.size();i++){
            database.child("topics").child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences))
                    .child("topic"+i).setValue(topicList.get(i).replace(" ","_"));
        }
    }

    private void subscribeToTopics(){
        for(String topic:topicList){
            FirebaseMessaging.getInstance().subscribeToTopic(topic.replace(" ","_"));
        }
    }

    private void applyLayoutChanges(boolean isEmpty){
        if(isEmpty){
            listView.setVisibility(View.GONE);
            noTopics.setVisibility(View.VISIBLE);

        }
        else{
            listView.setVisibility(View.VISIBLE);
            noTopics.setVisibility(View.GONE);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        if (getActivity().getFragmentManager().getBackStackEntryCount() > 1) {
            addTopicsToFirebase();
            subscribeToTopics();
            SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
            getFragmentManager().popBackStack();
        }
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        addTopicsToFirebase();
        subscribeToTopics();
        SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
        getFragmentManager().popBackStack();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
