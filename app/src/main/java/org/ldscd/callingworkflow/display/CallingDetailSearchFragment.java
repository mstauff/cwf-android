package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import com.android.volley.Response;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.MemberSearchAdapter;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.MemberData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


/**
 *
 * Activities that contain this fragment must implement the
 * {@link CallingDetailSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CallingDetailSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallingDetailSearchFragment extends android.support.v4.app.Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String INDIVIDUAL_ID = "individualId";
    private long individualId;
    private View v;
    ListView list;
    MemberSearchAdapter adapter;
    SearchView memberSearch;
    List<Member> members;
    AutoCompleteTextView memberLookup;

    @Inject
    DataManager dataManager;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param individualId Parameter 1.
     * @return A new instance of fragment CallingDetailSearchFragment.
     */
    public static CallingDetailSearchFragment newInstance(long individualId) {
        CallingDetailSearchFragment fragment = new CallingDetailSearchFragment();
        Bundle args = new Bundle();
        args.putLong(INDIVIDUAL_ID, individualId);
        fragment.setArguments(args);
        return fragment;
    }

    public CallingDetailSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment. */
        v = inflater.inflate(R.layout.fragment_calling_detail_search, container, false);
        init();
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Long individualId);
    }

    private void init() {
        if (getArguments() != null) {
            individualId = getArguments().getLong(INDIVIDUAL_ID);
        }
        if(individualId > 0) {
            final ArrayList<Member> memberList = new ArrayList<>();
            dataManager.getWardList(new Response.Listener<List<Member>>() {
                @Override
                public void onResponse(List<Member> members) {
                    memberList.addAll(members);
                }
            });

            adapter = new MemberSearchAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, memberList);
            memberLookup = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_calling_detail_member_lookup);
            memberLookup.setAdapter(adapter);
            memberLookup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Member selected = (Member) arg0.getAdapter().getItem(arg2);
                    mListener.onFragmentInteraction(selected.getIndividualId());
                }
            });
            final String name = dataManager.getMemberName(individualId);
            if(name != null && name.length() > 0) {
                memberLookup.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        memberLookup.showDropDown();
                        memberLookup.setText(name);
                        memberLookup.setSelection(memberLookup.getText().length());
                    }
                }, 500);
            }
        }
    }
}