package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Grade implements Serializable{
    public String name;
    public ArrayList<Lesson> lessons;
    public String imgURL;
    public boolean isDownloaded;

    public Grade(String name, ArrayList<Lesson> lessons, String imgURL, boolean isDownloaded) {
        this.name = name;
        this.lessons = lessons;
        this.imgURL = imgURL;
        this.isDownloaded = isDownloaded;
    }
}
