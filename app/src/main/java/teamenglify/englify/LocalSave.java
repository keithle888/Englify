package teamenglify.englify;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static teamenglify.englify.MainActivity.mainActivity;


/**
 * Created by keith on 03-Mar-17.
 */

public class LocalSave {
    public static boolean saveString(String fileName, String input) {
        try {
            FileOutputStream fos = mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
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
            FileOutputStream fos = mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(input);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method saveObject: Trying to save " + fileName + " but caught Exception: " + e);
            return false;
        }
        return true;
    }

    public static String loadString(String fileName) {
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = mainActivity.openFileInput(fileName);
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
            FileInputStream fis = mainActivity.openFileInput(fileName);
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

    public static Object loadObject(int stringID) {
        try {
            FileInputStream fis = mainActivity.openFileInput(mainActivity.getString(stringID));
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
        for (String file : mainActivity.fileList()) {
            if (file.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    public static String saveMedia(String fileName, S3Object s3Object) {
        try {
            FileOutputStream fos = mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(IOUtils.toByteArray(s3Object.getObjectContent()));
            fos.close();
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method saveMedia: Tried saving " + fileName + " but caught Exception: " + e);
            return null;
        }
        return mainActivity.getFilesDir().getAbsolutePath() + fileName;
    }

    public static FileInputStream loadAudio(String fileName) {
        FileInputStream fis = null;
        try {
            fis = mainActivity.openFileInput(fileName);
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method loadAudio(): Tried saving " + fileName + " but caught Exception: " + e);
        }
        return fis;
    }

    public static byte[] loadImage(String fileName) {
        byte[] array = null;
        try {
            FileInputStream fis = mainActivity.openFileInput(fileName);
            array = IOUtils.toByteArray(fis);
        } catch (Exception e) {
            Log.d("Englify", "Class LocalSave: Method loadAudio(): Tried saving " + fileName + " but caught Exception: " + e);
        }
        return array;
    }
}
