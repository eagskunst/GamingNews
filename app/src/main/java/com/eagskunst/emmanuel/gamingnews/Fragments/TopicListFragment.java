package com.eagskunst.emmanuel.gamingnews.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;

import com.eagskunst.emmanuel.gamingnews.R;


public class TopicListFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private OnFragmentInteractionListener mListener;
    private AlertDialog alertDialog;

    public TopicListFragment() {
        // Required empty public constructor
    }


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
        setHasOptionsMenu(true);
        return v;
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
                if (getActivity().getFragmentManager().getBackStackEntryCount() > 1) {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return true;
                /*
                Providing topic addition
                 */
            case R.id.ig_button_add:
                createTopicEditionDialog();
                alertDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createTopicEditionDialog() {
        final EditText editText = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(editText);
        builder.setTitle(R.string.add_topic);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG,editText.getText().toString());
                dialogInterface.dismiss();
            }
        });
        alertDialog = builder.create();
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
            getFragmentManager().popBackStack();
        }
        mListener = null;
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
