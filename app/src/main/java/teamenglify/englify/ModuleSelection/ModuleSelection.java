package teamenglify.englify.ModuleSelection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.R;
import android.widget.ImageButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ModuleSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModuleSelection extends Fragment {

    private int mParam1;
    private Lesson lesson;
    MainActivity mainActivity;
    private String previous_fragment_action_bar_title;


    public ModuleSelection() {
        // Required empty public constructor
    }

    public static ModuleSelection newInstance(Lesson lesson, String previous_action_bar_title) {
        ModuleSelection fragment = new ModuleSelection();
        Bundle args = new Bundle();
        fragment.lesson = lesson;
        fragment.previous_fragment_action_bar_title = previous_action_bar_title;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_module_selection, container, false);
        ImageButton readingBtn = (ImageButton) view.findViewById(R.id.ReadingBtn);
        ImageButton vocabBtn = (ImageButton) view.findViewById(R.id.VocabBtn);
        ImageButton execiseBtn = (ImageButton) view.findViewById(R.id.ExerciseBtn);
        mainActivity = MainActivity.getMainActivity();


        readingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_container,
                                ListingFragment.newInstance(ListingFragment.LIST_READS,
                                    lesson.findModule(getString(R.string.Conversation_Folder_Name)),
                                    mainActivity.getSupportActionBar().getTitle().toString()),
                                "READ_LISTING")
                        .addToBackStack(null)
                        .commit();
            }
        });

        vocabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_container,
                                ListingFragment.newInstance(ListingFragment.LIST_VOCABS,
                                    lesson.findModule(getString(R.string.Vocab_Folder_Name)),
                                    mainActivity.getSupportActionBar().getTitle().toString()),
                                "VOCAB_LISTING")
                        .addToBackStack(null)
                        .commit();
            }
        });

        execiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_container,
                                ListingFragment.newInstance(ListingFragment.LIST_EXERCISES,
                                        lesson.findModule("Exercise"),
                                        mainActivity.getSupportActionBar().getTitle().toString()),
                                "EXERCISE_LISTING")
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lesson != null) {
            mainActivity.getSupportActionBar().setTitle(previous_fragment_action_bar_title + ListingFragment.ACTION_BAR_DELIMITER + "L" + lesson.name);
        }
    }
}
