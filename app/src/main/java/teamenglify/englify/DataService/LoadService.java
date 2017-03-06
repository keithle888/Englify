package teamenglify.englify.DataService;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import teamenglify.englify.LocalSave;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

/**
 * Created by Keith on 05-Mar-17.
 */

public class LoadService {
    public static ArrayList<String> loadListings(String parentDirectory) {
        Object obj = LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        if (obj != null) {
            HashMap<String, ArrayList<String>> sortedListings = (HashMap<String, ArrayList<String>>) obj;
            return sortedListings.get(parentDirectory);
        } else {
            Log.d("Englify", "Class LoadService: Method loadListings: obj is null");
            return null;
        }
    }
}
