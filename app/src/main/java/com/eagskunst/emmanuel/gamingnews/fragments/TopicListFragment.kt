package com.eagskunst.emmanuel.gamingnews.fragments

import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class TopicListFragment : Fragment() {

    private val TAG = this.javaClass.simpleName

    private lateinit var alertDialog: AlertDialog
    private lateinit var topicsAdapter: ArrayAdapter<String>
    private lateinit var topicList: MutableList<String>
    private lateinit var noTopics: TextView
    private lateinit var listView: ListView
    private lateinit var sharedPreferences: SharedPreferences

    // TODO: Add button to clean the list
    // TODO: Add illegalargumentexception for subscribeToTopic/unsuscribe. Add a Toast with "no add special characters" messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_topic_list, container, false)
        sharedPreferences = activity.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        setHasOptionsMenu(true)
        val retrievedTopics = SharedPreferencesLoader.retrieveTopics(sharedPreferences)
        if (retrievedTopics == null) {
            val spEditor = sharedPreferences.edit()
            topicList = ArrayList()
            SharedPreferencesLoader.saveTopics(spEditor, topicList)
        } else {
            topicList = retrievedTopics.toMutableList()
        }

        for (t in topicList) Log.d(TAG, "Topic: $t")
        noTopics = v.findViewById(R.id.tv_topiclist)
        listView = v.findViewById(R.id.topiclist_lv)
        setupListView()
        applyLayoutChanges(topicList.isEmpty())
        return v
    }

    private fun setupListView() {
        topicsAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, topicList)
        listView.adapter = topicsAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, i, _ ->
            val t = view.findViewById<TextView>(android.R.id.text1)
            createTopicEditionDialog(t.text.toString(), i)
            alertDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Adding the '+' button
        inflater.inflate(R.menu.image_button_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        /*
        Providing homeUpButton for this upper fragment so it can return to SettingsFragment
         */
        if (id == android.R.id.home) {
            if (activity.fragmentManager.backStackEntryCount >= 1) {
                try {
                    addTopicsToFirebase()
                    subscribeToTopics()
                    SharedPreferencesLoader.saveTopics(sharedPreferences.edit(), topicList)
                    fragmentManager.popBackStack()
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(activity, R.string.ilegalargumentfirebase, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
                return true
            }
            /*
            Providing topic addition
             */
            createTopicEditionDialog("create", 0)
            alertDialog.show()
            return true
        } else if (id == R.id.ig_button_add) {
            createTopicEditionDialog("create", 0)
            alertDialog.show()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun createTopicEditionDialog(text: String, position: Int) {
        val editText = EditText(activity)
        val builder = AlertDialog.Builder(activity)
        builder.setView(editText)
        builder.setTitle(R.string.add_topic)

        if (text.isNotEmpty() && text != "create") {
            editText.setText(text)
        }

        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        builder.setPositiveButton(R.string.add) { dialogInterface, _ ->
            if (text == "create") {
                topicList.add(editText.text.toString())
            } else if (editText.text.toString().isNotEmpty()) {
                topicList[position] = editText.text.toString()
                if (!text.equals(editText.text.toString(), ignoreCase = true)) {
                    try {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(text.replace(" ", "_").uppercase())
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(activity, R.string.ilegalargumentfirebase, Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            } else {
                topicList.removeAt(position)
                try {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(text.replace(" ", "_").uppercase())
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(activity, R.string.ilegalargumentfirebase, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            topicsAdapter.notifyDataSetChanged()
            dialogInterface.dismiss()
            applyLayoutChanges(topicList.isEmpty())
        }
        alertDialog = builder.create()
    }

    private fun addTopicsToFirebase() {
        val database = FirebaseDatabase.getInstance().reference
        database.child("topics").child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences)).removeValue()
        val userReference = database.child("topics")
            .child(SharedPreferencesLoader.getFirebaseToken(sharedPreferences)).child("subscribedList")
        for (i in topicList.indices) {
            userReference.child(i.toString()).setValue(topicList[i].replace(" ", "_").uppercase())
        }
    }

    private fun subscribeToTopics() {
        for (topic in topicList) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic.replace(" ", "_").uppercase())
        }
    }

    private fun applyLayoutChanges(isEmpty: Boolean) {
        if (isEmpty) {
            listView.visibility = View.GONE
            noTopics.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            noTopics.visibility = View.GONE
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (activity.fragmentManager.backStackEntryCount > 1) {
            addTopicsToFirebase()
            subscribeToTopics()
            SharedPreferencesLoader.saveTopics(sharedPreferences.edit(), topicList)
            fragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        try {
            addTopicsToFirebase()
            subscribeToTopics()
            SharedPreferencesLoader.saveTopics(sharedPreferences.edit(), topicList)
            fragmentManager.popBackStack()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(activity, R.string.ilegalargumentfirebase, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): TopicListFragment {
            return TopicListFragment()
        }
    }
}
