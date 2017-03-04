package teamenglify.englify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import teamenglify.englify.DataService.S3Properties;
import teamenglify.englify.FeedbackModule.Feedback;
import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LoginFragment.LoginFragment;
import teamenglify.englify.ModuleSelection.ModuleSelection;
import teamenglify.englify.ReadingModule.ReadingModule;
import teamenglify.englify.VocabModule.VocabModule;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.*;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public static String grade;
    public static String lesson;
    public static String vocab;
    public static String read;
    public final static String bucketName = "englify";
    public final static String parentFolder = "res";
    public static int position;
    public String currentListingType;
    public String currentListingURL;
    public static AmazonS3Client s3Client;

    //variables from ListingDataService
    public ArrayList<String> gradeListing;
    public ArrayList<String> lessonListing;
    public ArrayList<String> readListing;
    public ArrayList<String> vocabListing;
    public ArrayList<String> readImageURLListing;
    public ArrayList<String> audioConversationURLListing;
    public ArrayList<String> audioVocabURLListing;
    public ArrayList<String> audioConversationTextsToMatch;
    //analytics variable
    public static MobileAnalyticsManager analytics;
    //media player
    public int currentPage;
    public boolean readyForAudioBarToLoad;
    //permissions variables
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    //variables for Navigation Drawer
    private String[] mMenuItems;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    //variable for SpeechRecognition
    public boolean readyForSpeechRecognitionToLoad = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //default code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize variables
        currentPage = 0;
        readyForAudioBarToLoad = false;
        //check permissions, else request for them
        checkAndRequestPermissions();
        mainActivity = this;
        //initialize mobile analytics
        initializeMobileAnalytics();
        //initialize s3Client variable on another thread.
        new startS3Client().execute();
        //initialize navigation drawer
        initializeNavigationDrawer();
        //initialize login Page (default starting fragment)
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_container);
        if (fragment == null) {
            fragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.activity_main_container, fragment).commit();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(analytics != null) {
            analytics.getSessionClient().pauseSession();
            analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(analytics != null) {
            analytics.getSessionClient().resumeSession();
        }
    }

    public static MainActivity getMainActivity(){
        return mainActivity;
    }

    public void loadNextListing() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.activity_main_container, ListingFragment.newInstance(currentListingType, currentListingURL)).addToBackStack(null).commit();
    }

    public void loadModuleSelection() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.activity_main_container, ModuleSelection.newInstance(currentListingType, currentListingURL)).addToBackStack(null).commit();
    }

    public void loadReadingModule() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.activity_main_container, ReadingModule.newInstance(currentListingType,currentListingURL)).addToBackStack(null).commit();
    }

    public void loadVocabModule() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.activity_main_container, VocabModule.newInstance(currentListingType,currentListingURL)).addToBackStack(null).commit();
    }

    public void setCurrentListingType(String s) {
        currentListingType = s;
        Log.d("currentListingType", s);
    }

    public void setCurrentListingURL(String s) {
        currentListingURL = s;
        Log.d("currentListingURL", s);
    }

    public String getCurrentListingType() {
        return currentListingType;
    }

    public String getCurrentListingURL() {
        return currentListingURL;
    }

    public void setGrade(String s) {
        grade = s;
        Log.d("new Grade: ", s);
    }


    private class startS3Client extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    MainActivity.getMainActivity().getApplicationContext(),
                    S3Properties.IDENTITYPOOLID, // Identity Pool ID
                    Regions.AP_NORTHEAST_1); // Region
            s3Client = new AmazonS3Client(credentialsProvider);
            return null;
        }
    }

    public void setGradeListing(ArrayList<String> array) {
        gradeListing = array;
        Log.d("New gradeListing: ", array.toString());
    }

    public void setLessonListing(ArrayList<String> array) {
        lessonListing = array;
        Log.d("New lessonListing: ", array.toString());
    }

    public void setReadListing(ArrayList<String> array) {
        readListing = array;
        Log.d("New unitListing: ", array.toString());
    }

    public void setVocabListing(ArrayList<String> array) {
        vocabListing = array;
        Log.d("New vocabListing: ", array.toString());
    }

    public void setReadImageListing(ArrayList<String> array) {
        readImageURLListing = array;
        Log.d("New readURL: ", array.toString());
    }

    public ArrayList<String> getGradeListing() {
        return gradeListing;
    }

    public ArrayList<String> getLessonListing() {
        return lessonListing;
    }

    public ArrayList<String> getReadListing() {
        return readListing;
    }

    public ArrayList<String> getVocabListing() {
        return vocabListing;
    }

    public ArrayList<String> getReadImageURLListing() {
        return readImageURLListing;
    }

    public void setAudioConversationURLListingURLListing(ArrayList<String> list) {
        this.audioConversationURLListing = list;
        if (list != null) {
            Log.d("Audio Convo URLs found", list.toString());
        }
    }

    public void setAudioVocabURLListingURLListing(ArrayList<String> list) {
        this.audioConversationURLListing = list;
        if (list != null) {
            Log.d("Audio Vocab URLs found", list.toString());
        }

    }

    public void setCurrentPage(int i) {
        currentPage = i;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Record Audio Permission Granted.");
                } else {
                    Log.d("MainActivity", "Record Audio Permission Denied.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void checkAndRequestPermissions() {
        //Record Audio Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(getMainActivity())
                                .setTitle("Requesting Permission")
                                .setMessage("The application requires permission to use the microphone for speech recognition.")
                                .show();
                    }
                });

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
    }

    private void initializeMobileAnalytics() {
        try {
            analytics = MobileAnalyticsManager.getOrCreateInstance(
                    this.getApplicationContext(),
                    S3Properties.ANALYTICSID, //Amazon Mobile Analytics App ID
                    S3Properties.IDENTITYPOOLID //Amazon Cognito Identity Pool ID
            );
        } catch(InitializationException ex) {
            Log.e(this.getClass().getName(), "Failed to initialize Amazon Mobile Analytics", ex);
        }
    }

    private void initializeNavigationDrawer() {
        //Code for Navigation Menu
        mMenuItems = getResources().getStringArray(R.array.menu_items);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mMenuItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            //Choose which fragment to load.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment newFragment = null;
                switch (position) {
                    case 0:
                        //Goes to Grade
                        mainActivity.setCurrentListingType("gradeListing");
                        newFragment = new ListingFragment();
                        break;
                    case 1:
                        //Goes to Login
                        newFragment = new LoginFragment();
                        break;
                    case 2:
                        newFragment = new TextToSpeech();
                        break;
                    case 3:
                        newFragment = new Feedback();
                        break;
                }
                if (newFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, newFragment).addToBackStack(null).commit();
                }
                mDrawerLayout.closeDrawers();

            }
        });
        //create button for Nav Drawer in Action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
}
