package org.ldscd.callingworkflow.display;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.ConflictCause;
import org.ldscd.callingworkflow.model.Calling;

public class ConflictInfoFragment extends DialogFragment {
    Calling calling = null;
    public void setCalling(Calling calling) {
        this.calling = calling;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View conflictInfo = inflater.inflate(R.layout.conflict_info, container, false);

        TextView messageView = (TextView) conflictInfo.findViewById(R.id.conflict_message);
        if(calling.getConflictCause() != null) {
            String explanation;
            if(calling.getConflictCause().equals(ConflictCause.EQUIVALENT_POTENTIAL_AND_ACTUAL)) {
                explanation = conflictInfo.getResources().getString(R.string.equivalent_potential_and_actual_explaination);
            } else {
                explanation = conflictInfo.getResources().getString(R.string.equivalent_deleted_explanation);
            }
            messageView.setText(conflictInfo.getResources().getString(R.string.calling_changed_message, explanation));
        }

        Button dismissButton = (Button) conflictInfo.findViewById(R.id.conflict_info_dismiss);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button deleteButton = (Button) conflictInfo.findViewById(R.id.conflict_info_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: add handling to delete this calling from CWF then dismiss
            }
        });

        return conflictInfo;
    }
}
