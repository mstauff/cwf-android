package org.ldscd.callingworkflow.display;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.ldscd.callingworkflow.R;
import org.ldscd.callingworkflow.model.Member;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link MemberLookupButtonFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberLookupButtonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberLookupButtonFragment extends Fragment implements
        MemberLookupFragment.memberLookupFragmentInteractionListener  {
    /* Fields. */
    private Member member;
    private OnFragmentInteractionListener mListener;

    /* Constructor(s). */
    public MemberLookupButtonFragment() {
        // Required empty public constructor
    }

    /* Methods */

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MemberLookupButtonFragment.
     */
    public static MemberLookupButtonFragment newInstance() {
        MemberLookupButtonFragment fragment = new MemberLookupButtonFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        /* Inflate the layout for this fragment. */
        View view = inflater.inflate(R.layout.fragment_member_lookup_button, container, false);


        ImageButton searchButton = (ImageButton) view.findViewById(R.id.member_lookup_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wireUpFragments(savedInstanceState);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Member member);
    }

    @Override
    public void onFragmentInteraction(Member member) {
        this.member = member;
    }

    private void wireUpFragments(Bundle savedInstanceState) {
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            MemberLookupFragment searchFragment = new MemberLookupFragment();
            if(member != null && member.getIndividualId() > 0) {
                Bundle args = new Bundle();
                args.putLong(CallingDetailSearchFragment.INDIVIDUAL_ID, member.getIndividualId());
                searchFragment.setArguments(args);
            }
           /* getFragmentManager().beginTransaction()
                    .add(R.id.calling_detail_member_lookup_fragment, searchFragment)
                    .commit();*/
        }
    }
}
