package org.ldscd.callingworkflow.display;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
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

    private LeaderClerkResourceListener mListener;
    private static String PROPOSED_NAME = "proposedMemberName";
    private static String CURRENTLY_CALLED_NAME = "currentlyCalledMemberName";
    private static String CALLING_NAME = "callingName";
    private String proposedMemberName;
    private String currentlyCalledMemberName;
    private String callingName;

    public static LeaderClerkResourceDialogFragment newInstance(String proposedMemberName, String currentlyCalledMemberName, String callingName) {
        LeaderClerkResourceDialogFragment fragment = new LeaderClerkResourceDialogFragment();
        Bundle args = new Bundle();
        args.putString(PROPOSED_NAME, proposedMemberName);
        args.putString(CURRENTLY_CALLED_NAME, currentlyCalledMemberName);
        args.putString(CALLING_NAME, callingName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            proposedMemberName = getArguments().getString(PROPOSED_NAME);
            currentlyCalledMemberName = getArguments().getString(CURRENTLY_CALLED_NAME);
            callingName = getArguments().getString(CALLING_NAME);
        }
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
        final LeaderClerkResourceListener tempListener = mListener;
        Button releaseButton = (Button)v.findViewById(R.id.button_release_current_in_lcr);
        releaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = getAlertDialog();
                adb.setMessage(getResources().getString(R.string.warning_release_message, currentlyCalledMemberName, callingName));
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                tempListener.onLeaderClerkResourceFragmentInteraction(Operation.RELEASE);
                                dialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                );
                adb.show();
                dismiss();
            }
        });

        Button updateButton = (Button)v.findViewById(R.id.button_update_calling_in_lcr);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = getAlertDialog();
                String message = currentlyCalledMemberName != null && currentlyCalledMemberName.length() > 0
                        ? getResources().getString(R.string.warning_update_release_message, proposedMemberName, currentlyCalledMemberName, callingName)
                        : getResources().getString(R.string.warning_update_message, proposedMemberName, callingName);
                adb.setMessage(message);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                tempListener.onLeaderClerkResourceFragmentInteraction(Operation.UPDATE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    }
                );
                adb.show();
                dismiss();
            }
        });

        Button deleteButton = (Button)v.findViewById(R.id.button_delete_calling_in_lcr);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            AlertDialog.Builder adb = getAlertDialog();
            adb.setMessage(getResources().getString(R.string.warning_release_message, currentlyCalledMemberName, callingName));
            final LeaderClerkResourceListener tempListener = mListener;
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            tempListener.onLeaderClerkResourceFragmentInteraction(Operation.DELETE);
                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            );
            adb.show();
            dismiss();
            }
        });
    }

    private void navigateUrlChanges() {
        try {
            mListener.onLeaderClerkResourceFragmentInteraction(Operation.UPDATE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private AlertDialog.Builder getAlertDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setTitle(getResources().getString(R.string.warning));
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }
        );

        return adb;
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