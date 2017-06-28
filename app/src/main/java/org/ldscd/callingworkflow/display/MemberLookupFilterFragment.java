package org.ldscd.callingworkflow.display;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
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
        final ToggleButton zero = (ToggleButton)view.findViewById(R.id.member_lookup_filter_calling_zero);
        setToggleButtonColor(zero, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[0]);

        zero.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.getNumberCallings()[0] = isChecked;
                setToggleButtonColor(zero, isChecked);
            }
        });
        final ToggleButton one = (ToggleButton)view.findViewById(R.id.member_lookup_filter_calling_one);
        setToggleButtonColor(one, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[1]);

        one.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.getNumberCallings()[1] = isChecked;
                setToggleButtonColor(one, isChecked);
            }
        });
        final ToggleButton two = (ToggleButton)view.findViewById(R.id.member_lookup_filter_calling_two);
        setToggleButtonColor(two, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[2]);
        two.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.getNumberCallings()[2] = isChecked;
                setToggleButtonColor(two, isChecked);
            }
        });
        final ToggleButton threePlus = (ToggleButton)view.findViewById(R.id.member_lookup_filter_calling_three_plus);
        setToggleButtonColor(threePlus, filterOption.getNumberCallings() != null && filterOption.getNumberCallings()[3]);
        threePlus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.getNumberCallings()[3] = isChecked;
                setToggleButtonColor(threePlus, isChecked);
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
        final ToggleButton twelveEighteenButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_twelve_to_eighteen);
        setToggleButtonColor(twelveEighteenButton, filterOption.isTwelveEighteen());
        /* Eighteen Plus. */
        final ToggleButton eighteenPlusButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_eighteen_plus);
        setToggleButtonColor(eighteenPlusButton, filterOption.isEighteenPlus());
        eighteenPlusButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setEighteenPlus(isChecked);
                ageSearchToggleView(false, isChecked, twelveEighteenButton, eighteenPlusButton);
            }
        });
        twelveEighteenButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setTwelveEighteen(isChecked);
                ageSearchToggleView(isChecked, false, twelveEighteenButton, eighteenPlusButton);
            }
        });
        ageSearchToggleView(filterOption.isTwelveEighteen(), filterOption.isEighteenPlus(), twelveEighteenButton, eighteenPlusButton);
        /* Male. */
        final ToggleButton maleButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_male);
        setToggleButtonColor(maleButton, filterOption.isMale());
        /* Female. */
        final ToggleButton femaleButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_female);
        setToggleButtonColor(femaleButton, filterOption.isFemale());
        femaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setFemale(isChecked);
                auxiliaryToggleView(false, isChecked, maleButton, femaleButton);
            }
        });
        maleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setMale(isChecked);
                auxiliaryToggleView(isChecked, false, maleButton, femaleButton);
            }
        });
        auxiliaryToggleView(filterOption.isMale(), filterOption.isFemale(), maleButton, femaleButton);

        /* High Priest */
        final ToggleButton highPriestButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_high_priest);
        highPriestButton.setChecked(filterOption.isHighPriest());
        highPriestButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setHighPriest(isChecked);
                setToggleButtonColor(highPriestButton, isChecked);
            }
        });
        /* Elders */
        final ToggleButton eldersButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_elders_quorum);
        eldersButton.setChecked(filterOption.isElders());
        eldersButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setElders(isChecked);
                setToggleButtonColor(eldersButton, isChecked);
            }
        });
        /* Priests */
        final ToggleButton priestButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_priest);
        priestButton.setChecked(filterOption.isPriests());
        priestButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setPriests(isChecked);
                setToggleButtonColor(priestButton, isChecked);
            }
        });
        /* Teachers */
        final ToggleButton teacherButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_teacher);
        teacherButton.setChecked(filterOption.isTeachers());
        teacherButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setTeachers(isChecked);
                setToggleButtonColor(teacherButton, isChecked);
            }
        });
        /* Deacon */
        final ToggleButton deaconButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_deacon);
        deaconButton.setChecked(filterOption.isDeacons());
        deaconButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setDeacons(isChecked);
                setToggleButtonColor(deaconButton, isChecked);
            }
        });
        /* Relief Society */
        final ToggleButton reliefSocietyButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_relief_society);
        reliefSocietyButton.setChecked(filterOption.isReliefSociety());
        reliefSocietyButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setReliefSociety(isChecked);
                setToggleButtonColor(reliefSocietyButton, isChecked);
            }
        });
        /* Laurels */
        final ToggleButton laurelButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_laurel);
        laurelButton.setChecked(filterOption.isLaurel());
        laurelButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setLaurel(isChecked);
                setToggleButtonColor(laurelButton, isChecked);
            }
        });
        /* Mia Maids */
        final ToggleButton miaMaidButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_mia_maid);
        miaMaidButton.setChecked(filterOption.isMiaMaid());
        miaMaidButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setMiaMaid(isChecked);
                setToggleButtonColor(miaMaidButton, isChecked);
            }
        });
        /* Beehives */
        final ToggleButton beehiveButton = (ToggleButton)view.findViewById(R.id.member_lookup_filter_beehive);
        beehiveButton.setChecked(filterOption.isBeehive());
        beehiveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOption.setBeehive(isChecked);
                setToggleButtonColor(beehiveButton, isChecked);
            }
        });
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
    public void auxiliaryToggleView(boolean male, boolean female, ToggleButton maleButton, ToggleButton femaleButton) {
        TableLayout maleLayout;
        TableLayout femaleLayout;
        if(male) {
            setToggleButtonColor(femaleButton, false);
            setToggleButtonColor(maleButton, true);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(VISIBLE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(GONE);
        } else if(female) {
            setToggleButtonColor(femaleButton, true);
            setToggleButtonColor(maleButton, false);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(GONE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(VISIBLE);
        } else {
            setToggleButtonColor(femaleButton, false);
            setToggleButtonColor(maleButton, false);
            maleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_priesthood_container);
            maleLayout.setVisibility(GONE);
            femaleLayout = (TableLayout)view.findViewById(R.id.member_lookup_filter_relief_society_container);
            femaleLayout.setVisibility(GONE);
        }
    }

    public void ageSearchToggleView(boolean twelveToEighteen, boolean eighteenPlus, ToggleButton twelveToEighteenButton, ToggleButton eighteenPlusButton) {
        if(twelveToEighteen) {
            setToggleButtonColor(twelveToEighteenButton, true);
            setToggleButtonColor(eighteenPlusButton, false);
        } else if(eighteenPlus) {
            setToggleButtonColor(twelveToEighteenButton, false);
            setToggleButtonColor(eighteenPlusButton, true);
        } else {
            setToggleButtonColor(twelveToEighteenButton, false);
            setToggleButtonColor(eighteenPlusButton, false);
        }
    }

    private void setToggleButtonColor(ToggleButton button, boolean isChecked) {
        if(isChecked) {
            button.setChecked(true);
            button.setTextColor(getResources().getColor(R.color.about_accent));
        } else {
            button.setChecked(false);
            button.setTextColor(getResources().getColor(R.color.ldstools_black));
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