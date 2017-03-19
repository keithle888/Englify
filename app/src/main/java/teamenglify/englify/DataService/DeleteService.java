package teamenglify.englify.DataService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import teamenglify.englify.LocalSave;
import teamenglify.englify.LoginFragment.LoginFragment;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by keith on 18-Mar-17.
 */

public class DeleteService extends AsyncTask<Void, Void, Void>{
    private ProgressDialog pd;
    private Grade grade;

    public DeleteService(Grade grade) {
        this.grade = grade;
    }

    @Override
    public void onPreExecute() {
        Log.d("Englify", "Class DeleteService: Method onPreExecute(): Delete Service starting, opening ProgressDialog.");
        pd = new ProgressDialog(mainActivity);
        pd.setTitle("Download");
        pd.setCancelable(false);
        //set the count to the total number of items that need to be deleted
        int count = 0;
        for (String fileName : mainActivity.fileList()) {
            if (fileName.contains(grade.name)) {
                count++;
            }
        }
        pd.setMax(count);
        pd.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        deleteGrade();
        return null;
    }


    @Override
    public void onPostExecute(Void result) {
        pd.dismiss();
        Toast.makeText(mainActivity, "Grade deleted.", Toast.LENGTH_LONG).show();
        mainActivity.clearBackStack();
        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_container, new LoginFragment()).addToBackStack(null).commit();
        super.onPostExecute(result);
    }

    public void deleteGrade() {
        //delete media resources seperately stored on grade.
        for (String fileName : mainActivity.fileList()) {
            if (fileName.contains(grade.name)) {
                Log.d("Englify", "Class DeleteService: Method deleteGrade(): Deleting -> " + fileName);
                mainActivity.deleteFile(fileName);
                pd.setProgress(pd.getProgress() + 1);
            }
        }
        Grade newGrade = new Grade(grade.name, new ArrayList<Lesson>(), null, false);
        //null all grade's variables to delete references
        grade.lessons = null;
        //reset grade stored in Root Listing
        RootListing rootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
        rootListing.overrideGrade(newGrade);
        LocalSave.saveObject(R.string.S3_Object_Listing, rootListing);
    }
}
