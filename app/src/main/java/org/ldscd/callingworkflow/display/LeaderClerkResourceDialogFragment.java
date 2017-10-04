package org.ldscd.callingworkflow.display;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONException;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.constants.Operation;
import org.ldscd.callingworkflow.model.Calling;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     LeaderClerkResourceDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link LeaderClerkResourceDialogFragment.LeaderClerkResourceListener}.</p>
 */
public class LeaderClerkResourceDialogFragment extends BottomSheetDialogFragment {

    private static final String UNIT_NUMBER = "unit_number";
    private static final String CALLING = "calling";
    private LeaderClerkResourceListener mListener;
    private Calling calling;
    private Long unitNumber;

    public static LeaderClerkResourceDialogFragment newInstance() {
        return new LeaderClerkResourceDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leaderclerkresource_dialog, container, false);
        wireupButtons(v);
        return v;
    }

    /* Comes from CallingDetailFragment. */
    public void OnLCRCallingUpdateListener(LeaderClerkResourceListener listener) {
        mListener = listener;
    }

    private void wireupButtons(View v) {
        Button releaseButton = (Button)v.findViewById(R.id.button_release_current_in_lcr);
        releaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mListener.onLeaderClerkResourceFragmentInteraction(Operation.RELEASE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });

        Button updateButton = (Button)v.findViewById(R.id.button_update_calling_in_lcr);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mListener.onLeaderClerkResourceFragmentInteraction(Operation.UPDATE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });

        Button deleteButton = (Button)v.findViewById(R.id.button_delete_calling_in_lcr);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mListener.onLeaderClerkResourceFragmentInteraction(Operation.DELETE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_leaderclerkresource_dialog, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    public interface LeaderClerkResourceListener {
        void onLeaderClerkResourceFragmentInteraction(Operation operation) throws JSONException;
    }
}