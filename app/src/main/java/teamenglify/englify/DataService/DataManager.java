package teamenglify.englify.DataService;

import android.os.AsyncTask;

/**
 * Created by Keith on 06-Mar-17.
 */

public class DataManager extends AsyncTask<String , Void, Boolean> {
    @Override
    public void onPreExecute() {

    }

    @Override
    public Boolean doInBackground(String...params) {
        return Boolean.TRUE;
    }

    @Override
    public void onPostExecute(Boolean result) {
        
    }
}
