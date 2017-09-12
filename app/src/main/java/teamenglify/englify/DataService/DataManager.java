package teamenglify.englify.DataService;

/**
 * The DataManager class is used to request for data. Its methods will check whether the requested resource is available locally, if not, it will start a DownloadService to download, save the resource locally. To retrieve the resource, the variable in MainActivity downloadedObject will be set as the downloaded resource, once the download is finished.
 * @author Keith Leow
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;


//Used to decide whether to download grades from the internet or from internal storage
public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();
    private static final int LOCAL_STORAGE_VERSION = 1;
    private static final String LOCAL_STORAGE_VERSION_FILE_NAME = "LOCAL_STORAGE_VERSION";
    private Context context;

    public DataManager(Context context) {
        this.context = context;
    }
    /**
     * getListing() is to get a listing of all the Grades (a RootListing)
     * Once it has been loaded, the MainActivity variable downloadedObject will be set as the RootListing object.
     */
    public void getListing() {
        Log.d("Englify", "Class DataManager: Method getListing(): Checking memory for listing availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing)) && ((RootListing)LocalSave.loadObject(R.string.S3_Object_Listing)).grades != null) {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing was found in internal memory.");
            //Call Update UI method in "GRADE_LISTING" ListingFragment to update grade
            ((ListingFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("GRADE_LISTING")).mUpdateUIAfterDataLoaded();
        } else {
            if (mainActivity.hasInternetConnection) {
                Log.d("Englify", "Class DataManager: Method getListing(): Listing not available in internal memory. Moving to download listing from AWS S3");
                new DownloadService(DownloadService.DOWNLOAD_LISTING_OF_GRADES, context).execute();
            } else {
                Toast.makeText(mainActivity, "No internet connection detected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void download_list_of_lessons(Grade grade) {
        if (mainActivity.hasInternetConnection) {
            new DownloadService(DownloadService.DOWNLOAD_LISTING_OF_LESSONS, grade, context).execute();
        } else {
            Toast.makeText(mainActivity, "No internet connection detected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void download_lesson(Grade grade, Lesson lesson) {
        if (mainActivity.hasInternetConnection) {
            ask_to_download_lesson(grade, lesson);
        } else {
            Toast.makeText(mainActivity, "No internet connection detected.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used to delete the grade from local memory and reset the Grade variable isDownloaded to false.
     * @param grade
     */
    public void deleteGrade(Grade grade) {
        DeleteService.INSTANCE.deleteGrade(context, grade.name);
    }

    public void ask_to_download_lesson(final Grade grade,final Lesson lesson) {
        new AlertDialog.Builder(mainActivity)
                .setTitle(R.string.Download_Prompt_Title)
                .setMessage(mainActivity.getString(R.string.Download_Prompt_Message) + " " + lesson.name + "?")
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DownloadService(DownloadService.DOWNLOAD_LESSON, grade, lesson, context).execute();
                    }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Nothing happens
                    }
                })
                .show();
    }

    /**
     * The method checks what grades have been downloaded and calls UpdateService to check whether updates are available for the grades that have been downloaded. Grades that need updating will be deleted and re-downloaded.
     */
    public void checkForUpdates() {
        //create variables
        ArrayList<Grade> gradesToBeChecked = new ArrayList<>();
        //check whether anything has been downloaded. If not, throw a toast.
        RootListing rootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
        if (rootListing != null && rootListing.grades.size() != 0) {
            //Check if each grade has lessons.
            for (Grade grade : rootListing.grades) {
                if (grade.lessons != null && grade.lessons.size() != 0) {
                    for (Lesson lesson : grade.lessons) {
                        if (lesson.modules != null && lesson.modules.size() != 0) {
                            gradesToBeChecked.add(grade);
                            break;
                        }
                    }
                }
            }
            //If there are no lessons downloaded
            if (gradesToBeChecked.size() == 0) {
                showToastNoLessonsDownloaded();
            } else { // There are lessons that can be checked for updates
                new UpdateService(gradesToBeChecked, context).execute();
            }
        } else {
            showToastNoLessonsDownloaded();
        }
    }

    public void showToastNoLessonsDownloaded() {
        Toast.makeText(mainActivity, R.string.Update_Reject, Toast.LENGTH_LONG).show();
    }

    public boolean checkLocalStorageVersionIsLatest() {
        Log.d(TAG, "Starting check on local storage version.");
        if (LocalSave.doesFileExist(LOCAL_STORAGE_VERSION_FILE_NAME)) {
            Log.d(TAG, "Local storage version present. Checking if version is different.");
            try {
                int localVersionNumber = Integer.parseInt(LocalSave.loadString(LOCAL_STORAGE_VERSION_FILE_NAME));
                if (localVersionNumber == LOCAL_STORAGE_VERSION) {
                    Log.d(TAG,"Local storage version is the latest. Version number: " + localVersionNumber);
                    return true;
                } else {
                    Log.d(TAG, "Local version storage is out of date. Local version number: " + localVersionNumber + ", Latest version number: " + LOCAL_STORAGE_VERSION);
                    return false;
                }
            } catch (Exception e) {
                Log.d(TAG, "Locally stored version number of incorrectly saved.");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Local storage version has not been initialized. Saving latest storage version.");
            LocalSave.saveString(LOCAL_STORAGE_VERSION_FILE_NAME, String.valueOf(LOCAL_STORAGE_VERSION));
            return false;
        }
        return false;
    }
}
