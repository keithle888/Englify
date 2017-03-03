package teamenglify.englify;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * Created by keith on 03-Mar-17.
 */

public class LocalSave {
    public static boolean saveString(String fileName, String input) {
        try {
            FileOutputStream fos = MainActivity.mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(input.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method saveString: Caught Exception: " + e);
            return false;
        }
        return true;
    }

    public static boolean saveObject(String fileName, Object input) {
        try {
            FileOutputStream fos = MainActivity.mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(input);
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method saveObject: Caught Exception: " + e);
            return false;
        }
        return true;
    }

    public static String loadString(String fileName) {
        String output = null;
        try {
            FileInputStream fis = MainActivity.mainActivity.openFileInput(fileName);
            fis.read();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method loadString: Caught Exception:" + e);
            return null;
        }
        return output;
    }

    public static boolean doesFileExist(String fileName) {
        for (String file : MainActivity.mainActivity.fileList()) {
            if (file.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }
}
