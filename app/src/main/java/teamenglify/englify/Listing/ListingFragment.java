package teamenglify.englify.Listing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;

import teamenglify.englify.DataService.ListingDataService;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

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
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ListingAdapter listingAdapter;
    private ImageView noContentImage;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String listingType;


    public ListingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListingFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        //initialize variables
        MainActivity mainActivity = MainActivity.getMainActivity();
        if (listingType == null) {
            listingType = mainActivity.getCurrentListingType();
        }
        Log.d("Fragment Loading:", listingType);
        if(listingType.equalsIgnoreCase("gradeListing")){
            MainActivity.getMainActivity().getSupportActionBar().setTitle("Grade Selection");
        } else if (listingType.equalsIgnoreCase("lessonListing")){
            MainActivity.getMainActivity().getSupportActionBar().setTitle("Lesson Selection");
        } else if (listingType.equalsIgnoreCase("vocabListing")){
            MainActivity.getMainActivity().getSupportActionBar().setTitle("Vocab Selection");
        } else if (listingType.equalsIgnoreCase("readListing")) {
            MainActivity.getMainActivity().getSupportActionBar().setTitle("Read Selection");
        }


        //inflate view
        View v = inflater.inflate(R.layout.fragment_listing, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        noContentImage = (ImageView) v.findViewById(R.id.noContentImage);

        //check for existing ArrayList<String>, if not, run ListingDataService (Thread)
        ArrayList<String> listOfChoices = getCurrentListMainActivity(listingType);

        if (listOfChoices == null || listOfChoices.size() == 0) {
            try {
                new asyncTask().execute();
            } catch (Exception e) {
                Log.d("Error", e.toString());
                new AlertDialog.Builder(MainActivity.getMainActivity())
                        .setTitle("An error has occurred")
                        .setMessage("Error on executing AsyncTask in ListingFragment. Check message on console")
                        .show();
            }
        } else {
            updateFragmentAfterDataLoaded();
        }
        return v;
    }

    @Override
    public void onDestroy() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    public void updateFragmentAfterDataLoaded() {
        //find the listOfChoices again, after the thread has loaded it onto the mainActivity.
        ArrayList<String> listOfChoices = getCurrentListMainActivity(listingType);
        ArrayList<String> listOfChoicesVocab = new ArrayList<>();

        if(listOfChoices.size()==0){
            noContentImage.setImageResource(R.drawable.logonocontent);
        } else {
            if(listingType.equalsIgnoreCase("vocabListing")){
                for(String choice : listOfChoices){
                    String [] tempArr = choice.split("-");
                    listOfChoicesVocab.add(tempArr[0]);
                }
                listingAdapter = new ListingAdapter(listOfChoicesVocab, listingType);
            } else {
                listingAdapter = new ListingAdapter(listOfChoices, listingType);
            }
            recyclerView.setAdapter(listingAdapter);
            //recyclerView.addItemDecoration( new VerticalSpaceDecorator(20));
            //recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    public ArrayList<String> getCurrentListMainActivity(String listingType) {
        ArrayList<String> returnList = new ArrayList<String>();
        if (listingType.equalsIgnoreCase("gradeListing")) {
            returnList = MainActivity.getMainActivity().gradeListing;
        } else if (listingType.equalsIgnoreCase("lessonListing")) {
            returnList = MainActivity.getMainActivity().lessonListing;
        } else if (listingType.equalsIgnoreCase("readListing")) {
            returnList = MainActivity.getMainActivity().readListing;
        } else if (listingType.equalsIgnoreCase("vocabListing")) {
            returnList = MainActivity.getMainActivity().vocabListing;
        }
        return returnList;
    }

    private class asyncTask extends AsyncTask <Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            //Start Progress Dialog Box
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading In Progress");
            progressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void...Void) {
            try {
                Runnable runnable = new ListingDataService(listingType);
                Thread myThread = new Thread(runnable);
                myThread.start();
                myThread.join();
            } catch (InterruptedException e) {
                Log.d("Error:", "with ListingDataService Thread: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            if(ListingDataService.s3Connection){
                updateFragmentAfterDataLoaded();
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
                new AlertDialog.Builder(MainActivity.getMainActivity())
                        .setTitle("An error has occurred")
                        .setMessage("Error! No Internet Connection!!")
                        .show();
            }

        }
    }
}
