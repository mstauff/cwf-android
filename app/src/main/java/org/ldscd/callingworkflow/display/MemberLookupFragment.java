package org.ldscd.callingworkflow.display;

import android.content.Context;
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
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.display.adapters.MemberLookupAdapter;
import org.ldscd.callingworkflow.model.FilterOption;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.ldscd.callingworkflow.display.CallingDetailFragment.CAN_VIEW_PRIESTHOOD;
import static org.ldscd.callingworkflow.display.CallingDetailFragment.INDIVIDUAL_ID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMemberLookupFragmentListener} interface
 * to handle interaction events.
 */
public class MemberLookupFragment extends Fragment implements MemberLookupFilterFragment.OnMemberLookupFilterListener {
    /* Fields. */
    public static String FRAG_NAME = "MEMBER_LOOKUP_FRAGMENT";
    public static String positionTypeIdName = "POSITION_TYPE_ID";
    public Integer positionTypeId;
    @Inject
    DataManager dataManager;

    private OnMemberLookupFragmentListener mListener;
    private ArrayList<Member> members;
    private View view;
    private MemberLookupAdapter adapter;
    private ListView listView;
    private Member currentSelection;
    private boolean firstTime;
    private boolean canViewPriesthoodFilter;

    /* Popup items. */
    private FilterOption filterOption;

    /* Constructor(s). */
    public MemberLookupFragment() { }

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
        Bundle bundle = this.getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            /* Get permission to view priesthood filter */
            canViewPriesthoodFilter = bundle.getBoolean(CAN_VIEW_PRIESTHOOD, false);
            /* Get the positionTypeId */
            positionTypeId = bundle.getInt(positionTypeIdName, 0);
            firstTime = true;
        }
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
                createMemberLookupFilterFragment(filterOption);
            }
        });
    }

    public void createMemberLookupFilterFragment(FilterOption filterOption) {
        MemberLookupFilterFragment memberLookupFilterFragment = MemberLookupFilterFragment.newInstance(filterOption);
        memberLookupFilterFragment.setMemberLookupFilterListener(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CAN_VIEW_PRIESTHOOD, canViewPriesthoodFilter);
        memberLookupFilterFragment.setArguments(bundle);
        memberLookupFilterFragment.show(getFragmentManager(), MemberLookupFragment.FRAG_NAME);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /* Comes from LookupFilterFragment. */
    public void OnFilterOptionsChangedListener(FilterOption filterOption) {
        this.filterOption = filterOption == null ? new FilterOption(true) : filterOption;
        setAdapter();
        this.listView.setAdapter(adapter);
    }

    public void setMemberLookupListener(OnMemberLookupFragmentListener listener) {
        mListener = listener;
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
    public interface OnMemberLookupFragmentListener {
        void onMemberLookupFragmentInteraction(Member member);
    }

    private void init() {
        TableRow removeSelection = (TableRow) view.findViewById(R.id.member_lookup_clear_selection_button_container);
        final TextView currentSelectionLabel = (TextView) view.findViewById(R.id.member_lookup_current_selection_label);
        Bundle bundle = this.getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            Long individualId = bundle.getLong(INDIVIDUAL_ID, 0);
            if(individualId > 0) {
                currentSelection = dataManager.getMember(individualId);
                if(currentSelection != null) {
                    currentSelectionLabel.setText(currentSelection.getFormattedName());
                    removeSelection.setVisibility(View.VISIBLE);
                }
            } else {
                currentSelectionLabel.setText("");
                removeSelection.setVisibility(View.GONE);
            }
        }

        removeSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectionLabel.setText("");
                mListener.onMemberLookupFragmentInteraction(null);
                getFragmentManager().popBackStack();
            }
        });
        listView = (ListView) view.findViewById(R.id.member_lookup_member_list);
        if(adapter == null) {
            setAdapter();
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Member selected = (Member) arg0.getAdapter().getItem(arg2);
                mListener.onMemberLookupFragmentInteraction(selected);
                getFragmentManager().popBackStack();
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
        if(positionTypeId != null && positionTypeId > 0) {
            /* Create pre-set filter options */
            PositionMetaData positionMetaData = dataManager.getPositionMetadata(positionTypeId);
            if(positionMetaData != null) {
                filterOption.setFilterOptions(positionMetaData, firstTime);
                firstTime = false;
            }
        }

        List<Member> filterMembers = filterOption.filterMembers(members);
        adapter = new MemberLookupAdapter(getContext(), cFrag, view.getId(), filterMembers, dataManager);
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
                    members = new ArrayList<>();
                }
                members.addAll(memberList);
                response.onResponse(true);
            }
        });
    }
}