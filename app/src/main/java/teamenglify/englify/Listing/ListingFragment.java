package teamenglify.englify.Listing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.DataService.DownloadService;
import teamenglify.englify.DataService.ListingDataService;
import teamenglify.englify.DataService.LoadService;
import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.R;

import static teamenglify.englify.DataService.DataManager.*;
import static teamenglify.englify.MainActivity.currentDirectory;
import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingFragment extends Fragment {
    //Fixed variables to be used to determine listing type
    public static final int GRADE_LISTING = 0;
    public static final int LESSON_LISTING = 1;
    public static final int MODULE_LISTING = 2;
    public static final int READ_LISTING = 3;
    public static final int VOCAB_LISTING = 4;
    public static final int EXERCISE_LISTING = 5;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ImageView noContentImage;
    private int listingType;
    private String gradeSelected;
    private String lessonSelected;
    private String moduleSelected;
    private String readOrVocabPartSelected;


    public ListingFragment() {
        // Required empty public constructor
    }

    public static ListingFragment newInstance(int listingType, String gradeSelected, String lessonSelected, String moduleSelected, String readOrVocabPartSelected) {
        ListingFragment fragment = new ListingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, listingType);
        args.putString(ARG_PARAM2, gradeSelected);
        args.putString(ARG_PARAM3, lessonSelected);
        args.putString(ARG_PARAM4, moduleSelected);
        args.putString(ARG_PARAM5, readOrVocabPartSelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingType = getArguments().getInt(ARG_PARAM1);
            gradeSelected = getArguments().getString(ARG_PARAM2);
            lessonSelected = getArguments().getString(ARG_PARAM3);
            moduleSelected = getArguments().getString(ARG_PARAM4);
            readOrVocabPartSelected = getArguments().getString(ARG_PARAM5);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate view
        View view = inflater.inflate(R.layout.fragment_listing, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        noContentImage = (ImageView) view.findViewById(R.id.noContentImage);
        Log.d("Englify", "Class ListingFragment: Method onCreateView(): Loading listing " + listingType);
        //get the listings based on which listingType
        ArrayList<String> listings = new ArrayList<String>();
        if (listingType == GRADE_LISTING) {
            //set the correct Title in action bar
            mainActivity.getSupportActionBar().setTitle("Grade Listing");
            ArrayList<Grade> grades = new DataManager().getListing();
            Log.d("Englify", "Class ListingFragment: Method onCreateView(): Received ArrayList<Grade>: " + grades.toString());
            //get name of grades into an ArrayList<String> listings
            for (Grade grade : grades) {
                listings.add(grade.name);
            }
        } if (listingType == LESSON_LISTING) {
            //get the associated grade
            Grade grade = new DataManager().getGrade(gradeSelected);
        }
        //sort the listings
        Collections.sort(listings);
        listingAdapter = new ListingAdapter(listings, listingType);
        recyclerView.setAdapter(listingAdapter);
        //load additional settings
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }
}
