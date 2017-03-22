package teamenglify.englify.DataService;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 06-Mar-17.
 */
//Used to decide whether to download grades from the internet or from internal storage
public class DataManager {

    public void getListing() {
        Log.d("Englify", "Class DataManager: Method getListing(): Checking memory for listing availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing)) && ((RootListing)LocalSave.loadObject(R.string.S3_Object_Listing)).grades != null) {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing was found in internal memory.");
            MainActivity.downloadedObject = LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        } else {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing not available in internal memory. Moving to download listing from AWS S3");
            DownloadService download = new DownloadService(teamenglify.englify.DataService.DownloadService.DOWNLOAD_LISTING);
            download.execute();
        }
    }

    public void getGrade(Grade grade) {
        //get grade from local memory
        Log.d("Englify", "Class DataManager: Method getListing(): Checking memory for " + grade.name + " availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing))) {
            RootListing rootListing = (RootListing) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
            grade = rootListing.findGrade(grade.name);
        }
        if (grade != null && grade.isDownloaded) {
            Log.d("Englify", "Class DataManager: Method getListing(): " + grade.name + " was found in internal memory.");
            mainActivity.downloadedObject = grade;
        } else {
            //grade has not been downloaded.
            Log.d("Englify", "Class DataManager: Method getListing(): " + grade.name + " downloaded to internal memory. Moving to download listing from AWS S3");
            promptForDownload(grade);
        }
    }

    public void deleteGrade(Grade grade) {
        promptForDeletion(grade);
    }

    public void promptForDownload(final Grade grade) {
        //create a dialog to ask whether they want to download the grade
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(mainActivity.getString(R.string.Download_Prompt_Title))
                .setMessage(mainActivity.getString(R.string.Download_Prompt_Message) + " " + grade.name + " ?")
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mainActivity.onBackPressed();
                        Toast.makeText(mainActivity,R.string.Reject_Download_Message,Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new DownloadService(DownloadService.DOWNLOAD_GRADE, grade).execute();
                    }
                })
                .show();
    }

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

    public void checkForUpdates() {
        ArrayList<Grade> gradesToBeChecked = new ArrayList<>();
        //check whether anything has been downloaded. If not, throw a toast.
        if (LocalSave.loadObject(R.string.S3_Object_Listing) == null ) {
            Toast.makeText(mainActivity, R.string.Update_Reject, Toast.LENGTH_LONG).show();
        } else {
            RootListing rootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
            if (rootListing.grades != null && rootListing.grades.size() != 0) {
                for (Grade grade : rootListing.grades) {
                    if (grade.isDownloaded) {
                        gradesToBeChecked.add(grade);
                    }
                }
                if (gradesToBeChecked.size() == 0) { //no grades have been downloaded, only RootListing was downloaded.
                    Toast.makeText(mainActivity, R.string.Update_Reject, Toast.LENGTH_LONG).show();
                } else {
                    new UpdateService(gradesToBeChecked).execute();
                }
            } else {
                Toast.makeText(mainActivity, R.string.Update_Reject, Toast.LENGTH_LONG).show();
            }
        }
    }
}
