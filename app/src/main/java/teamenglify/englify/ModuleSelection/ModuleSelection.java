package teamenglify.englify.ModuleSelection;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private String mParam1;
    private String mParam2;
    MainActivity mainActivity;


    public ModuleSelection() {
        // Required empty public constructor
    }


    public static ModuleSelection newInstance(String param1, String param2) {
        ModuleSelection fragment = new ModuleSelection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
                mainActivity.setCurrentListingType("readListing");
                mainActivity.setCurrentListingURL(mainActivity.parentFolder);
                mainActivity.loadNextListing();

            }
        });

        vocabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(Color.parseColor("#ffffbb33"));
                mainActivity.getSupportActionBar().setTitle("Vocab Selection");
                mainActivity.setCurrentListingType("vocabListing");
                mainActivity.setCurrentListingURL(mainActivity.parentFolder);
                mainActivity.loadNextListing();
            }
        });
        return v;
    }
}
