package teamenglify.englify.Listing;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;


import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Exercise;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.lesson;
import static teamenglify.englify.MainActivity.mainActivity;

import android.support.v7.widget.GridLayoutManager;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingFragment extends Fragment {
    //Fixed variables to be used to determine listing type
    public static final int LIST_GRADES = 0;
    public static final int LIST_LESSONS = 1;
    public static final int LIST_MODULES = 2;
    public static final int LIST_READS = 3;
    public static final int LIST_VOCABS = 4;
    public static final int LIST_EXERCISES = 5;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String ARG_PARAM1 = "ARG_PARAM1";
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ListingAdapterLesson listingAdapterLesson;
    private ImageView noContentImage;
    private int listingType;
    private Object object_to_load; //When we are dealing with lessons or anything below


    public ListingFragment() {
        // Required empty public constructor
    }

    public static ListingFragment newInstance(int listingType) {
        ListingFragment fragment = new ListingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, listingType);
        fragment.setArguments(args);
        return fragment;
    }

    public static ListingFragment newInstance(int listingType, Object object_to_load) {
        ListingFragment fragment = new ListingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, listingType);
        fragment.object_to_load = object_to_load;
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
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        noContentImage = (ImageView) view.findViewById(R.id.noContentImage);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        updateActionBarTitle();
        //objectToLoad not present, download
        if (listingType == LIST_GRADES) {
            new DataManager().getListing();
        } else if (listingType == LIST_LESSONS) {
            //check if the list of lessons for the grade has been downloaded (using size)
            Grade selected_grade = (Grade) object_to_load;
            if (selected_grade.lessons.size() == 0) {
                new DataManager().download_list_of_lessons(selected_grade);
            } else {
                mUpdateUIAfterDataLoaded(selected_grade);
            }
        } else {
            mUpdateUIAfterDataLoaded();
        }
    }

    public void mUpdateUIAfterDataLoaded() {
        Log.d("Englify", "Class ListingFragment: Method mUpdateUIAfterDataLoaded(): Updating UI.");
        //get the listings based on which listingType
        if (listingType == LIST_GRADES) {
            Object object = LocalSave.loadObject(getString(R.string.S3_Object_Listing));
            RootListing grades = (RootListing) object;
            listingAdapter = new ListingAdapter(object, listingType);
            recyclerView.setAdapter(listingAdapter);
            //load additional settings
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 2);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);
        } else{
            listingAdapter = new ListingAdapter(object_to_load, listingType);
            recyclerView.setAdapter(listingAdapter);
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 1);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);
        }
    }

    public void mUpdateUIAfterDataLoaded(Grade grade) {
        Log.d("Englify", "Class ListingFragment: Method mUpdateUIAfterDataLoaded(): Updating ListingFragment UI with the listing of lessons for " + grade.name + " with " + grade.lessons.size() + " lessons.");
        if (listingType == LIST_LESSONS) {
            mainActivity.getSupportActionBar().setTitle(grade.name);
            listingAdapterLesson = new ListingAdapterLesson(grade);
            recyclerView.setAdapter(listingAdapterLesson);
            //load additional settings
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 1);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);
        }
    }
    public void updateActionBarTitle() {
        String title = null;
        if (listingType == LIST_GRADES) {
            title = "Grade Listing";
        } else if (listingType == LIST_LESSONS) {
            title = "Lesson Listing";
        } else if (listingType == LIST_VOCABS) {
            title = "Vocab Listing";
        } else if (listingType == LIST_READS) {
            title = "Read Listing";
        } else if (listingType == LIST_EXERCISES) {
            title = "Exercise Listing";
        }
        if (title != null) {
            mainActivity.getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }



    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
