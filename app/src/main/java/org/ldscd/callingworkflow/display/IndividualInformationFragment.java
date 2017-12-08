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
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.web.DataManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

public class IndividualInformationFragment extends BottomSheetDialogFragment {

    private static final String INDIVIDUAL_ID = "individualId";
    @Inject
    DataManager dataManager;

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
            long individualId = getArguments().getLong(INDIVIDUAL_ID);
            member = dataManager.getMember(individualId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_individual_information, container, false);
        if(member != null) {
            TextView nameView = (TextView) v.findViewById(R.id.member_information_member_name);
            nameView.setText(member.getFormattedName());
            TextView callingView = (TextView) v.findViewById(R.id.member_information_calling);
            if(member.getCurrentCallings() != null && !member.getCurrentCallings().isEmpty()) {
                String names = member.getCurrentCallingsWithTime().toString();
                callingView.setText(names.substring(1, names.length() -1));
            } else {
                TextView callingLabel = (TextView) v.findViewById(R.id.member_information_calling_label);
                callingLabel.setVisibility(View.GONE);
                callingView.setVisibility(View.GONE);
            }

            TextView proposedCalling = (TextView) v.findViewById(R.id.member_information_proposed_calling);
            if(member.getProposedCallings() != null && member.getProposedCallings().size() > 0) {
                String names = member.getProposedCallingsWithStatus().toString();
                proposedCalling.setText(names.substring(1, names.length() -1));
            } else {
                TextView proposedCallingLabel = (TextView) v.findViewById(R.id.member_information_proposed_label);
                proposedCallingLabel.setVisibility(View.GONE);
                proposedCalling.setVisibility(View.GONE);
            }
            createViewItems(v, member);
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

    public void createViewItems(View v, final Member member) {
        /* Create xml for bottom sheet. */
        TableLayout tableLayout = (TableLayout) v.findViewById(R.id.member_information_table_layout);
        tableLayout.setGravity(Gravity.CENTER);
        /* Set the horizontal separating border. */
        tableLayout.addView(getBorderView());
        /* For phone */
        if((member.getIndividualPhone() == null || member.getIndividualPhone().isEmpty()) &&
           (member.getHouseholdPhone() == null || member.getHouseholdPhone().isEmpty())) {
                TextView noData = getNoDataTextView(getResources().getText(R.string.no_phone_number).toString());
                TableRow tableRow = getTableRow();
                tableRow.addView(noData);
                tableLayout.addView(tableRow);
        } else {
            /* Individual phone. */
            String phone = member.getIndividualPhone();
            if(phone != null && !phone.isEmpty()) {
                /* Phone number. */
                TextView individualPhoneView = getPhoneTextView(phone);
                /* SMS image button. */
                ImageButton individualPhoneImageButton = getPhoneImageButton(phone);
                /* Add all items to tablerow. */
                TableRow tableRow = getTableRow();
                tableRow.addView(individualPhoneView);
                tableRow.addView(individualPhoneImageButton);
                tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                /* Get underneath text. */
                TableRow underRow = getTableRow();
                underRow.addView(getIndividualHouseholdTextView(getResources().getText(R.string.list_display_options_individual).toString()));
                tableLayout.addView(underRow);
            }
            /* Home phone. */
            phone = member.getHouseholdPhone();
            if(phone != null && !phone.isEmpty()) {
                /* Phone number. */
                TextView homePhoneView = getPhoneTextView(phone);
                /* SMS image button. */
                ImageButton homePhoneImageButton = getPhoneImageButton(phone);
                /* Add all items to tablerow. */
                TableRow tableRow = getTableRow();
                tableRow.addView(homePhoneView);
                tableRow.addView(homePhoneImageButton);
                tableLayout.addView(tableRow);
                /* Get underneath text. */
                TableRow underRow = getTableRow();
                underRow.addView(getIndividualHouseholdTextView(getResources().getText(R.string.household).toString()));
                tableLayout.addView(underRow);
            }
        }
        /* For Email */
        /* Set the horizontal separating border. */
        tableLayout.addView(getBorderView());
        if((member.getIndividualEmail() == null || member.getIndividualEmail().isEmpty()) &&
           (member.getHouseholdEmail() == null || member.getHouseholdEmail().isEmpty())) {
            TextView noData = getNoDataTextView(getResources().getText(R.string.no_email_address).toString());
            TableRow tableRow = getTableRow();
            tableRow.addView(noData);
            tableLayout.addView(tableRow);
        } else {
            String email = member.getIndividualEmail();
            if(email != null && !email.isEmpty()) {
                /* Individual email text. */
                TextView individualEmailView = getEmailTextView(email);
                /* Under email text. */
                TextView individualTextView = getIndividualHouseholdTextView(getResources().getText(R.string.list_display_options_individual).toString());
                /* Add all items to tableRow. */
                TableRow tableRow = getTableRow();
                tableRow.addView(individualEmailView);
                tableLayout.addView(tableRow);
                TableRow underRow = getTableRow();
                underRow.addView(individualTextView);
                tableLayout.addView(underRow);
            }
            email = member.getHouseholdEmail();
            if(email != null && !email.isEmpty()) {
                /* Home email text. */
                TextView householdEmailView = getEmailTextView(email);
                /* Under email text. */
                TextView householdTextView = getIndividualHouseholdTextView(getResources().getText(R.string.household).toString());
                /* Add all items to tableRow. */
                TableRow tableRow = getTableRow();
                tableRow.addView(householdEmailView);
                TableRow underRow = getTableRow();
                underRow.addView(householdTextView);
                tableLayout.addView(underRow);
            }
        }
        /* Add address to the view. */
        tableLayout.addView(getBorderView());
        if(member.getStreetAddress() != null && member.getStreetAddress().length() > 0) {
            try {
                /* Get address and encode it. */
                final String strAddress = member.getStreetAddress();
                final String encodedAddress = URLEncoder.encode(strAddress, "UTF-8");
                TextView textView = getEmailTextView(member.getStreetAddress());
                textView.setPadding(45, 30, 0, 20);
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.marker_pin_blue, 0, 0, 0);
                Linkify.addLinks(textView, Linkify.MAP_ADDRESSES);
                /* Set the onclick listener. */
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initGoogleMap(encodedAddress);
                    }
                });
                /* Add items to tablerow and then table. */
                TableRow tableRow = getTableRow();
                tableRow.addView(textView);
                tableLayout.addView(tableRow);
            } catch (Error | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            /* Add no address information. */
            TextView textView = getNoDataTextView(getResources().getText(R.string.no_address).toString());
            TableRow tableRow = getTableRow();
            tableRow.addView(textView);
            tableLayout.addView(tableRow);
        }
    }

    /* Phone Number link. */
    private TextView getPhoneTextView(String content) {
        TextView textView = getTextView(content);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 20f;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        Linkify.addLinks(textView, Linkify.PHONE_NUMBERS);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(45, 30, 0, 0);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.phone_blue, 0, 0, 0);
        textView.setCompoundDrawablePadding(30);
        return textView;
    }

    /* The SMS Text Message button next to the phone number. */
    private ImageButton getPhoneImageButton(final String content) {
        ImageButton imageButton = new ImageButton(this.getContext());
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 0f;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        imageButton.setLayoutParams(layoutParams);
        imageButton.setBackgroundColor(getResources().getColor(R.color.ldstools_white));
        imageButton.setContentDescription("sms message");
        imageButton.setPadding(0,30,30, 0);
        imageButton.setImageResource(R.drawable.sms_blue);
        if(content != null && content.length() > 0) {
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendSMS(content);
                }
            });
        }
        return imageButton;
    }

    /* Email link text View. */
    private TextView getEmailTextView(String content) {
        TextView textView = getTextView(content);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        Linkify.addLinks(textView, Linkify.EMAIL_ADDRESSES);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(45, 30, 0, 0);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mail_blue, 0, 0, 0);
        textView.setCompoundDrawablePadding(30);
        return textView;
    }

    /* Underneath text showing Individual or Household. */
    private TextView getIndividualHouseholdTextView(String content) {
        TextView textView = getTextView(content);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1f;
        textView.setLayoutParams(layoutParams);
        textView.setPadding(160, 0, 0, 20);
        textView.setTextColor(getResources().getColor(R.color.ldstools_gray_light));
        return textView;
    }

    /* Generic TableRow. */
    private TableRow getTableRow() {
        return new TableRow(this.getContext());
    }

    /* Generic TextView creation. */
    private TextView getTextView(String content) {
       TextView textView = new TextView(this.getContext());
        textView.setEms(30);
        textView.setTextColor(getResources().getColor(R.color.ldstools_black));
        if(content != null && !content.isEmpty()) {
            textView.setText(content);
        }
        return textView;
    }

    /* TextView for no data. */
    private TextView getNoDataTextView(String content) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        TextView textView = getTextView(content);
        textView.setLayoutParams(layoutParams);
        textView.setPadding(25, 30, 0, 30);
        return textView;
    }

    /* Separating horizontal border. */
    private View getBorderView() {
        View view = new View(this.getContext());
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(getResources().getColor(R.color.ldstools_gray_light));
        return view;
    }
}