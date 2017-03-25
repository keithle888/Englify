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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.getMainActivity;
import static teamenglify.englify.MainActivity.mainActivity;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingFragment extends Fragment {
    //Fixed variables to be used to determine listing type
    public static ListingFragment listingFragment;
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

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        noContentImage = (ImageView) view.findViewById(R.id.noContentImage);
        Log.d("Englify", "Class ListingFragment: Method onCreateView(): Loading listing " + listingType);
        mHandler = new Handler();
        //objectToLoad not present, download
        if (listingType == GRADE_LISTING && ((RootListing)objectToLoad).grades == null) {
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

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public void mUpdateUIAfterDataLoaded(Object object) {
        Log.d("Englify", "Class ListingFragment: Method mUpdateUIAfterDataLoaded(): Updating UI.");
        //get the listings based on which listingType
        if (listingType == GRADE_LISTING) {
            RootListing grades = (RootListing) object;
            listingAdapter = new ListingAdapter(object, listingType);
            recyclerView.setAdapter(listingAdapter);
            //load additional settings
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 2);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);
        }else if (listingType == LESSON_LISTING || listingType == READ_LISTING){

            listingAdapter = new ListingAdapter(object, listingType);
            recyclerView.setAdapter(listingAdapter);
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 1);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);

        }else {
            listingAdapter = new ListingAdapter(object, listingType);
            recyclerView.setAdapter(listingAdapter);
            mainActivity.mLayoutManager = new GridLayoutManager(mainActivity.getApplicationContext(), 1);
            recyclerView.setLayoutManager(mainActivity.mLayoutManager);
        }

        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //recyclerView.setLayoutManager(layoutManager);
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
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mBackgroundThread);
    }
}
