package org.ldscd.callingworkflow.display;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.model.Position;
import org.ldscd.callingworkflow.web.DataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class CreateCallingFragment extends Fragment implements MemberLookupFragment.OnMemberLookupFragmentListener {
    public static final String PARENT_ORG_ID = "parentOrgId";
    public static final String TAG = "createCallingFragment";

    @Inject
    DataManager dataManager;

    private View view;
    private Org parentOrg;
    private Member proposedMember;
    private SubFragmentOpenListener subFragmentListener;

    Spinner positionDropdown;
    Spinner statusDropdown;
    EditText notesBox;

    //this is used to keep track of what action bar items to display in the activity
    public interface SubFragmentOpenListener {
        void onBaseFragmentStarted();
        void onSubFragmentStarted();
    }

    public Calling getNewCalling() {

        Long parentId = parentOrg.getId();
        Long proposedId = null;
        if(proposedMember != null) {
            proposedId = proposedMember.getIndividualId();
        }
        Position position = (Position)positionDropdown.getSelectedItem();
        String status = statusDropdown.getSelectedItem().toString();
        String notes = notesBox.getText().toString();

        return new Calling(null, null, null, proposedId, null, position, null, status, notes, parentId);
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
            ArrayAdapter positionAdapter = new ArrayAdapter<Position>(getActivity(), android.R.layout.simple_list_item_1, potentialPositions);
            positionDropdown.setAdapter(positionAdapter);
        }

        //Status Dropdown
        List<CallingStatus> statusOptions = new ArrayList(Arrays.asList(CallingStatus.values()));
        statusDropdown = (Spinner) view.findViewById(R.id.new_calling_status_dropdown);
        ArrayAdapter statusAdapter = new ArrayAdapter<CallingStatus>(getActivity(), android.R.layout.simple_list_item_1, statusOptions);
        statusDropdown.setAdapter(statusAdapter);

        //notes editbox
        notesBox = (EditText) view.findViewById(R.id.new_calling_notes);

        //Member Lookup
        if(proposedMember != null) {
            TextView proposedMemberTextView = (TextView) view.findViewById(R.id.member_lookup_name);
            proposedMemberTextView.setText(proposedMember.getFormattedName());
        }
        ImageButton searchButton = (ImageButton) view.findViewById(R.id.member_lookup_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subFragmentListener.onSubFragmentStarted();
                openMemberLookup();
            }
        });

        return view;

    }

    private void openMemberLookup() {
        MemberLookupFragment memberLookupFragment = new MemberLookupFragment();
        memberLookupFragment.setMemberLookupListener(this);
        if(proposedMember != null && proposedMember.getIndividualId() > 0) {
            Bundle args = new Bundle();
            args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, proposedMember.getIndividualId());
            memberLookupFragment.setArguments(args);
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.create_calling_fragment_container, memberLookupFragment, MemberLookupFragment.FRAG_NAME)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onMemberLookupFragmentInteraction(Member member) {
        proposedMember = member;
    }
}
