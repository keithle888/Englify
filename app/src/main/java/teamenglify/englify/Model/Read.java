package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import timber.log.Timber;

import static teamenglify.englify.MainActivity.bucketName;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Read implements Serializable{
    public String name;
    public ArrayList<ReadPart> readParts;
    public Date lastModified;

    public Read (String name, ArrayList<ReadPart> readParts) {
        this.name = name;
        this.readParts = readParts;
    }

    public Read (String name, ArrayList<ReadPart> readParts, Date lastModified) {
        this.name = name;
        this.readParts = readParts;
        this.lastModified = lastModified;
    }

    public Read(String name) {
        this.name = name;
        readParts = new ArrayList<>();
    }

    public boolean doesReadPartExist(String reading) {
        for (ReadPart readPart : readParts) {
            if (readPart.reading.equalsIgnoreCase(reading)) {
                return true;
            }
        }
        return false;
    }

    public void addReadPartAudio(String readPartName, String audioAbsolutePath) {
        if (doesReadPartExist(readPartName)) {
            ReadPart readPart = findReadPart(readPartName);
            readPart.audioURL = audioAbsolutePath;
        } else {
            readParts.add(new ReadPart(readPartName, null, audioAbsolutePath));
        }
    }

    public void addReadPartImg(String readPartName, String imgAbsolutePath) {
        if (doesReadPartExist(readPartName)) {
            ReadPart readPart = findReadPart(readPartName);
            readPart.imgURL = imgAbsolutePath;
        } else {
            readParts.add(new ReadPart(readPartName, imgAbsolutePath, null));
        }
    }

    public ReadPart findReadPart(String readPartName) {
        ReadPart toReturn = null;
        for (ReadPart readPart : readParts) {
            if (readPart.reading.equalsIgnoreCase(readPartName)) {
                toReturn = readPart;
            }
        }
        return toReturn;
    }

    public void overwriteTexts(LinkedList<String> texts) {
        if (texts != null && readParts.size() != 0) {
            Timber.d("Class Read, Method overwriteTexts(): Number of readParts => " + readParts.size() + ", Number of texts => " + texts.size());
            Timber.d("Class Read, Method overwriteTexts(): Overwriting read texts with contents -> " + texts.toString());
            //There may be more pictures than texts (DB Error). Run only up to which ever list size is the smallest.
            if (readParts.size() <= texts.size()) {
                for (ReadPart readPart : readParts) {
                    String text = texts.pop();
                    Timber.d(bucketName, "Class Read, Method overwriteTexts(): Overwriting -> " + readPart.reading + " with -> " + text);
                    readPart.reading = text;
                }
            } else {
                for (int i = 0; i < texts.size() ; i++) {
                    String text = texts.pop();
                    Timber.d(bucketName, "Class Read, Method overwriteTexts(): Overwriting -> " + readParts.get(i).reading + " with -> " + text);
                    readParts.get(i).reading = text;
                }
            }
        }
    }
}
