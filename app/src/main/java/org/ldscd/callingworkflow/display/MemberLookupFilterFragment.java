package org.ldscd.callingworkflow.display;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView;
import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.FilterOption;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MemberLookupFilterFragment extends DialogFragment {
    /* Fields. */
    public static final String NUMBER_OF_CALLINGS = "numberOfCallings";
    public static final String TIME_IN_CALLING = "timeInCalling";
    public static final String HIGH_PRIEST = "highPriest";
    public static final String ELDERS = "elders";
    public static final String PRIESTS = "priests";
    public static final String TEACHERS = "teachers";
    public static final String DEACONS = "deacons";
    public static final String RELIEF_SOCIETY = "reliefSociety";
    public static final String LAUREL = "laurel";
    public static final String MIA_MAID = "miaMaid";
    public static final String BEEHIVE = "beehive";
    public static final String TWELVE_EIGHTEEN = "twelveEighteen";
    public static final String EIGHTEEN_PLUS = "eighteenPlus";
    public static final String MALE = "male";
    public static final String FEMALE = "female";

    private FilterOption filterOption;

    private OnFragmentInteractionListener mListener;
    private View view = null;

    /* Constructor(s). */
    public MemberLookupFilterFragment() {
        // Required empty public constructor
    }

    public static MemberLookupFilterFragment newInstance(FilterOption filterOption) {
        MemberLookupFilterFragment fragment = new MemberLookupFilterFragment();
        Bundle args = new Bundle();
        if(filterOption != null) {
            args.putBooleanArray(NUMBER_OF_CALLINGS, filterOption.getNumberCallings());
            args.putDouble(TIME_IN_CALLING, filterOption.getTimeInCalling());
            args.putBoolean(HIGH_PRIEST, filterOption.isHighPriest());
            args.putBoolean(ELDERS, filterOption.isElders());
            args.putBoolean(PRIESTS, filterOption.isPriests());
            args.putBoolean(TEACHERS, filterOption.isTeachers());
            args.putBoolean(DEACONS, filterOption.isDeacons());
            args.putBoolean(RELIEF_SOCIETY, filterOption.isReliefSociety());
            args.putBoolean(LAUREL, filterOption.isLaurel());
            args.putBoolean(MIA_MAID, filterOption.isMiaMaid());
            args.putBoolean(BEEHIVE, filterOption.isBeehive());
            args.putBoolean(TWELVE_EIGHTEEN, filterOption.isTwelveEighteen());
            args.putBoolean(EIGHTEEN_PLUS, filterOption.isEighteenPlus());
            args.putBoolean(MALE, filterOption.isMale());
            args.putBoolean(FEMALE, filterOption.isFemale());
            fragment.setArguments(args);
        }
        return fragment;
    }

    /* Methods. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterOption = new FilterOption(getArguments().getBooleanArray(NUMBER_OF_CALLINGS),
                    getArguments().getInt(TIME_IN_CALLING), getArguments().getBoolean(HIGH_PRIEST),
                    getArguments().getBoolean(ELDERS), getArguments().getBoolean(PRIESTS),
                    getArguments().getBoolean(TEACHERS), getArguments().getBoolean(DEACONS),
                    getArguments().getBoolean(RELIEF_SOCIETY), getArguments().getBoolean(LAUREL),
                    getArguments().getBoolean(MIA_MAID), getArguments().getBoolean(BEEHIVE),
                    getArguments().getBoolean(TWELVE_EIGHTEEN), getArguments().getBoolean(EIGHTEEN_PLUS),
                    getArguments().getBoolean(MALE), getArguments().getBoolean(FEMALE));
                    //getArguments().getBoolean(HIGH_PRIEST), getArguments().getBoolean(HIGH_PRIEST));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate the view. */
        view = inflater.inflate(R.layout.fragment_member_lookup_filter, container, false);
        wireUpUIComponents(view);
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void wireUpUIComponents(final View view) {
        /* Number of Callings. */
        if(filterOption.getNumberCallings() == null) {
            filterOption.setNumberCallings(new boolean[] {false, false, false, false });
        }
        final TextView zero = (TextView)view.findViewById(R.id.member_lookup_filter_calling_zero);
        setToggleButtonColor(zero, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[0]);

        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.getNumberCallings()[0] = !filterOption.getNumberCallings()[0];
                setToggleButtonColor(zero, filterOption.getNumberCallings()[0]);
            }
        });
 
        final TextView one = (TextView)view.findViewById(R.id.member_lookup_filter_calling_one);
        setToggleButtonColor(one, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[1]);

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.getNumberCallings()[1] = !filterOption.getNumberCallings()[1];
                setToggleButtonColor(one, filterOption.getNumberCallings()[1]);
            }
        });
        final TextView two = (TextView)view.findViewById(R.id.member_lookup_filter_calling_two);
        setToggleButtonColor(two, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[2]);
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.getNumberCallings()[2] = !filterOption.getNumberCallings()[2];
                setToggleButtonColor(two, filterOption.getNumberCallings()[2]);
            }
        });
        final TextView threePlus = (TextView)view.findViewById(R.id.member_lookup_filter_calling_three_plus);
        setToggleButtonColor(threePlus, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[3]);
        threePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.getNumberCallings()[3] = !filterOption.getNumberCallings()[3];
                setToggleButtonColor(threePlus, filterOption.getNumberCallings()[3]);
            }
        });
        /* Years in Callings. */
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.member_lookup_filter_years_in_calling);
        seekBar.setProgress(getProgressStep(filterOption.getTimeInCalling()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView timeInCallingLabel = (TextView) view.findViewById(R.id.member_lookup_filter_years_in_calling_label);
                String display = "";
                double stepSize = progress;
                if(progress < 12) {
                    display = progress + " Month(s)";
                } else {
                    stepSize = getTimeInCalling(progress);
                    display = stepSize + " Year(s)";
                }
                filterOption.setTimeInCalling(getProgressStep(stepSize));
                timeInCallingLabel.setText(display);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        /* Twelve to Eighteen. */
        final TextView twelveEighteenButton = (TextView)view.findViewById(R.id.member_lookup_filter_twelve_to_eighteen);
        setToggleButtonColor(twelveEighteenButton, filterOption.isTwelveEighteen());
        /* Eighteen Plus. */
        final TextView eighteenPlusButton = (TextView)view.findViewById(R.id.member_lookup_filter_eighteen_plus);
        setToggleButtonColor(eighteenPlusButton, filterOption.isEighteenPlus());
        eighteenPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setEighteenPlus(!filterOption.isEighteenPlus());
                ageSearchToggleView(false, filterOption.isEighteenPlus(), twelveEighteenButton, eighteenPlusButton);
            }
        });
        twelveEighteenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setTwelveEighteen(!filterOption.isTwelveEighteen());
                ageSearchToggleView(filterOption.isTwelveEighteen(), false, twelveEighteenButton, eighteenPlusButton);
            }
        });
        ageSearchToggleView(filterOption.isTwelveEighteen(), filterOption.isEighteenPlus(), twelveEighteenButton, eighteenPlusButton);

        /* High Priest */
        final TextView highPriestButton = (TextView)view.findViewById(R.id.member_lookup_filter_high_priest);
        setChecked(highPriestButton, filterOption.isHighPriest());
        highPriestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setHighPriest(!filterOption.isHighPriest());
                setToggleButtonColor(highPriestButton, filterOption.isHighPriest());
            }
        });
        /* Elders */
        final TextView eldersButton = (TextView)view.findViewById(R.id.member_lookup_filter_elders_quorum);
        setChecked(eldersButton,filterOption.isElders());
        eldersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setElders(!filterOption.isElders());
                setToggleButtonColor(eldersButton, filterOption.isElders());
            }
        });
        /* Priests */
        final TextView priestButton = (TextView)view.findViewById(R.id.member_lookup_filter_priest);
        setChecked(priestButton, filterOption.isPriests());
        priestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setPriests(!filterOption.isPriests());
                setToggleButtonColor(priestButton, filterOption.isPriests());
            }
        });
        /* Teachers */
        final TextView teacherButton = (TextView)view.findViewById(R.id.member_lookup_filter_teacher);
        setChecked(teacherButton, filterOption.isTeachers());
        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setTeachers(!filterOption.isTeachers());
                setToggleButtonColor(teacherButton, filterOption.isTeachers());
            }
        });
        /* Deacon */
        final TextView deaconButton = (TextView)view.findViewById(R.id.member_lookup_filter_deacon);
        setChecked(deaconButton, filterOption.isDeacons());
        deaconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setDeacons(!filterOption.isDeacons());
                setToggleButtonColor(deaconButton, filterOption.isDeacons());
            }
        });
        /* Relief Society */
        final TextView reliefSocietyButton = (TextView)view.findViewById(R.id.member_lookup_filter_relief_society);
        setChecked(reliefSocietyButton, filterOption.isReliefSociety());
        reliefSocietyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setReliefSociety(!filterOption.isReliefSociety());
                setToggleButtonColor(reliefSocietyButton, filterOption.isReliefSociety());
            }
        });
        /* Laurels */
        final TextView laurelButton = (TextView)view.findViewById(R.id.member_lookup_filter_laurel);
        setChecked(laurelButton, filterOption.isLaurel());
        laurelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setLaurel(!filterOption.isLaurel());
                setToggleButtonColor(laurelButton, filterOption.isLaurel());
            }
        });
        /* Mia Maids */
        final TextView miaMaidButton = (TextView)view.findViewById(R.id.member_lookup_filter_mia_maid);
        setChecked(miaMaidButton, filterOption.isMiaMaid());
        miaMaidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setMiaMaid(!filterOption.isMiaMaid());
                setToggleButtonColor(miaMaidButton, filterOption.isMiaMaid());
            }
        });
        /* Beehives */
        final TextView beehiveButton = (TextView)view.findViewById(R.id.member_lookup_filter_beehive);
        setChecked(beehiveButton, filterOption.isBeehive());
        beehiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setBeehive(!filterOption.isBeehive());
                setToggleButtonColor(beehiveButton, filterOption.isBeehive());
            }
        });
         /* Male. */
        final TextView maleButton = (TextView)view.findViewById(R.id.member_lookup_filter_male);
        setToggleButtonColor(maleButton, filterOption.isMale());
        /* Female. */
        final TextView femaleButton = (TextView)view.findViewById(R.id.member_lookup_filter_female);
        setToggleButtonColor(femaleButton, filterOption.isFemale());
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setFemale(!filterOption.isFemale());
                auxiliaryToggleView(false, filterOption.isFemale(), maleButton, femaleButton);
                if (!filterOption.isFemale()) {
                    setToggleButtonOff(reliefSocietyButton, laurelButton, miaMaidButton, beehiveButton, null);
                }
            }
        });
       
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterOption.setMale(!filterOption.isMale());
                auxiliaryToggleView(filterOption.isMale(), false, maleButton, femaleButton);
                if (!filterOption.isMale()) {
                    setToggleButtonOff(highPriestButton, eldersButton, priestButton, teacherButton, deaconButton);
                }
            }
        });
        auxiliaryToggleView(filterOption.isMale(), filterOption.isFemale(), maleButton, femaleButton);
        if(!filterOption.isMale()) {
            setToggleButtonOff(highPriestButton, eldersButton, priestButton, teacherButton, deaconButton);
        }
        if(!filterOption.isFemale()) {
            setToggleButtonOff(reliefSocietyButton, laurelButton, miaMaidButton, beehiveButton, null);
        }
        /* Done Button */
        Button doneButton = (Button)view.findViewById(R.id.member_lookup_filter_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClick();
            }
        });
    }

    public void onDoneButtonClick() {
        if (mListener != null) {
            mListener.onFragmentInteraction(filterOption);
        }
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(FilterOption filterOption);
    }

    /* Creates a radial before between the Male and Female options. Displays the appropriate auxiliaries. */
    public void auxiliaryToggleView(boolean male, boolean female, TextView maleButton, TextView femaleButton) {
        TableLayout maleLayout;
        TableLayout femaleLayout;
        if(male) {
            setToggleButtonColor(femaleButton, false);
            setToggleButtonColor(maleButton, true);
            filterOption.setFemale(false);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(VISIBLE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(GONE);
        } else if(female) {
            setToggleButtonColor(femaleButton, true);
            setToggleButtonColor(maleButton, false);
            filterOption.setMale(false);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(GONE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(VISIBLE);
        } else {
            setToggleButtonColor(femaleButton, false);
            setToggleButtonColor(maleButton, false);
            filterOption.setMale(false);
            filterOption.setFemale(false);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(GONE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(GONE);
        }
    }

    public void ageSearchToggleView(boolean twelveToEighteen, boolean eighteenPlus, TextView twelveToEighteenButton, TextView eighteenPlusButton) {
        if(twelveToEighteen) {
            setToggleButtonColor(twelveToEighteenButton, true);
            setToggleButtonColor(eighteenPlusButton, false);
            filterOption.setEighteenPlus(false);
        } else if(eighteenPlus) {
            setToggleButtonColor(twelveToEighteenButton, false);
            setToggleButtonColor(eighteenPlusButton, true);
            filterOption.setTwelveEighteen(false);
        } else {
            setToggleButtonColor(twelveToEighteenButton, false);
            setToggleButtonColor(eighteenPlusButton, false);
            filterOption.setTwelveEighteen(false);
            filterOption.setEighteenPlus(false);
        }
    }

    private void setToggleButtonColor(TextView button, boolean filterOption) {
        if(filterOption) {
            setChecked(button, true);
            button.setTextColor(getResources().getColor(R.color.about_accent));
        } else {
            setChecked(button, false);
            button.setTextColor(getResources().getColor(R.color.ldstools_black));
        }
    }

    private void setToggleButtonOff(TextView button_one, TextView button_two, TextView button_three, TextView button_four, TextView button_five) {
        setChecked(button_one, false);
        setChecked(button_two, false);
        setChecked(button_three, false);
        setChecked(button_four, false);
        if(button_five != null) {
            setChecked(button_five, false);
        }
    }

    private void setChecked(TextView button, boolean checked) {
        if(checked) {
            button.setTextColor(getResources().getColor(R.color.ldstools_black));
            button.setBackground(getResources().getDrawable(R.drawable.selected_filter_background));
        } else {
            button.setTextColor(getResources().getColor(R.color.ldstools_gray_dark));
            button.setBackground(null);
        }
    }

    private double getTimeInCalling(int progress) {
        double stepSize = 0;
        switch(progress) {
            case 12:
                stepSize = 1;
                break;
            case 13:
                stepSize = 1.5;
                break;
            case 14:
                stepSize = 2;
                break;
            case 15:
                stepSize = 2.5;
                break;
            case 16:
                stepSize = 3;
                break;
            case 17:
                stepSize = 3.5;
                break;
            case 18:
                stepSize = 4;
                break;
            case 19:
                stepSize = 4.5;
                break;
            case 20:
                stepSize = 5;
                break;
        }
        return stepSize;
    }

    private int getProgressStep(double stepSize) {
        int progress = 0;
        if(stepSize == 12) {
            progress = 1;
        } else if(stepSize == 1.5) {
            progress = 13;
        } else if(stepSize == 2) {
            progress = 14;
        } else if(stepSize == 2.5) {
            progress = 15;
        } else if(stepSize == 3) {
            progress = 16;
        } else if(stepSize == 3.5) {
            progress = 17;
        } else if(stepSize == 4) {
            progress = 18;
        } else if(stepSize == 4.5) {
            progress = 19;
        } else if(stepSize == 5) {
            progress = 20;
        }
        return progress;
    }
}