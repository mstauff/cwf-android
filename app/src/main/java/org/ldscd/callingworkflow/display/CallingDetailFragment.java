package org.ldscd.callingworkflow.display;

import android.content.Context;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;


import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * A fragment representing a single Calling detail screen.
 * This fragment is either contained in a {@link ExpandableOrgsListActivity}
 * in two-pane mode (on tablets) or a {@link CallingDetailActivity}
 * on handsets.
 */
public class CallingDetailFragment extends Fragment {
    @Inject
    DataManager dataManager;
    Spinner statusDropdown;
    TextView notes;
    public static final String ORG_ID = "orgId";
    public static final String CALLING_ID = "calling_id";
    public static final String INDIVIDUAL_ID = "individualId";

    private static long orgId;
    private Member member;
    private Long individualId;
    private Calling calling;
    private Org org;
    private View view;
    private OnFragmentInteractionListener mListener;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CallingDetailFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param orgId Parameter 1.
     * @return A new instance of fragment CallingDetailSearchFragment.
     */
    public static CallingDetailFragment newInstance(long orgId) {
        CallingDetailFragment fragment = new CallingDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ORG_ID, orgId);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(boolean search, Calling calling, boolean hasChanges);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calling_detail, container, false);
        statusDropdown = (Spinner) view.findViewById(R.id.calling_detail_status_dropdown);
        notes = (TextView) view.findViewById(R.id.notes_calling_detail);
        Bundle bundle = this.getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            /* Initialize UI */
            orgId = bundle.getLong(ORG_ID, 0);
            org = dataManager.getOrg(orgId);
            String callingId = bundle.getString(CALLING_ID);
            hydrateCalling(callingId);
            individualId = bundle.getLong(INDIVIDUAL_ID, 0);
            wireUpFinalizeButton();
            wireUpStatusDropdown();
            wireUpMemberSearch();
        }
        return view;
    }

    private void wireUpMemberSearch() {
        if(individualId != 0) {
            String formattedName = dataManager.getMemberName(individualId);
            if(formattedName != null) {
                TextView name = (TextView) view.findViewById(R.id.member_lookup_name);
                name.setText(formattedName);
            }
        }
        ImageButton searchButton = (ImageButton) view.findViewById(R.id.member_lookup_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrgChanges(true);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean mDualPane;
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View detailsFrame = null; //getActivity().findViewById(R.id.mem;
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            //mCurCheckPosition = savedInstanceState.getInt("currentMember", 0);
        }

        if (mDualPane) {
            //showDetails(mCurCheckPosition);
        }
    }

    private void hydrateCalling(String callingId) {
        calling = dataManager.getCalling(callingId);
        if(calling != null) {
            TextView callingName = (TextView)view.findViewById(R.id.label_calling_detail_position);
            callingName.setText(calling.getPosition().getName());
            final TextView currentlyCalled = (TextView)view.findViewById(R.id.calling_detail_currently_called);
            String name = dataManager.getMemberName(calling.getMemberId());
            currentlyCalled.setText(name);
            TextView notes = (TextView) view.findViewById(R.id.notes_calling_detail);
            if(calling.getNotes() != null && calling.getNotes().length() > 0)  {
                notes.setText(calling.getNotes());
            }
        }
    }

    public String getNotes() {
        TextView notes = (TextView) view.findViewById(R.id.notes_calling_detail);
        return notes.getText().toString();
    }

    private void wireUpStatusDropdown() {
        List<CallingStatus> status = new ArrayList(Arrays.asList(CallingStatus.values()));
        Spinner statusDropdown = (Spinner)view.findViewById(R.id.calling_detail_status_dropdown);
        ArrayAdapter adapter = new ArrayAdapter<CallingStatus>(this.getContext(), android.R.layout.simple_list_item_1, status);
        statusDropdown.setAdapter(adapter);
        statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                submitOrgChanges(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(calling != null && calling.getProposedStatus() != null) {
            statusDropdown.setSelection(adapter.getPosition(CallingStatus.get(calling.getProposedStatus())));
        }
    }

    private void wireUpFinalizeButton() {
        /* Finalize calling button setup */
        Button button = (Button) view.findViewById(R.id.button_finalize_calling);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrgChanges(false);
            }
        });
    }

    private void submitOrgChanges(boolean search) {
        boolean hasChanges = false;
        CallingStatus callingStatus = (CallingStatus)statusDropdown.getSelectedItem();
        String status = null;
        if(callingStatus != null) {
            status = callingStatus.name();
        }
        if(status != null && !status.equals(calling.getProposedStatus())) {
            calling.setProposedStatus(status);
            hasChanges = true;
        }
        CharSequence extraNotes = notes.getText();
        if(!extraNotes.equals(calling.getNotes())) {
            calling.setNotes(extraNotes.toString());
            hasChanges = true;
        }
        if(individualId.equals(calling.getProposedIndId())) {
            calling.setProposedIndId(individualId);
            hasChanges = true;
        }
        mListener.onFragmentInteraction(search, calling, hasChanges);
    }

    private void wireUpFragments(Bundle savedInstanceState) {
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            MemberLookupFragment memberLookupFragment = new MemberLookupFragment();
            if(member != null && member.getIndividualId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, member.getIndividualId());
                memberLookupFragment.setArguments(args);
            }
            FragmentManager fragmentManager = this.getFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.calling_detail_main_fragment_container, memberLookupFragment, null)
                    .addToBackStack(null)
                    .commit();

            /*MemberLookupButtonFragment searchFragment = new MemberLookupButtonFragment();
            if(calling != null && calling.getProposedIndId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, calling.getProposedIndId());
                searchFragment.setArguments(args);
            }*/
        }
    }
}
