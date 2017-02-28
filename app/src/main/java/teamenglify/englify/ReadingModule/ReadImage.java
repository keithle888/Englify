package teamenglify.englify.ReadingModule;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import teamenglify.englify.DataService.ListingDataService;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadImage extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ViewPager viewPager;
    private ProgressDialog progressDialog;
    private ImageFragmentStateAdapter imageFragmentStateAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ReadImage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReadImage.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadImage newInstance(String param1, String param2) {
        ReadImage fragment = new ReadImage();
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
        View v =  inflater.inflate(R.layout.fragment_read_image, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.readViewPager);
            try {
                Runnable runnable = new ListingDataService("readImageListing");
                Thread myThread = new Thread(runnable);
                myThread.start();
                myThread.join();
                ArrayList <String> listOfRead = MainActivity.getMainActivity().getReadImageURLListing();
                imageFragmentStateAdapter = new ImageFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), listOfRead);
                viewPager.setAdapter(imageFragmentStateAdapter);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        Log.d("ReadImage", "viewPager:onPageSelected: " + Integer.toString(position));
                        if (MainActivity.getMainActivity().getCurrentPage() != position) {
                            MainActivity.getMainActivity().setCurrentPage(position);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                //find the listOfChoices again, after the thread has loaded it onto the mainActivity.
            } catch (InterruptedException e) {
                Log.d("Error:", "with ListingDataService Thread: " + e);
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

    private class AsyncFetch extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Loading In Progress");
            progressDialog.setMax(10);
            progressDialog.setProgress(0);
            progressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            ListingDataService listingDataService = new ListingDataService("readImageListing");
            ArrayList <String> listOfRead = listingDataService.getListOfChoices();
            imageFragmentStateAdapter = new ImageFragmentStateAdapter(MainActivity.getMainActivity().getSupportFragmentManager(), listOfRead);
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            progressDialog.hide();
            viewPager.setAdapter(imageFragmentStateAdapter);
        }
    }
}
