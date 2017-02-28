package teamenglify.englify.DataService;

import android.content.Context;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class ReadTextDataService extends Thread {
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private static ArrayList<String> vocabList=new ArrayList<String>();

    public ReadTextDataService(String path) {
        vocabList.clear();
        try {
            URL url = new URL(path);
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                // str is one line of text; readLine() strips the newline character(s)
                String[] lineArray = TextUtils.split(str, "\n");
                if (lineArray.length > 0) {
                    for (String s : lineArray) {
                        vocabList.add(s);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ArrayList<String> getVocabList(){
        return vocabList;
    }
}
