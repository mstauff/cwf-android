package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.MemberLookupAdapter;
import org.ldscd.callingworkflow.model.FilterOption;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemberLookupFragment.memberLookupFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberLookupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberLookupFragment extends Fragment implements MemberLookupFilterFragment.OnFragmentInteractionListener {
    /* Fields. */
    public static String FRAG_NAME = "MEMBER_LOOKUP_FRAGMENT";
    @Inject
    DataManager dataManager;

    private memberLookupFragmentInteractionListener mListener;
    private ArrayList<Member> members;
    private View view;
    private MemberLookupAdapter adapter;
    private ListView listView;

    /* Popup items. */
    private FilterOption filterOption;

    /* Constructor(s). */
    public MemberLookupFragment() { }

    /**
     * @return A new instance of fragment memberLookupFragment.
     */
    public static MemberLookupFragment newInstance(String param1, String param2) {
        return new MemberLookupFragment();
    }

    /* Methods. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate the view. */
        view = inflater.inflate(R.layout.fragment_member__lookup_, container, false);
        /* Inflate and wireup the filter button. */
        wireUpFilterButton(view);
        /* Initialize the listview of members. */
        init();
        return view;
    }

    private void wireUpFilterButton(View v) {
        final ImageButton filterButton = (ImageButton) v.findViewById(R.id.member_lookup_filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filterOption == null) {
                    filterOption = new FilterOption(true);
                }
                MemberLookupFilterFragment memberLookupFilterFragment =
                        MemberLookupFilterFragment.newInstance(filterOption);
                Bundle args = new Bundle();
                if(filterOption != null) {
                    args.putBooleanArray(MemberLookupFilterFragment.NUMBER_OF_CALLINGS, filterOption.getNumberCallings());
                    args.putDouble(MemberLookupFilterFragment.TIME_IN_CALLING, filterOption.getTimeInCalling());
                    args.putBoolean(MemberLookupFilterFragment.HIGH_PRIEST, filterOption.isHighPriest());
                    args.putBoolean(MemberLookupFilterFragment.ELDERS, filterOption.isElders());
                    args.putBoolean(MemberLookupFilterFragment.PRIESTS, filterOption.isPriests());
                    args.putBoolean(MemberLookupFilterFragment.TEACHERS, filterOption.isTeachers());
                    args.putBoolean(MemberLookupFilterFragment.DEACONS, filterOption.isDeacons());
                    args.putBoolean(MemberLookupFilterFragment.RELIEF_SOCIETY, filterOption.isReliefSociety());
                    args.putBoolean(MemberLookupFilterFragment.LAUREL, filterOption.isLaurel());
                    args.putBoolean(MemberLookupFilterFragment.MIA_MAID, filterOption.isMiaMaid());
                    args.putBoolean(MemberLookupFilterFragment.BEEHIVE, filterOption.isBeehive());
                    args.putBoolean(MemberLookupFilterFragment.TWELVE_EIGHTEEN, filterOption.isTwelveEighteen());
                    args.putBoolean(MemberLookupFilterFragment.EIGHTEEN_PLUS, filterOption.isEighteenPlus());
                    args.putBoolean(MemberLookupFilterFragment.MALE, filterOption.isMale());
                    args.putBoolean(MemberLookupFilterFragment.FEMALE, filterOption.isFemale());
                    memberLookupFilterFragment.setArguments(args);
                }
                memberLookupFilterFragment.show(getFragmentManager(), null);
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

    @Override
    public void onFragmentInteraction(FilterOption filterOption) {
        this.filterOption = filterOption == null ? new FilterOption(true) : filterOption;
        setAdapter();
        this.listView.setAdapter(adapter);
        //adapter.notifyDataSetChanged();
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
        listView = (ListView) view.findViewById(R.id.member_lookup_member_list);
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

    private void createAdapter() {
        final MemberLookupFragment cFrag = this;
        if(filterOption == null) {
            filterOption = new FilterOption(true);
        }

        ArrayList<Member> filterMembers = filterOption.filterMembers(members);
        adapter = new MemberLookupAdapter(getContext(), cFrag, view.getId(), filterMembers);
    }

    private void setAdapter() {
        if(members == null || members.isEmpty()) {
            setMembers(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean response) {
                    createAdapter();
                }
            });
        } else {
            createAdapter();
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