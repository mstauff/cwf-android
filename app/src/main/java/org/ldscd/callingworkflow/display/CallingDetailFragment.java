package org.ldscd.callingworkflow.display;

import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

/**
 * A fragment representing a single Calling detail screen.
 * This fragment is either contained in a {@link ExpandableOrgsListActivity}
 * in two-pane mode (on tablets) or a {@link CallingDetailActivity}
 * on handsets.
 */
public class CallingDetailFragment extends Fragment implements MemberLookupFragment.OnMemberLookupFragmentListener, LeaderClerkResourceDialogFragment.LeaderClerkResourceListener {
    @Inject
    DataManager dataManager;
    Spinner statusDropdown;
    TextView notes;
    public static final String ORG_ID = "orgId";
    public static final String CALLING_ID = "calling_id";
    public static final String INDIVIDUAL_ID = "individualId";
    public static final String CALLING = "calling";

    private Member proposedMember;
    private Long individualId;
    private Calling calling;
    private View view;
    private OnCallingDetailFragmentListener mListener;
    private Operation operation;
    private boolean resetProposedStatus = false;
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

    @Override
    public void onMemberLookupFragmentInteraction(Member member) {
        //set status spinner to be reset to proposed if proposedMember changed, UI must be done after the fragment has been restarted
        if(this.proposedMember != member) {
            resetProposedStatus = true;
        }

        /* If the currently selected member is different from the originally selected member
           remove the proposed calling from the previously selected member.
         */
        if(this.proposedMember != null && this.proposedMember != member) {
            this.proposedMember.removeProposedCalling(this.calling);
        }
        /* Re-assign the proposed member to the newly selected member. */
        this.proposedMember = member;
        this.calling.setProposedIndId(member == null ? 0 : member.getIndividualId());
        wireUpMemberSearch();
    }

