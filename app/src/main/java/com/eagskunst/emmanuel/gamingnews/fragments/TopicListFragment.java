package com.eagskunst.emmanuel.gamingnews.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
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
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;


public class TopicListFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private AlertDialog alertDialog;
    private ArrayAdapter<String> topicsAdapter;
    private List<String> topicList;
    private TextView noTopics;
    private ListView listView;
    private SharedPreferences sharedPreferences;

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
        if(topicList == null){
            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            topicList = new ArrayList<>();
            SharedPreferencesLoader.saveTopics(spEditor,topicList);
        }

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
                    try{
                        addTopicsToFirebase();
                        subscribeToTopics();
                        SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
                        getFragmentManager().popBackStack();
                    }catch (IllegalArgumentException e){
                        Toast.makeText(getActivity(),R.string.ilegalargumentfirebase,Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
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
                    if(!text.equalsIgnoreCase(editText.getText().toString())){
                        try{
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(text.replace(" ","_").toUpperCase());
                        }catch(IllegalArgumentException e){
                            Toast.makeText(getActivity(),R.string.ilegalargumentfirebase,Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    topicList.remove(position);
                    try{
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(text.replace(" ","_").toUpperCase());
                    }catch(IllegalArgumentException e){
                        Toast.makeText(getActivity(),R.string.ilegalargumentfirebase,Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
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
        DatabaseReference userReference = database.child("topics")
                .child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences)).child("subscribedList");
        for(int i = 0;i<topicList.size();i++){
            userReference.child(Integer.toString(i)).setValue(topicList.get(i).replace(" ","_").toUpperCase());
        }
    }

    private void subscribeToTopics(){
        for(String topic:topicList){
            FirebaseMessaging.getInstance().subscribeToTopic(topic.replace(" ","_").toUpperCase());
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



    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity().getFragmentManager().getBackStackEntryCount() > 1) {
            addTopicsToFirebase();
            subscribeToTopics();
            SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try{
            addTopicsToFirebase();
            subscribeToTopics();
            SharedPreferencesLoader.saveTopics(sharedPreferences.edit(),topicList);
            getFragmentManager().popBackStack();
        }catch (IllegalArgumentException e){
            Toast.makeText(getActivity(),R.string.ilegalargumentfirebase,Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }
}
