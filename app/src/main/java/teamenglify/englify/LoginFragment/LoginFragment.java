package teamenglify.englify.LoginFragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {



    public LoginFragment() {
        // Required empty public constructor
    }
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        //set title
        mainActivity.getSupportActionBar().setTitle("Englify Home");
        //set button listener
        Button getStartedButton = (Button) view.findViewById(R.id.get_started_button);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffffbb33"));
                //Get Main Activity to Open ListingFragment with GRADE_LISTING TAG
                mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, ListingFragment.newInstance(ListingFragment.LIST_GRADES), "GRADE_LISTING").addToBackStack(null).commit();
            }
        });
        return view;
    }
}
