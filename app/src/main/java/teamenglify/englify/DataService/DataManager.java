package teamenglify.englify.DataService;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 06-Mar-17.
 */

public class DataManager {

    public ArrayList<Grade> getListing() {
        Log.d("Englify", "Class DataManager: Method getListing(): Checking memory for listing availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing))) {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing was found in internal memory.");
            return (ArrayList<Grade>) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        } else {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing not available in internal memory. Moving to download listing from AWS S3");
            AsyncTask download = new DownloadService(teamenglify.englify.DataService.DownloadService.DOWNLOAD_LISTING);
            download.execute();
            try {
                Log.d("Englify", "Class DataManager: Method getListing(): Waiting for DownloadService to finish.");
                download.get();
            } catch (Exception e) {
                Log.d("Englify", "Class DataManager: Method getListing(): Exception caught: " + e.toString());
                return null;
            }
            //once download is done
            return (ArrayList<Grade>) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        }
    }

    public Grade getGrade(Grade grade) {
        if (LocalSave.doesFileExist(grade.name)) {
            return (Grade) LocalSave.loadObject(grade.name);
        } else {
            promptForDownload(grade);
            return null;
        }
    }

    public void promptForDownload(Grade grade) {
        //create a dialog to ask whether they want to download the grade
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(mainActivity.getString(R.string.Download_Prompt_Title))
                .setMessage(mainActivity.getString(R.string.Download_Prompt_Message) + " " + grade.name)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
    }
}
