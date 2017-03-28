package teamenglify.englify;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.DataService.S3Properties;
import teamenglify.englify.FeedbackModule.Feedback;
import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LoginFragment.LoginFragment;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.ModuleSelection.ModuleSelection;
import teamenglify.englify.ReadingModule.ReadingModule;
import teamenglify.englify.Settings.DeleteGrade;
import teamenglify.englify.Tutorial.Tutorial;
import teamenglify.englify.VocabModule.VocabModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.*;
public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public static String strGrade;
    public static String lesson;
    public static String vocab;
    public static String read;
    public static String bucketName;
    public static String rootDirectory;
    public static String currentDirectory;
    public static int position;
    public static AmazonS3Client s3Client;
    public static TransferUtility transferUtility;
    public static RecyclerView.LayoutManager mLayoutManager;
    //variables for Background Thread that updates internet status
    private Handler mHandler;
    public boolean hasInternetConnection;
    public boolean isWiFiConnection;
    //variables from DataManager
    public static Object downloadedObject;
    //variables from ListingFragment
    //analytics variable
    public static MobileAnalyticsManager analytics;
    public static HashMap<String,ArrayList<String>> analyticList = new HashMap<>();
    //permissions variables
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    //variables for Navigation Drawer
    private String[] mMenuItems;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private File root;
    private ArrayList<File> fileList = new ArrayList<File>();

    //variable for SpeechRecognition
    public boolean readyForSpeechRecognitionToLoad = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //default code
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        //initialize background thread
        HandlerThread mHandlerThread = new HandlerThread(getLocalClassName());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mBackgroundThread);
        //initialize variables
        bucketName = getString(R.string.Bucket_Name);
        currentDirectory = rootDirectory = getString(R.string.Root_Directory);
        transferUtility = new TransferUtility(s3Client, getApplicationContext());
        //check permissions, else request for them
        checkAndRequestPermissions();
        mainActivity = this;
        //initialize mobile analytics
        initializeMobileAnalytics();
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,2 );
        //initialize s3Client variable on another thread.
        mHandler.post(startS3Client);
        //initialize navigation drawer
        initializeNavigationDrawer();
        //initialize login Page (default starting fragment)
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_container);
        if (fragment == null) {
            fragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.activity_main_container, fragment).commit();
        }

        AnalyticsEvent event = analytics.getEventClient().createEvent("LessonCompleted").withAttribute("lessonOne","lessonOne");
        analytics.getEventClient().recordEvent(event);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mBackgroundThread);
        mHandler.post(mBackgroundThread);
        //create RootListing if none exists (eg. 1st time app download)
        if (fileList().length == 0) {
            LocalSave.saveObject(getString(R.string.S3_Object_Listing), new RootListing(null));
        } else if (!LocalSave.doesFileExist(getString(R.string.S3_Object_Listing))) {
            LocalSave.saveObject(getString(R.string.S3_Object_Listing), new RootListing(null));
        }
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();

        for(File f : subFiles){
            Log.d("DOWNLOADED FILES -- ",f.getName());
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
        Log.d("MainActivity", "onPaused");
        if(analytics != null) {
            //Log.d("MainActivity", "event not recorded");
            //analytics.getEventClient().submitEvents();
            Iterator it = analyticList.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                ArrayList<String> dataList = (ArrayList<String>) pair.getValue();
                Log.d("main activity", ""+dataList.toString());
                if(dataList.size()>10){
                    AnalyticsEvent event = analytics.getEventClient().createEvent((String)pair.getKey()).withAttribute("Completed","Completed");
                    analytics.getEventClient().recordEvent(event);
                    Log.d("main activity", (String)pair.getKey());
                    Log.d("main activity", event.toString());
                    Log.d("main activity", "event recorded");
                } else {
                    Log.d("main activity", "event not recorded");

                }
                //it.remove(); // avoids a ConcurrentModificationException
            }
        }
        analytics.getSessionClient().pauseSession();
        analytics.getEventClient().submitEvents();
        mHandler.removeCallbacks(mBackgroundThread);
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

    public void loadNextListing(int listingType, Object objectToLoad) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.activity_main_container, ListingFragment.newInstance(listingType, objectToLoad)).addToBackStack(null).commit();
    }

    public void loadModuleListing(int i,Lesson lesson) {
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, ModuleSelection.newInstance(i, lesson)).addToBackStack(null).commit();
    }

    public void loadReadingModule(Read read) {
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, ReadingModule.newInstance(read)).addToBackStack(null).commit();
    }

    public void loadVocabModule(Vocab vocab) {
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, VocabModule.newInstance(vocab)).addToBackStack(null).commit();
    }

    private Runnable startS3Client = new Runnable() {
        public void run() {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    MainActivity.getMainActivity().getApplicationContext(),
                    S3Properties.IDENTITYPOOLID, // Identity Pool ID
                    Regions.AP_NORTHEAST_1); // Region
            s3Client = new AmazonS3Client(credentialsProvider);
        }
    };

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
                    S3Properties.IDENTITYPOOLID//Amazon Cognito Identity Pool ID
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
                        //Goes to Login
                        newFragment = new LoginFragment();
                        break;
                    case 1:
                        //Goes to Grade
                        newFragment = ListingFragment.newInstance(ListingFragment.GRADE_LISTING, LocalSave.loadObject(getString(R.string.S3_Object_Listing)));
                        break;
                    case 2:
                        newFragment = new TextToSpeech();
                        break;
                    case 3:
                        newFragment = new Feedback();
                        break;
                    case 4:
                        //redirects user back if nothing has been downloaded
                        RootListing root = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
                        if (root == null || root.grades == null) {
                            Toast.makeText(mainActivity,"No grades downloaded.မည္သည့္အဆင့္မွ် ထုတ္ယူ (download) ၿခင္းမရွိပ္။",Toast.LENGTH_LONG).show();
                        } else {
                            newFragment = new DeleteGrade();
                        }
                        break;
                    case 5:
                        newFragment = new Tutorial();
                        break;
                    case 6:
                        new DataManager().checkForUpdates();
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

    private Runnable mBackgroundThread = new Runnable() {
        public void run() {
            //update internet connectivity
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (hasInternetConnection != (activeNetwork != null && activeNetwork.isConnected()) ) {
                hasInternetConnection = activeNetwork != null && activeNetwork.isConnected();
                isWiFiConnection = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                Log.d("Englify", "Class MainActivity: Method mBackgroundThread: Internet Status -> " + hasInternetConnection);
            }
            //cause the background thread to run every 1000ms.
            mHandler.postDelayed(mBackgroundThread, 1000);
        }
    };

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

            }
        }
    }

    public void clearBackStack() {
        Log.d("Englify", "Class MainActivity: Method clearBackStack(): Clearing Back Stack.");
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
