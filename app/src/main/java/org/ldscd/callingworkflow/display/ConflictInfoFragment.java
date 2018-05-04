package org.ldscd.callingworkflow.display;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.model.Calling;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.web.DataManager;

import javax.inject.Inject;

public class ConflictInfoFragment extends DialogFragment {
    /* Fields */
    Org org = null;
    Operation operation ;
    /* Properties */
    public void setOrg(Org org) {
        this.org = org;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Inject
    DataManager dataManager;

    /* Methods */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication) getActivity().getApplication()).getNetComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View conflictInfo = inflater.inflate(R.layout.conflict_info, container, false);
        String orgName = org.getDefaultOrgName();
        TextView title = conflictInfo.findViewById(R.id.conflict_title);
        title.setText(conflictInfo.getResources().getString(R.string.conflict_info_title));
        TextView messageView = conflictInfo.findViewById(R.id.conflict_message);
        messageView.setText(conflictInfo.getResources().getString(R.string.org_conflict_warning, orgName));
        TextView callingView = conflictInfo.findViewById(R.id.conflict_callings);
        callingView.setText(getPotentialCallings(org));

        Button dismissButton = conflictInfo.findViewById(R.id.conflict_info_dismiss);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Button deleteButton = conflictInfo.findViewById(R.id.conflict_info_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Deletes the parent base org. */
                dataManager.deleteOrg(org)
                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast.makeText(getContext(), getResources().getString(R.string.org_deleted_successfully), Toast.LENGTH_LONG);
                            getActivity().recreate();
                            dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), getResources().getString(R.string.org_failed_to_delete), Toast.LENGTH_LONG);
                        }
                    });
            }
        });

        Switch agreementSwitch = conflictInfo.findViewById(R.id.conflict_understand_switch);
        agreementSwitch.setChecked(false);
        agreementSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });
        if(agreementSwitch.isChecked()) {
            deleteButton.setEnabled(true);
        } else {
            deleteButton.setEnabled(false);
        }
        return conflictInfo;
    }

    private String getPotentialCallings(Org org) {
        StringBuilder sb = new StringBuilder();
        for(Calling calling : org.allOrgCallings()) {
            if(calling.getProposedIndId() != null && calling.getProposedIndId() > 0) {
                sb.append(calling.getPosition().getName());
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }
}