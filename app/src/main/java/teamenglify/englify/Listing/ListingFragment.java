package teamenglify.englify.Listing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Collections;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;
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
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ImageView noContentImage;
    private int listingType;
    private Handler mHandler;
    private Object objectToLoad;


    public ListingFragment() {
        // Required empty public constructor
    }

    public static ListingFragment newInstance(int listingType, Object objectToLoad) {
        ListingFragment fragment = new ListingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, listingType);
        fragment.objectToLoad = objectToLoad;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingType = getArguments().getInt(ARG_PARAM1);
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
        mHandler = new Handler();
        //objectToLoad not present, download
        if (listingType == GRADE_LISTING) {
            //set the correct Title in action bar
            mainActivity.getSupportActionBar().setTitle("Grade Listing");
            new DataManager().getListing();
            Log.d("Englify", "Class ListingFragment: Method onCreateView(): Starting mBackgroundThread.");
            mHandler.post(mBackgroundThread);
        } else if (objectToLoad instanceof Grade && !((Grade) objectToLoad).isDownloaded) {
            Log.d("Englify", "Class ListingFragment: Method onCreateView(): Asking DataManager to get " + ((Grade)objectToLoad).name);
            new DataManager().getGrade((Grade) objectToLoad);
            Log.d("Englify", "Class ListingFragment: Method onCreateView(): Starting mBackgroundThread.");
            mHandler.post(mBackgroundThread);
        } else {
            mUpdateUIAfterDataLoaded(objectToLoad);
        }
        return view;
    }

    public void mUpdateUIAfterDataLoaded(Object object) {
        Log.d("Englify", "Class ListingFragment: Method mUpdateUIAfterDataLoaded(): Updating UI.");
        //get the listings based on which listingType
        if (listingType == GRADE_LISTING) {
            RootListing grades = (RootListing) object;
            Log.d("Englify", "Class ListingFragment: Method mUpdateUIAfterDataLoaded: Received RootListing: " + grades.grades.toString());
        }
        listingAdapter = new ListingAdapter(object, listingType);
        recyclerView.setAdapter(listingAdapter);
        //load additional settings
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    private Runnable mBackgroundThread = new Runnable() {
        @Override
        public void run() {
            if (mainActivity.downloadedObject != null) {
                objectToLoad = mainActivity.downloadedObject;
                mUpdateUIAfterDataLoaded(objectToLoad);
                mainActivity.downloadedObject = null;
                Log.d("Englify", "Class ListingFragment: Method mBackgroundThread: Found downloadedObject.");
            } else {
                mHandler.postDelayed(mBackgroundThread, 500);
                Log.d("Englify", "Class ListingFragment: Method mBackgroundThread: Looping.");
            }
            Log.d("Englify", "Class ListingFragment: Method mBackgroundThread: Loop is running.");
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mBackgroundThread);
    }
}
