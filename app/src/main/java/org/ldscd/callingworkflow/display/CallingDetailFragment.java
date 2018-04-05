package org.ldscd.callingworkflow.display;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.model.PositionRequirements;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

import static org.ldscd.callingworkflow.display.MemberLookupFilterFragment.CAN_VIEW_PRIESTHOOD;

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
    public static final String CAN_VIEW = "canView";
    public static final String HAS_CONFLICT = "hasConflict";

    private Member proposedMember;
    private Long individualId;
    private Calling calling;
    private View view;
    private OnCallingDetailFragmentListener mListener;
    private boolean resetProposedStatus = false;
    private int statusPosition = 0;
    private boolean canView = true;
    private boolean canViewPriesthoodFilters = false;
    private boolean hasConflict = false;
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
        /* Set status spinner to be reset to proposed if proposedMember changed,
         *  UI must be done after the fragment has been restarted
         */
        if(this.proposedMember != member || member == null) {
            resetProposedStatus = true;
            statusPosition = member == null ? 0 : 1;
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
        wireUpMemberSearch(canView);
    }

    @Override
    public void onLeaderClerkResourceFragmentInteraction(Operation operation) throws JSONException {
        ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
        pd.show();
        if(operation == Operation.RELEASE) {
            dataManager.releaseLDSCalling(calling, LCRResponse, errorListener);
        } else if(operation == Operation.UPDATE) {
            dataManager.updateLDSCalling(calling, LCRResponse, errorListener);
        } else if(operation == Operation.DELETE) {
            if (calling.isCwfOnly()) {
                dataManager.deleteCalling(calling, LCRResponse, errorListener);
            } else {
                dataManager.deleteLDSCalling(calling, LCRResponse, errorListener);
            }
        }
    }

    protected Response.Listener<Boolean> LCRResponse = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean response) {
            getActivity().finish();
            Toast.makeText(getContext(), getResources().getString(R.string.items_saved), Toast.LENGTH_SHORT).show();
        }
    };

    protected Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public interface OnCallingDetailFragmentListener {
        void onFragmentInteraction(Calling calling, boolean hasChanges);
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

        dataManager.getUserInfo(null, null, false, new Response.Listener<LdsUser>() {
            @Override
            public void onResponse(LdsUser user) {
                if(user != null) {
                    canViewPriesthoodFilters = dataManager.getPermissionManager().hasPermission(user.getUnitRoles(), Permission.PRIESTHOOD_OFFICE_READ);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calling_detail, container, false);
        statusDropdown = view.findViewById(R.id.calling_detail_status_dropdown);
        notes = view.findViewById(R.id.notes_calling_detail);
        Bundle bundle = this.getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            /* Initialize UI */
            calling = (Calling)bundle.getSerializable(CALLING);
            individualId = bundle.getLong(INDIVIDUAL_ID);
            canView = bundle.getBoolean(CAN_VIEW);
            hasConflict = bundle.getBoolean(HAS_CONFLICT);
            hydrateCalling();
            wireUpFinalizeButton(canView);
            wireUpStatusDropdown(canView);
            wireUpMemberSearch(canView);
            wireUpNotes(canView);
        }
        return view;
    }

    private void wireUpMemberSearch(boolean canView) {
        if(!canView) {
            /* Hide the member lookup button if the user doesn't have rights to see it. */
            TableLayout tableLayout = view.findViewById(R.id.member_lookup_button_layout_table);
            tableLayout.setVisibility(View.GONE);
            TextView proposedNameLabel = view.findViewById(R.id.label_calling_detail_proposed);
            proposedNameLabel.setVisibility(View.GONE);
        } else {
            ImageView memberWarningIcon = view.findViewById(R.id.member_selection_warning);
            memberWarningIcon.setVisibility(View.GONE);
            if (calling.getProposedIndId() != null && calling.getProposedIndId() != 0) {
                this.proposedMember = dataManager.getMember(calling.getProposedIndId());
                String formattedName = dataManager.getMemberName(calling.getProposedIndId());
                if (formattedName != null) {
                    TextView name = view.findViewById(R.id.member_lookup_name);
                    name.setText(formattedName);
                    if (proposedMember != null && proposedMember.getIndividualId() > 0) {
                        name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                wireUpIndividualInformationFragments(proposedMember.getIndividualId());
                            }
                        });
                        PositionMetaData metaData = dataManager.getPositionMetadata(calling.getPosition().getPositionTypeId());
                        PositionRequirements requirements = metaData != null ? metaData.getRequirements() : null;
                        if(requirements != null && !requirements.meetsRequirements(proposedMember)) {
                            memberWarningIcon.setVisibility(View.VISIBLE);
                            memberWarningIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    View dialogView = getActivity().getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
                                    TextView messageTextView = dialogView.findViewById(R.id.warning_message);
                                    messageTextView.setText(R.string.missing_requirement_warning);
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                            .setView(dialogView)
                                            .setPositiveButton(R.string.ok, null)
                                            .show();
                                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)positiveButton.getLayoutParams();
                                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                    positiveButton.setLayoutParams(layoutParams);
                                }
                            });
                        }
                    }
                }
            }
        }
        ImageButton searchButton = view.findViewById(R.id.member_lookup_button);
        if(!hasConflict) {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createMemberLookupFragment();
                }
            });
        } else {
            searchButton.setEnabled(false);
        }
    }

    public void createMemberLookupFragment() {
        MemberLookupFragment memberLookupFragment = new MemberLookupFragment();

        Bundle args = new Bundle();
        if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, calling.getProposedIndId());
        }
        args.putBoolean(CAN_VIEW_PRIESTHOOD, canViewPriesthoodFilters);
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
            String name = dataManager.getMemberName(calling.getMemberId() != null ? calling.getMemberId() : 0);
            currentlyCalled.setText(name);
            if(calling.getMemberId() != null && calling.getMemberId() > 0) {
                currentlyCalled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wireUpIndividualInformationFragments(calling.getMemberId());
                    }
                });
            }
        }
    }

    private void wireUpStatusDropdown(boolean canView) {
        if(!canView) {
            statusDropdown.setVisibility(View.GONE);
            TextView statusLabel = view.findViewById(R.id.label_calling_detail_status);
            statusLabel.setVisibility(View.GONE);
        } else {
            dataManager.getCallingStatus(new Response.Listener<List<CallingStatus>>() {
                @Override
                public void onResponse(List<CallingStatus> statusList) {
                    if (calling.getProposedStatus() == null || !calling.getProposedStatus().equals(CallingStatus.UNKNOWN)) {
                        statusList.remove(CallingStatus.UNKNOWN);
                    }

                    ArrayAdapter adapter = new ArrayAdapter<CallingStatus>(getContext(), android.R.layout.simple_list_item_1, statusList);
                    statusDropdown.setAdapter(adapter);
                    if (calling != null && calling.getProposedStatus() != null) {
                        statusDropdown.setSelection(adapter.getPosition(calling.getProposedStatus()));
                    }
                    if(resetProposedStatus) {
                        statusDropdown.setSelection(statusPosition, true);
                        resetProposedStatus = false;
                    }
                    if(!hasConflict) {
                        statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                submitOrgChanges();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    } else {
                        statusDropdown.setEnabled(false);
                    }
                }
            });
        }
    }

    private void wireUpNotes(boolean canView) {
        if(!canView) {
            notes.setVisibility(View.GONE);
        } else {
            if(!hasConflict) {
                notes.addTextChangedListener(textWatcherNotesListener);
            } else {
                notes.setEnabled(false);
            }
            notes.setText(calling.getNotes() != null && calling.getNotes().length() > 0 ? calling.getNotes() : "");
        }
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

    private void wireUpFinalizeButton(boolean canView) {
        /* Finalize calling button setup */
        Button button = view.findViewById(R.id.button_finalize_calling);
        if(!canView || hasConflict) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateLCR();
                }
            });
        }
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
        if(calling.getProposedIndId() != null && !calling.getProposedIndId().equals(individualId)) {
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
            Bundle args = new Bundle();
            args.putBoolean(CAN_VIEW_PRIESTHOOD, canViewPriesthoodFilters);
            if(proposedMember != null && proposedMember.getIndividualId() > 0) {
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, proposedMember.getIndividualId());
            }
            memberLookupFragment.setArguments(args);
            FragmentManager fragmentManager = this.getFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.calling_detail_main_fragment_container, memberLookupFragment, null)
                    .addToBackStack(null)
                    .commit();
        }
    }
}