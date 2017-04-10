package teamenglify.englify.DataService;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import teamenglify.englify.Model.Grade;
import teamenglify.englify.R;

import static teamenglify.englify.DataService.DownloadService.DOWNLOAD_GRADE;
import static teamenglify.englify.DataService.DownloadService.getSummaries;
import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.rootDirectory;

/**
 * Created by keith on 20-Mar-17.
 */

public class UpdateService extends AsyncTask<Void, String, Boolean> {
    private String baseMessage = "Checking:";
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
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (checkForGradeUpdates() == true) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onProgressUpdate(String...progress) {
        pd.setMessage(progress[0]);
        pd.setProgress(Integer.parseInt(progress[1]));
    }

    @Override
    public void onPostExecute(Boolean result) {
        pd.dismiss();
        if (result.booleanValue()) {
            promptForUpdate();
            Toast.makeText(mainActivity, R.string.Update_Complete, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mainActivity, R.string.Update_Not_Available , Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkForGradeUpdates() {
        gradesToBeUpdated = new ArrayList<>();
        List<S3ObjectSummary> summaries = getSummaries(rootDirectory);
        pd.setMax(summaries.size() * summaries.size());
        for (Grade grade : gradesToBeChecked) {                                                     //Iterate through all grades.
            Log.d(bucketName, "Class UpdateService: Method checkForGradeUpdates(): Last modified date of grade being checked -> " + grade.lastModified.toString());
            for (S3ObjectSummary summary : summaries) {
                publishProgress(baseMessage + grade.name, String.valueOf(pd.getProgress() + 1));
                String key = summary.getKey();
                Date lastModified = summary.getLastModified();
                String[] dKey = key.split("/");
                if (dKey.length >= 2 && key.contains(grade.name)) {                     //Summary is part of the grade being checked.
                    if (grade.lastModified.before(lastModified)) {                                  //If true, grade has have a modification inside.
                        gradesToBeUpdated.add(grade);
                        break;
                    }
                }
            }
        }
        if (gradesToBeUpdated.size() != 0) {
            return true;
        }
        return false;
    }

    public Grade containsGrade(ArrayList<Grade> grades, String gradeName) {
        for (Grade grade : grades) {
            if (grade.name.equalsIgnoreCase(gradeName)) {
                return grade;
            }
        }
        return null;
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
                        for (Grade grade : gradesToBeUpdated) {
                            new DeleteService(grade).execute();
                            new DownloadService(DOWNLOAD_GRADE, grade).execute();
                        }
                    }
                })
                .show();
    }
}
