package org.ldscd.callingworkflow.display;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.display.adapters.PositionArrayAdapter;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.LdsUser;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.model.PositionMetaData;
import org.ldscd.callingworkflow.model.PositionRequirements;
import org.ldscd.callingworkflow.model.permissions.constants.Permission;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.List;

import javax.inject.Inject;

import static org.ldscd.callingworkflow.display.MemberLookupFilterFragment.CAN_VIEW_PRIESTHOOD;

public class CreateCallingFragment extends Fragment implements MemberLookupFragment.OnMemberLookupFragmentListener {
    public static final String PARENT_ORG_ID = "parentOrgId";
    public static final String TAG = "createCallingFragment";

    @Inject
    DataManager dataManager;

    private View view;
    private Org parentOrg;
    private Member proposedMember;
    private Position position;
    private SubFragmentOpenListener subFragmentListener;
    boolean resetProposedStatus = false;
    boolean canViewPriesthood = false;
    Spinner positionDropdown;
    Spinner statusDropdown;
    EditText notesBox;

    //this is used to keep track of what action bar items to display in the activity
    public interface SubFragmentOpenListener {
        void onBaseFragmentStarted();
        void onSubFragmentStarted();
    }

    public Calling getNewCalling() {
        boolean cwfOnly = true;
        Long parentId = parentOrg.getId();
        Long proposedId = null;
        if(proposedMember != null) {
            proposedId = proposedMember.getIndividualId();
        }
        CallingStatus status = (CallingStatus) statusDropdown.getSelectedItem();
        String notes = notesBox.getText().toString();

        return new Calling(null, null, cwfOnly, null, proposedId, null, position, null, status, notes, parentId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication) getActivity().getApplication()).getNetComponent().inject(this);
    }

    public void setSubFragmentOpenListener(SubFragmentOpenListener subFragmentListener) {
        this.subFragmentListener = subFragmentListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        subFragmentListener.onBaseFragmentStarted();
        if(resetProposedStatus) {
            statusDropdown.setSelection(1); //the first option below 'none'
            resetProposedStatus = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_calling, container, false);
        long parentOrgId = getActivity().getIntent().getLongExtra(PARENT_ORG_ID, 0);
        parentOrg = dataManager.getOrg(parentOrgId);

        if(parentOrg != null) {
            //Position Dropdown
            List<Position> potentialPositions = parentOrg.potentialNewPositions();
            positionDropdown = (Spinner) view.findViewById(R.id.new_calling_position_dropdown);
            final PositionArrayAdapter positionAdapter = new PositionArrayAdapter<Position>(getContext(), android.R.layout.simple_list_item_1, potentialPositions);
            positionDropdown.setAdapter(positionAdapter);
            position = (Position)positionDropdown.getSelectedItem();
            positionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    position = (Position)positionAdapter.getItem(i);
                    checkPositionRequirements();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        //Status Dropdown
        dataManager.getCallingStatus(new Response.Listener<List<CallingStatus>>() {
            @Override
            public void onResponse(List<CallingStatus> statusOptions) {
                if(statusOptions != null) {
                    statusOptions.remove(CallingStatus.UNKNOWN);
                    statusDropdown = (Spinner) view.findViewById(R.id.new_calling_status_dropdown);
                    ArrayAdapter statusAdapter = new ArrayAdapter<CallingStatus>(getActivity(), android.R.layout.simple_list_item_1, statusOptions);
                    statusDropdown.setAdapter(statusAdapter);
                }
            }
        });

        //notes editbox
        notesBox = (EditText) view.findViewById(R.id.new_calling_notes);

        //Member Lookup
        if(proposedMember != null) {
            TextView proposedMemberTextView = (TextView) view.findViewById(R.id.member_lookup_name);
            proposedMemberTextView.setText(proposedMember.getFormattedName());
        }
        checkPositionRequirements();
        ImageButton searchButton = (ImageButton) view.findViewById(R.id.member_lookup_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subFragmentListener.onSubFragmentStarted();
                openMemberLookup();
            }
        });

        LdsUser user = dataManager.getCurrentUser();
        if(user != null) {
            canViewPriesthood = dataManager.getPermissionManager().hasPermission(user.getUnitRoles(), Permission.PRIESTHOOD_OFFICE_READ);
        }

        return view;

    }

    private void checkPositionRequirements() {
        ImageView memberWarningIcon = (ImageView) view.findViewById(R.id.member_selection_warning);
        memberWarningIcon.setVisibility(View.GONE);
        if(proposedMember != null) {
            PositionMetaData metaData = dataManager.getPositionMetadata(position.getPositionTypeId());
            PositionRequirements requirements = metaData != null ? metaData.getRequirements() : null;
            if(requirements != null && !requirements.meetsRequirements(proposedMember)) {
                memberWarningIcon.setVisibility(View.VISIBLE);
                memberWarningIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.warning_dialog_text, null);
                        TextView messageTextView = (TextView)dialogView.findViewById(R.id.warning_message);
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

    private void openMemberLookup() {
        MemberLookupFragment memberLookupFragment = new MemberLookupFragment();
        memberLookupFragment.setMemberLookupListener(this);
        Bundle args = new Bundle();
        args.putBoolean(CAN_VIEW_PRIESTHOOD, canViewPriesthood);
        if(proposedMember != null && proposedMember.getIndividualId() > 0) {
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, proposedMember.getIndividualId());
        }
        args.putInt(MemberLookupFragment.positionTypeIdName,  position != null ? position.getPositionTypeId() : 0);
        memberLookupFragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .replace(R.id.create_calling_fragment_container, memberLookupFragment, MemberLookupFragment.FRAG_NAME)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onMemberLookupFragmentInteraction(Member member) {
        if(proposedMember != member) {
            proposedMember = member;
            //We can't make changes directly to the spinner here or they'll be overridden to it's previous state when the fragment resumes
            resetProposedStatus = true;
        }
    }
}
