package teamenglify.englify.DataService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 05-Mar-17.
 */

public class DownloadService extends AsyncTask<Void, Void, Boolean>{
    public final static int DOWNLOAD_LISTING = 0;
    public final static int DOWNLOAD_GRADE = 1;
    private int downloadType;
    private Grade grade;
    private ProgressDialog pd;

    public DownloadService(int i) {
        this.downloadType = i;
    }

    public DownloadService (int i, Grade grade) {
        this.downloadType = i;
        this.grade = grade;
    }

    @Override
    public void onPreExecute() {
        Log.d("Englify", "Class DownloadService: Method onPreExecute(): Download Service starting, opening ProgressDialog.");
        pd = new ProgressDialog(mainActivity);
        pd.show();
        pd.setTitle("Download");
        pd.setCancelable(false);
    }

    @Override
    public Boolean doInBackground(Void...voids) {
        //change download depending on downloadType
        if (downloadType == DOWNLOAD_LISTING) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading listing.");
            pd.setMax(1);
            pd.setProgress(0);
            pd.setMessage("Downloading listings.");
            downloadListing();
            return Boolean.TRUE;
        } else if (downloadType == DOWNLOAD_GRADE && grade != null) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading " + grade.name + " .");
            pd.setMessage("Downloading " + grade.name + " structure.");
            pd.setMax(3);
            pd.setProgress(0);
            //downloadGradeStructure();
            pd.setProgress(1);
            pd.setMessage("Downloading " + grade.name + " audio files.");
            //downloadGradeAudio();
            pd.setProgress(2);
            pd.setMessage("Downloading " + grade.name + " images");
            //downloadGradeImages();
            pd.setProgress(3);
            pd.setMessage(grade.name + " download complete.");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Object onPostExecute(Boolean result) {
        pd.dismiss();
        Log.d("Englify", "Class DownloadService: Method doInBackground(): Download finish.");
    }

    public void downloadListing() {
        //download all objects
        ObjectListing objects = MainActivity.s3Client.listObjects(MainActivity.bucketName, MainActivity.rootDirectory);
        List<S3ObjectSummary> summaries = objects.getObjectSummaries();
        Log.d("Englify", "Class DownloadService: Method downloadListing(): Downloaded object listing from AWS S3.");
        //identify grades
        HashMap<String, String> identifiedGrades = new HashMap<>();
        for (S3ObjectSummary summary : summaries) {
            String key = summary.getKey();
            String[] keyParts = key.split("/");
            if (keyParts.length == 2) { //the listing for grades will always only have 2 parts ("res/grade01/")
                //put into identifiedGrades if the grade does exist
                if (!identifiedGrades.containsKey(keyParts[1])) {
                    identifiedGrades.put(keyParts[1], "");
                }
            }
        }
        //Generate grades based on identified grades
        Set<String> listOfGrades = identifiedGrades.keySet();
        Log.d("Englify", "Class DownloadService: Method downloadListing(): Identified grades: " + listOfGrades.toString());
        ArrayList<Grade> grades = new ArrayList<Grade>();
        for (String grade : listOfGrades) {
            grades.add(new Grade(grade, null, null));
        }
    }
}
