package org.ldscd.callingworkflow.display;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.MemberData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

public class IndividualInformationFragment extends BottomSheetDialogFragment {

    private static final String INDIVIDUAL_ID = "individualId";
    private static long individualId;
    @Inject
    MemberData memberData;

    private Member member;

    public IndividualInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param individualId Parameter 1.
     * @return A new instance of fragment IndividualInformationFragment.
     */
    public static IndividualInformationFragment newInstance(long individualId) {
        IndividualInformationFragment fragment = new IndividualInformationFragment();
        Bundle args = new Bundle();
        args.putLong(INDIVIDUAL_ID, individualId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CWFApplication)getActivity().getApplication()).getNetComponent().inject(this);
        if (getArguments() != null) {
            individualId = getArguments().getLong(INDIVIDUAL_ID);
            member = memberData.getMember(individualId);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_individual_information, container, false);
        if(member != null) {
            /* Hydrate Calling(s) */
            TextView calling = (TextView) v.findViewById(R.id.member_information_calling);
            calling.setText((member.getCurrentCallings() != null && member.getCurrentCallings().size() > 0)
                    ? member.getCurrentCallings().toString() : "");
            /* Hydrate Phone */
            TextView cell_phone = (TextView) v.findViewById(R.id.member_information_cell_phone);
            cell_phone.setText(member.getIndividualPhone() != null
                    ? member.getIndividualPhone() : "");
            /* Hydrate Home phone */
            TextView home_phone = (TextView) v.findViewById(R.id.member_information_home_phone);
            home_phone.setText(member.getHouseholdPhone() != null
                    ? member.getHouseholdPhone() : "");
            /* Hydrate Individual Email */
            TextView individual_email = (TextView) v.findViewById(R.id.member_information_individual_email);
            individual_email.setText(member.getIndividualEmail() != null
                    ? member.getIndividualEmail() : "");
            /* Hydrate Household Email */
            TextView email = (TextView) v.findViewById(R.id.member_information_home_email);
            email.setText(member.getHouseholdEmail() != null
                    ? member.getHouseholdEmail() : "");
            /* Hydrate Address */
            TextView address = (TextView) v.findViewById(R.id.member_information_address);
            try {
                final String strAddress = member.getStreetAddress();
                final String encodedAddress = URLEncoder.encode(strAddress, "UTF-8");
                address.setText(strAddress);
                address.setMovementMethod(LinkMovementMethod.getInstance());
                address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initGoogleMap(encodedAddress);
                    }
                });
            } catch (Error | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ImageButton cell_phone_sms = (ImageButton) v.findViewById(R.id.member_information_cell_phone_sms);
            cell_phone_sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendSMS(member.getIndividualPhone());
                }
            });

            ImageButton home_phone_sms = (ImageButton) v.findViewById(R.id.member_information_home_phone_sms);
            home_phone_sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendSMS(member.getHouseholdPhone());
                }
            });
        }
        return v;
    }

    private void initGoogleMap(String address) {
        String uri = "http://maps.google.com/maps?q=" + address;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        try
        {
            startActivity(intent);
        }
        catch(ActivityNotFoundException ex)
        {
            try
            {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(unrestrictedIntent);
            }
            catch(ActivityNotFoundException innerEx)
            {
                Toast.makeText(getContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendSMS(String phoneNumber) {
        if(phoneNumber != null && phoneNumber.length() > 0) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
            {
                String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getContext());
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra("address", phoneNumber);
                // Can be null in case that there is no default, then the user would be able to choose
                if (defaultSmsPackageName != null)
                // any app that support this intent.
                {
                    sendIntent.setPackage(defaultSmsPackageName);
                }
                startActivity(sendIntent);

            } else {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", phoneNumber);
                startActivity(smsIntent);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
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
        View contentView = View.inflate(getContext(), R.layout.fragment_individual_information, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}