    @Override
    public void onLeaderClerkResourceFragmentInteraction(Operation operation) throws JSONException {
        this.operation = operation;
        if(operation == Operation.RELEASE) {
            dataManager.releaseLDSCalling(calling, UpdateLCRResponse);
        } else if(operation == Operation.UPDATE) {
            dataManager.updateLDSCalling(calling, UpdateLCRResponse);
        } else if(operation == Operation.DELETE) {
            dataManager.deleteCalling(DeleteLCRResponse, calling);
        }else if(operation == Operation.DELETE_LCR) {
            dataManager.deleteLDSCalling(calling, UpdateLCRResponse);
        }

    }
    protected Response.Listener<JSONObject> UpdateLCRResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            if (jsonObject != null) {
                if(jsonObject.has("error")) {
                    Toast.makeText(getContext(), getResources().getString(R.string.error_update_Message), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if(jsonObject.has("positionId")) {
                            calling.setId(jsonObject.getLong("positionId"));
                        }
                        /* Release will only affect actual not potential */
                        if(operation.equals(Operation.RELEASE)) {
                            /* Update Google drive by removing the current memberId, active date, and callingId */
                            calling.setActiveDate(null);
                            calling.setActiveDateTime(null);
                            calling.setMemberId(0);
                            calling.setCwfId(null);
                        } else {
                            calling.setNotes("");
                            calling.setExistingStatus(null);
                            calling.setProposedIndId(0);
                            calling.setProposedStatus(CallingStatus.NONE);
                            calling.setActiveDate(DateTime.now());
                            calling.setActiveDateTime(DateTime.now());
                            calling.setMemberId(calling.getProposedIndId());
                            calling.setCwfId(null);
                            calling.setConflictCause(null);
                            //TODO: Calling ID needs to be set and
                        }
                        dataManager.updateCalling(new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(Boolean response) {
                                if(response) {
                                    changeActivities();
                                } else {
                                    Toast.makeText(getContext(),"Item Failed to update locally but was saved to lds.org.  Please restart your application.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, calling);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void changeActivities() {
        Intent intent = new Intent(getActivity(), OrgListActivity.class);
        startActivity(intent);
    }

    protected Response.Listener<Boolean> DeleteLCRResponse = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            if(!response) {
                Toast.makeText(getContext(), getResources().getString(R.string.error_update_Message), Toast.LENGTH_SHORT).show();
            } else {
                /* Callings that do not allow multiples we do not show delete option */
                calling.setNotes("");
                calling.setExistingStatus(null);
                calling.setProposedIndId(0);
                calling.setProposedStatus(CallingStatus.NONE);
                calling.setActiveDate(null);
                calling.setActiveDateTime(null);
                calling.setMemberId(0);
                calling.setCwfId(null);
                calling.setConflictCause(null);

                changeActivities();
            }
        }
    };

    public interface OnCallingDetailFragmentListener {
        public void onFragmentInteraction(Calling calling, boolean hasChanges);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnCallingDetailFragmentListener) getActivity();
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
    public void onResume() {
        super.onResume();
        if(resetProposedStatus) {
            statusDropdown.setSelection(1); //set to first option below 'none'
            resetProposedStatus = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calling_detail, container, false);
        statusDropdown = (Spinner) view.findViewById(R.id.calling_detail_status_dropdown);
        notes = (TextView) view.findViewById(R.id.notes_calling_detail);
        Bundle bundle = this.getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            /* Initialize UI */
            calling = (Calling)bundle.getSerializable(CALLING);
            individualId = bundle.getLong(INDIVIDUAL_ID);
            hydrateCalling();
            wireUpFinalizeButton();
            wireUpStatusDropdown();
            wireUpMemberSearch();
            wireUpNotes();
        }
        return view;
    }

    private void wireUpMemberSearch() {
        if(calling.getProposedIndId()!= 0) {
            this.proposedMember = dataManager.getMember(calling.getProposedIndId());
            String formattedName = this.proposedMember == null ? "" : this.proposedMember.getFormattedName();
            if(formattedName != null) {
                TextView name = (TextView) view.findViewById(R.id.member_lookup_name);
                name.setText(formattedName);
                if(proposedMember != null && proposedMember.getIndividualId() > 0) {
                    name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wireUpIndividualInformationFragments(proposedMember.getIndividualId());
                        }
                    });
                }
            }
        }
        ImageButton searchButton = (ImageButton) view.findViewById(R.id.member_lookup_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMemberLookupFragment();
            }
        });
    }

    public void createMemberLookupFragment() {
        MemberLookupFragment memberLookupFragment = new MemberLookupFragment();

        Bundle args = new Bundle();
        if(calling.getProposedIndId() > 0) {
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, calling.getProposedIndId());
        }
        args.putInt(MemberLookupFragment.positionTypeIdName, calling.getPosition().getPositionTypeId());
        memberLookupFragment.setArguments(args);

        memberLookupFragment.setMemberLookupListener(this);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.calling_detail_main_fragment_container, memberLookupFragment, MemberLookupFragment.FRAG_NAME)
                .addToBackStack(null)
                .commit();
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

    private void hydrateCalling() {
        if(calling != null) {
            TextView callingName = (TextView)view.findViewById(R.id.label_calling_detail_position);
            callingName.setText(calling.getPosition().getName());
            final TextView currentlyCalled = (TextView)view.findViewById(R.id.calling_detail_currently_called);
            String name = dataManager.getMemberName(calling.getMemberId());
            currentlyCalled.setText(name);
            if(calling.getMemberId() != null && calling.getMemberId() > 0) {
                currentlyCalled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wireUpIndividualInformationFragments(calling.getMemberId());
                    }
                });
            }
            TextView notes = (TextView) view.findViewById(R.id.notes_calling_detail);
            if(calling.getNotes() != null && calling.getNotes().length() > 0)  {
                notes.setText(calling.getNotes());
            }
        }
    }

    private void wireUpStatusDropdown() {
        dataManager.getCallingStatus(new Response.Listener<List<CallingStatus>>() {
            @Override
            public void onResponse(List<CallingStatus> statusList) {
                if(calling.getProposedStatus() == null || !calling.getProposedStatus().equals(CallingStatus.UNKNOWN)) {
                    statusList.remove(CallingStatus.UNKNOWN);
                }
                Spinner statusDropdown = (Spinner)view.findViewById(R.id.calling_detail_status_dropdown);
                ArrayAdapter adapter = new ArrayAdapter<CallingStatus>(getContext(), android.R.layout.simple_list_item_1, statusList);
                statusDropdown.setAdapter(adapter);
                if(calling != null && calling.getProposedStatus() != null) {
                    statusDropdown.setSelection(adapter.getPosition(calling.getProposedStatus()));
                }
                statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        submitOrgChanges();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void wireUpNotes() {
        TextView notes = (TextView) view.findViewById(R.id.notes_calling_detail);
        notes.addTextChangedListener(textWatcherNotesListener);
    }

    private final TextWatcher textWatcherNotesListener = new TextWatcher() {
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
                    submitOrgChanges();
                }
            };
            handler.postDelayed(runnable, 1000);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    };

    private void wireUpFinalizeButton() {
        /* Finalize calling button setup */
        Button button = (Button) view.findViewById(R.id.button_finalize_calling);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLCR();
            }
        });
    }

    private void updateLCR() {
        Member currentlyCalledMember = null;
        /* Calling object may have some null fields if it was recently created in the UI. */
        if(calling != null && calling.getMemberId() != null && calling.getMemberId() > 0) {
            currentlyCalledMember = dataManager.getMember(calling.getMemberId());
        }
        LeaderClerkResourceDialogFragment leaderClerkResourceDialogFragment =
                LeaderClerkResourceDialogFragment.newInstance(
                        proposedMember != null ? proposedMember.getFormattedName() : null,
                        currentlyCalledMember != null ? currentlyCalledMember.getFormattedName() : null,
                        calling.getPosition().getName(),
                        dataManager.canDeleteCalling(calling, dataManager.getOrg(calling.getParentOrg())),
                        calling.getId() != null && calling.getId() > 0);
        leaderClerkResourceDialogFragment.OnLCRCallingUpdateListener(this);
        leaderClerkResourceDialogFragment.show(getFragmentManager(), null);
    }

    private void submitOrgChanges() {
        boolean hasChanges = false;
        CallingStatus callingStatus = (CallingStatus)statusDropdown.getSelectedItem();
        if(callingStatus != null && !callingStatus.equals(calling.getProposedStatus())) {
            calling.setProposedStatus(callingStatus);
            hasChanges = true;
        }
        String extraNotes = notes.getText().toString();
        String originalNotes = calling.getNotes() == null ? "" : calling.getNotes();
        if(!extraNotes.equals(originalNotes)) {
            if(extraNotes.equals("")) {
                calling.setNotes(null);
            } else {
                calling.setNotes(extraNotes);
            }
            hasChanges = true;
        }
        if(!calling.getProposedIndId().equals(individualId)) {
            hasChanges = true;
        }
        mListener.onFragmentInteraction(calling, hasChanges);
    }

    public void wireUpIndividualInformationFragments(Long individualId) {
        if (individualId != null) {
            IndividualInformationFragment member_information_fragment = new IndividualInformationFragment();
            Bundle args = new Bundle();
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, individualId);
            member_information_fragment.setArguments(args);
            member_information_fragment.show(getFragmentManager(), null);
        }
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
            if(proposedMember != null && proposedMember.getIndividualId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, proposedMember.getIndividualId());
                memberLookupFragment.setArguments(args);
            }
            FragmentManager fragmentManager = this.getFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.calling_detail_main_fragment_container, memberLookupFragment, null)
                    .addToBackStack(null)
                    .commit();
        }
    }
}