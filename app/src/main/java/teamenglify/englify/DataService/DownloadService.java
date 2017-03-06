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

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 05-Mar-17.
 */

public class DownloadService extends AsyncTask<String, Void, Boolean>{
    private ProgressDialog pd;
    @Override
    public void onPreExecute() {
        pd = new ProgressDialog(mainActivity);
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    public Boolean doInBackground(String...params){
        String downloadType = params[0];
        if (downloadType != null) {
            pd.setTitle("Downloading " + params[0]);
            if (downloadType.equalsIgnoreCase("Grade")) {
                pd.setMax(1);
                downloadListings();
            } else {
                //for downloading grades
                pd.setMax(3);
                pd.setMessage("Downloading grade listings.");
                downloadGradeListings();
                pd.setProgress(1);
                pd.setMessage("Downloading grade images");
                downloadGradeImages();
                pd.setProgress(2);
                pd.setMessage("Downloading grade audio files");
                downloadGradeAudioFiles();
                pd.setProgress(3);
                pd.setMessage("Download Complete");
            }
        } else {
            Log.d("Englify", "Class DownloadService: Method Run(): String downloadType is null.");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void onPostExecute(Boolean result) {
        pd.dismiss();
        if (result) {
            Toast toast = Toast.makeText(mainActivity, "Download successful.", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(mainActivity, "Download failed.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void downloadListings() {
        ObjectListing objects = MainActivity.s3Client.listObjects(MainActivity.bucketName, MainActivity.rootDirectory);
        List<S3ObjectSummary> summaries = objects.getObjectSummaries();
        HashMap<String, ArrayList<String>> sortedListings = sortListings(summaries);
        LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), sortedListings);
    }

    public HashMap<String, ArrayList<String>> sortListings(List<S3ObjectSummary> summaries) {
        HashMap<String, ArrayList<String>> sortedListings = new HashMap<String, ArrayList<String>>();
        //Covert List<S3ObjectSummary> to an ArrayList<List<String>> where List<String> is the directory URL delimited by "/" (FOR SORTING PURPOSES)
        ArrayList<String[]> delimitedList = new ArrayList<>();
        for (S3ObjectSummary summary : summaries) {
            String url = summary.getKey();
            String[] delimitedURL = url.split("/");
            delimitedList.add(delimitedURL);
        }
        for (String[] delimitedURL : delimitedList) {
            String parentDirectory = "";
            String resource = delimitedURL[delimitedURL.length-1];
            if (delimitedURL.length == 2) { //Sort for Grade Listing
                for (int i = 0 ; i < delimitedURL.length - 1 ; i++) { //the parent directory of the folder we are identifying is always length-1
                    parentDirectory += delimitedURL[i] + "/";
                }
                if (!isFolder(delimitedURL[delimitedURL.length-1])) { //if the object is not a folder, but a resource (img / mp3 / txt file)
                    parentDirectory += mainActivity.getString(R.string.Append_To_Storage_For_Images);
                }
                //check if the sortedList already has the ArrayList to store the folder name
                if (sortedListings.containsKey(parentDirectory)) {
                    ArrayList<String> folderStorage = (ArrayList<String>) sortedListings.get(parentDirectory);
                    folderStorage.add(resource);
                } else { //sorted list does not have the ArrayList yet
                    ArrayList<String> folderStorage = new ArrayList<String>();
                    folderStorage.add(resource);
                    sortedListings.put(parentDirectory, folderStorage);
                }
            }
            Log.d("Englify", "Class DownloadService: Method sortListings: Saved " + resource + " to key -> " + parentDirectory);
        }
        return sortedListings;
    }

    public void

    public boolean isFolder(String entry) {
        if (entry.contains(".txt") || entry.contains(".mp3") || entry.contains(".png") || entry.contains(".jpg")) {
            return false;
        } else {
            return true;
        }
    }
}
