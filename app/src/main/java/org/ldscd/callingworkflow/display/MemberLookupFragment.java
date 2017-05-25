package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.MemberLookupAdapter;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.WebResources;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemberLookupFragment.memberLookupFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberLookupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberLookupFragment extends Fragment {
    @Inject
    DataManager dataManager;

    private memberLookupFragmentInteractionListener mListener;
    private ArrayList<Member> members;
    private View view;
    private MemberLookupAdapter adapter;

    public MemberLookupFragment() {

    }

    /**
     * @return A new instance of fragment memberLookupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberLookupFragment newInstance(String param1, String param2) {
        return new MemberLookupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the view. */
        view = inflater.inflate(R.layout.fragment_member__lookup_, container, false);
        /* Inflate and wireup the filter button. */
        wireUpFilterButton(view);
        /* Initialize the listview of members. */
        init();
        return view;
    }

    private void wireUpFilterButton(View v) {
        ImageButton filterButton = (ImageButton) v.findViewById(R.id.member_lookup_filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (memberLookupFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
    public interface memberLookupFragmentInteractionListener {
        void onFragmentInteraction(Member member);
    }

    private void init() {
        ListView listView = (ListView) view.findViewById(R.id.member_lookup_member_list);
        if(adapter == null) {
            setAdapter();
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Member selected = (Member) arg0.getAdapter().getItem(arg2);
                mListener.onFragmentInteraction(selected);
            }
        });
        EditText editText = (EditText)view.findViewById(R.id.member_lookup_search_box);
        editText.addTextChangedListener(textWatcherSearchListener);

        /*if(individualId > 0) {
            final ArrayList<Member> memberList = new ArrayList<>();
            dataManager.getWardList(new Response.Listener<List<Member>>() {
                @Override
                public void onResponse(List<Member> members) {
                    memberList.addAll(members);
                }
            });

            MemberLookupAdapter adapter = new MemberSearchAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, memberList);
            memberLookup = (AutoCompleteTextView) v.findViewById(R.id.autocomplete_calling_detail_member_lookup);
            memberLookup.setAdapter(adapter);
            memberLookup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Member selected = (Member) arg0.getAdapter().getItem(arg2);
                    mListener.onFragmentInteraction(selected.getIndividualId());
                }
            });
            final String name = memberData.getMemberName(individualId);
            if(name != null && name.length() > 0) {
                memberLookup.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        memberLookup.showDropDown();
                        memberLookup.setText(name);
                        memberLookup.setSelection(memberLookup.getText().length());
                    }
                }, 500);
            }*/
        //}
    }

    private final TextWatcher textWatcherSearchListener = new TextWatcher() {
        final android.os.Handler handler = new android.os.Handler();
        Runnable runnable;

        public void onTextChanged(final CharSequence s, int start, final int before, int count) {
            handler.removeCallbacks(runnable);
        }

        @Override
        public void afterTextChanged(final Editable s) {
            //show some progress, because you can access UI here
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(adapter == null) {
                       setAdapter();
                    }
                    adapter.getFilter().filter(s.toString());
                }
            };
            handler.postDelayed(runnable, 500);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    };

    public void wireUpIndividualInformationFragments(Long individualId) {
        if (individualId != null) {
            IndividualInformationFragment member_information_fragment = new IndividualInformationFragment();
            Bundle args = new Bundle();
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, individualId);
            member_information_fragment.setArguments(args);
            member_information_fragment.show(getFragmentManager(), null);
        }
    }

    private void setAdapter() {
        if(members == null || members.isEmpty()) {
            final MemberLookupFragment cFrag = this;
            setMembers(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    adapter = new MemberLookupAdapter(getContext(), cFrag, view.getId(), members);
                }
            });
        } else {
            adapter = new MemberLookupAdapter(getContext(), this, view.getId(), members);
        }
    }

    private void setMembers(final Response.Listener<Boolean> response) {
        dataManager.getWardList(new Response.Listener<List<Member>>() {
            @Override
            public void onResponse(List<Member> memberList) {
                if(members == null) {
                    members = new ArrayList<Member>();
                }
                members.addAll(memberList);
                response.onResponse(true);
            }
        });
    }
}