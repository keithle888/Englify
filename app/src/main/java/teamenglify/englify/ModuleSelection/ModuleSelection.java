package teamenglify.englify.ModuleSelection;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ModuleSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModuleSelection extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private int mParam1;
    MainActivity mainActivity;


    public ModuleSelection() {
        // Required empty public constructor
    }

    public static ModuleSelection newInstance(int param1) {
        ModuleSelection fragment = new ModuleSelection();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_module_selection, container, false);
        Button readingBtn = (Button) v.findViewById(R.id.ReadingBtn);
        Button vocabBtn = (Button) v.findViewById(R.id.VocabBtn);
        mainActivity = MainActivity.getMainActivity();
        mainActivity.getSupportActionBar().setTitle("Module Selection");

        readingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffffbb33"));
                mainActivity.getSupportActionBar().setTitle("Read Selection");
                //mainActivity.loadNextListing(ListingFragment.READ_LISTING);

            }
        });

        vocabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffffbb33"));
                mainActivity.getSupportActionBar().setTitle("Vocab Selection");
                //mainActivity.loadNextListing(ListingFragment.VOCAB_LISTING);
            }
        });
        return v;
    }
}
