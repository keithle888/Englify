package teamenglify.englify.DataService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import teamenglify.englify.Model.Grade;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.rootDirectory;
import static teamenglify.englify.MainActivity.s3Client;

/**
 * Created by keith on 20-Mar-17.
 */

public class UpdateService extends AsyncTask<Void, String, Void> {
    private String baseMessage = "Checking:";
    private ProgressDialog pd;
    private ArrayList<Grade> gradesToBeChecked;

    public UpdateService (ArrayList<Grade> grades) {
        gradesToBeChecked = grades;
    }

    @Override
    public void onPreExecute() {
        pd = new ProgressDialog(mainActivity);
        pd.setMessage(baseMessage);
        pd.setIndeterminate(true);

    }

    @Override
    protected Void doInBackground(Void... voids) {
        checkAndUpdateGrades();
        return null;
    }

    @Override
    protected void onProgressUpdate(String...progress) {
        pd.setMessage(baseMessage + "\n"  + progress[0]);
    }

    @Override
    public void onPostExecute(Void result) {
        pd.dismiss();
    }

    public void checkAndUpdateGrades() {
        GradeHashSet existingGradeNames = new GradeHashSet();
        for (Grade grade : gradesToBeChecked) {                                                                                             //get all the names of the grade into a hashset, so we can check whether keyNames exists.
            existingGradeNames.add(grade.name);
        }
        List<S3ObjectSummary> summaries = mainActivity.s3Client.listObjects(bucketName, rootDirectory).getObjectSummaries();                //get all the listings from S3
        for (S3ObjectSummary summary : summaries) {                                                                                         //loop through all the listings in S3
            String key = summary.getKey();
            Date date = summary.getLastModified();
            Log.d("Englify", "Class UpdateService: Method checkAndUpdateGrades(): " + key + " -> " + date.toString());
            if (existingGradeNames.hasMatch(key)) {                                                                                         //the key from S3 is matched against grades that are locally stored in the device.

            }
        }
    }

    private class GradeHashSet extends HashSet<String> {
        public boolean hasMatch(String key) {
            Iterator<String> iter = iterator();
            while (iter.hasNext()) {
                String matchingString = iter.next();
                if (key.contains(matchingString)) {
                    return true;
                }
            }
            return false;
        }
    }
}
