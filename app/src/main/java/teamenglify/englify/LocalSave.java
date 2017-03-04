package teamenglify.englify;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method saveObject: Caught Exception: " + e);
            return false;
        }
        return true;
    }

    public static String loadString(String fileName) {
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = MainActivity.mainActivity.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method loadString: Caught Exception:" + e);
            return null;
        }
        return sb.toString();
    }

    public static Object loadObject(String fileName) {
        try {
            FileInputStream fis = MainActivity.mainActivity.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object object = ois.readObject();
            ois.close();
            fis.close();
            return object;
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method loadObject: Caught Exception:" + e);
            return null;
        }
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
