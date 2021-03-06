package teamenglify.englify.DataService;

/**
 * The DataManager class is used to request for data. Its methods will check whether the requested resource is available locally, if not, it will start a DownloadService to download, save the resource locally. To retrieve the resource, the variable in MainActivity downloadedObject will be set as the downloaded resource, once the download is finished.
 * @author Keith Leow
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;
import timber.log.Timber;

import static teamenglify.englify.MainActivity.lesson;
import static teamenglify.englify.MainActivity.mainActivity;


//Used to decide whether to download grades from the internet or from internal storage
public class DataManager {
    /**
     * getListing() is to get a listing of all the Grades (a RootListing)
     * Once it has been loaded, the MainActivity variable downloadedObject will be set as the RootListing object.
     */
    public void getListing() {
        Timber.d("Class DataManager: Method getListing(): Checking memory for listing availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing)) &&
                LocalSave.loadObject(R.string.S3_Object_Listing) != null &&
                ((RootListing)LocalSave.loadObject(R.string.S3_Object_Listing)).grades != null &&
                ((RootListing)LocalSave.loadObject(R.string.S3_Object_Listing)).grades.size() != 0) {
            Timber.d("Class DataManager: Method getListing(): Listing was found in internal memory.");
            //Call Update UI method in "GRADE_LISTING" ListingFragment to update grade
            ((ListingFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("GRADE_LISTING")).mUpdateUIAfterDataLoaded();
        } else {
            if (mainActivity.hasInternetConnection) {
                Timber.d( "Class DataManager: Method getListing(): Listing not available in internal memory. Moving to download listing from AWS S3");
                new DownloadService(DownloadService.DOWNLOAD_LISTING_OF_GRADES).execute();
            } else {
                Toast.makeText(mainActivity, "No internet connection detected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void download_list_of_lessons(Grade grade) {
        if (mainActivity.hasInternetConnection) {
            new DownloadService(DownloadService.DOWNLOAD_LISTING_OF_LESSONS, grade).execute();
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
        promptForDeletion(grade);
    }

    /**
     * The AlertDialog prompt to get confirmation that the user wants to delete the grade. Is called by getDelete() method.
     * @param grade
     */
    public void promptForDeletion(final Grade grade) {
        //create a dialog to ask whether they want to download the grade
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(mainActivity.getString(R.string.Deletion_Prompt_Title))
                .setMessage(mainActivity.getString(R.string.Deletion_Check) + " " + grade.name + " ?")
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mainActivity.onBackPressed();
                    }
                })
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteService(grade).execute();
                    }
                })
                .show();
    }

    public void ask_to_download_lesson(final Grade grade,final Lesson lesson) {
        new AlertDialog.Builder(mainActivity)
                .setTitle(R.string.Download_Prompt_Title)
                .setMessage(mainActivity.getString(R.string.Download_Prompt_Message) + " " + lesson.name + "?")
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DownloadService(DownloadService.DOWNLOAD_LESSON, grade, lesson).execute();
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
                new UpdateService(gradesToBeChecked).execute();
            }
        } else {
            showToastNoLessonsDownloaded();
        }
    }

    public void showToastNoLessonsDownloaded() {
        Toast.makeText(mainActivity, R.string.Update_Reject, Toast.LENGTH_LONG).show();
    }
}
