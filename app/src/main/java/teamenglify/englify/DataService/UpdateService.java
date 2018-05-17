package teamenglify.englify.DataService;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import teamenglify.englify.LocalSave;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.DataService.DownloadService.getSummaries;
import static teamenglify.englify.DataService.DownloadService.readTextFile;
import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.rootDirectory;
import static teamenglify.englify.MainActivity.s3Client;

/**
 * Created by keith on 20-Mar-17.
 */

public class UpdateService extends AsyncTask<Void, String, Boolean> {
    private String baseMessage = mainActivity.getString(R.string.Update_Service_Base_Message);
    private ProgressDialog pd;
    private ArrayList<Grade> gradesToBeChecked;
    private ArrayList<Grade> gradesToBeUpdated;


    public UpdateService (ArrayList<Grade> grades) {
        gradesToBeChecked = grades;
    }

    @Override
    public void onPreExecute() {
        pd = new ProgressDialog(mainActivity);
        pd.setMessage(baseMessage);
        pd.setTitle(R.string.Update_Progress_Dialog_Title);
        pd.setCancelable(false);
        pd.show();
        pd.setMax(2); // 1 for checking for checking for new lessons, 2 for checking existing lessons
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return checkIfUpdateAvailable();
    }

    @Override
    public void onProgressUpdate(String...progress) {
        pd.setMessage(progress[0]);
        if (progress.length > 1) {
            pd.setProgress(Integer.parseInt(progress[1]));
        }
    }

    @Override
    public void onPostExecute(Boolean result) {
        pd.dismiss();
    }

    public boolean checkIfUpdateAvailable() {
        //Create variables needed
        RootListing rootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
        LinkedList<String> lesson_descriptions = null;
        if (rootListing.grades.size() != 0) {
            for (Grade grade : rootListing.grades) {
                publishProgress("Checking: " + grade.name + " for new lessons...");
                if (grade.lessons.size() != 0) {
                    //Check if there are new lessons.
                    List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name);
                    for (S3ObjectSummary summary : summaries) {
                        String[] delimited_path = summary.getKey().split("/");
                        if (delimited_path.length == 3) {       //looks only at lesson folders
                           if (DownloadService.isFolder(delimited_path[2]) && grade.findLesson(delimited_path[2]) == null) {
                               publishProgress("Creating new " + delimited_path[2]);
                               grade.lessons.add(new Lesson(delimited_path[2],summary.getLastModified()));
                           } else if (DownloadService.isTextFile(delimited_path[2])) {
                               lesson_descriptions = readTextFile(s3Client.getObject(bucketName, summary.getKey()));
                           }
                        }
                    }
                    //override the existing lesson descriptions
                    if (lesson_descriptions != null) {
                        grade.overwriteLessonDescriptions(lesson_descriptions);
                        LocalSave.saveObject(R.string.S3_Object_Listing, rootListing);
                    }
                    //Check whether existing lessons need updating
                    for (Lesson lesson : grade.lessons) {
                        summaries = getSummaries(rootDirectory, grade.name, lesson.name);
                        for (S3ObjectSummary summary : summaries) {
                            if (summary.getLastModified().after(lesson.lastModified)) {
                                new DownloadService(DownloadService.DOWNLOAD_LESSON, grade, lesson).execute();
                                break;
                            }
                        }
                    }
                } else {
                    return false;   // No lesson listings (means grade was not accessed before by user)
                }
            }
        }
        //No grade listings downloaded.
        return false;
    }

    public void promptForUpdate() {
        //create a dialog to ask whether they want to update
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage(R.string.Update_Prompt_Message)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mainActivity.onBackPressed();
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
