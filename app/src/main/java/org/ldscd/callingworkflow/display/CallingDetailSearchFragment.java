package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.volley.Response;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.MemberSearchAdapter;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.IWebResources;
import org.ldscd.callingworkflow.web.LocalFileResources;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View v;
    ListView list;
    MemberSearchAdapter adapter;
    SearchView memberSearch;
    List<Member> members;
    @Inject
    IWebResources webResources;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CallingDetailSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CallingDetailSearchFragment newInstance(String param1, String param2) {
        CallingDetailSearchFragment fragment = new CallingDetailSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_calling_detail_search, container, false);
        init();
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*@Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }*/

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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void init() {
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if(mParam1 == null) {
            final ArrayList<Member> memberList = new ArrayList<>();
            webResources.getWardList(new Response.Listener<List<Member>>() {
                @Override
                public void onResponse(List<Member> members) {
                    memberList.addAll(members);
                }
            });

            adapter = new MemberSearchAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, memberList);
            AutoCompleteTextView memberLookup = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_calling_detail_member_lookup);
            memberLookup.setAdapter(adapter);
            /*final ListView listView = (ListView) v.findViewById(R.id.member_search_list);
            listView.setTextFilterEnabled(true);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setClickable(true);
            listView.setAdapter(adapter);

            final SearchView searchView = (SearchView) v.findViewById(R.id.calling_detail_search_view);
            searchView.setIconifiedByDefault(false);
            searchView.setSubmitButtonEnabled(false);
            searchView.setQueryHint("Member Lookup");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if(searchView.hasFocus()) {
                        listView.setVisibility(View.VISIBLE);
                    }
                    adapter.filter(newText);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String newText){
                    return false;
                }
            });
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(listView != null) {
                        if (hasFocus && listView.getChildAt(0) != null) {
                            listView.setVisibility(View.VISIBLE);
                            ViewGroup.LayoutParams params = listView.getLayoutParams();
                            params.height = 3 * listView.getChildAt(0).getMeasuredHeight();
                            listView.setLayoutParams(params);
                        } else {
                            listView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    searchView.setQuery(adapter.getItem(position).getFormattedName(), true);
                    listView.setVisibility(View.INVISIBLE);
                }
            });*/
        }
    }
}