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
import java.util.List;
import java.util.logging.Handler;

import teamenglify.englify.DataService.DownloadService;
import teamenglify.englify.DataService.ListingDataService;
import teamenglify.englify.DataService.LoadService;
import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ImageView noContentImage;
    private String mParam1;
    private String mParam2;
    private String listingType;
    private Handler mHandler;


    public ListingFragment() {
        // Required empty public constructor
    }

    public static ListingFragment newInstance(String param1, String param2) {
        ListingFragment fragment = new ListingFragment();
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
        //inflate view
        View view = inflater.inflate(R.layout.fragment_listing, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        noContentImage = (ImageView) view.findViewById(R.id.noContentImage);
        //check listing type on local instance
        if (listingType == null) {
            listingType = mainActivity.currentListingType;
        }
        //update mainActivity currentListingType (back stack) if not the same
        if (listingType != null && mainActivity.currentListingType != listingType) {
            mainActivity.currentListingType = listingType;
        }
        Log.d("Englify", "Class ListingFragment: Method onPreExecute(): Loading listing " + listingType);
        //set the correct Title in action bar
        String title = mainActivity.currentListingType + " " + "Listing";
        mainActivity.getSupportActionBar().setTitle(title);
        //start download of Grade Listing if the local memory does not have the file
        if (listingType.equalsIgnoreCase("Grade") && !LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing))) {
            new DownloadService().execute(listingType);
        }
        //Use background Thread to load files when data is ready.
        HandlerThread thread = new HandlerThread("ListingFragmentBackgroundThread");
        thread.start();
        Looper looper = thread.getLooper()
        mHandler = new Handler(looper);
        listingAdapter = new ListingAdapter(listings, listingType);
        recyclerView.setAdapter(listingAdapter);
        //load additional settings
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }
}
