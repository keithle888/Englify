package teamenglify.englify.DataService;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.util.ArrayList;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 06-Mar-17.
 */
//Used to decide whether to download grades from the internet or from internal storage
public class DataManager {

    public void getListing() {
        Log.d("Englify", "Class DataManager: Method getListing(): Checking memory for listing availability.");
        if (LocalSave.doesFileExist(mainActivity.getString(R.string.S3_Object_Listing))) {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing was found in internal memory.");
            //the root listings (grades) has been downloaded before, therefore get all the grades (individually download from internal memory and pass it up
            ArrayList<Grade> grades = new ArrayList<>();
            for (Grade g : (ArrayList<Grade>) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing))) {
                Grade grade = (Grade) LocalSave.loadObject(g.name);
                grades.add(grade);
            }
            mainActivity.downloadedObject = grades;
        } else {
            Log.d("Englify", "Class DataManager: Method getListing(): Listing not available in internal memory. Moving to download listing from AWS S3");
            DownloadService download = new DownloadService(teamenglify.englify.DataService.DownloadService.DOWNLOAD_LISTING);
            download.execute();
            try {
                Log.d("Englify", "Class DataManager: Method getListing(): Waiting for DownloadService to finish.");
                download.get();
            } catch (Exception e) {
                Log.d("Englify", "Class DataManager: Method getListing(): Exception caught: " + e.toString());
            }
            //once download is done
            mainActivity.downloadedObject = LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        }
    }

    public void getGrade(String gradeSelected) {
        if (((Grade) LocalSave.loadObject(gradeSelected)).isDownloaded) {
            //grade has been downloaded.
            MainActivity.downloadedObject = LocalSave.loadObject(gradeSelected);
        } else {
            //grade has not been downloaded.
            promptForDownload(gradeSelected);
        }
    }

    public void promptForDownload(String gradeSelected) {
        //create a dialog to ask whether they want to download the grade
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(mainActivity.getString(R.string.Download_Prompt_Title))
                .setMessage(mainActivity.getString(R.string.Download_Prompt_Message) + " " + gradeSelected)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .show();
    }
}
