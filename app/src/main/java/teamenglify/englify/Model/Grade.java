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

    public Grade(String name) {
        this.name = name;
    }

    public void addLesson(Lesson lesson) {
        if (lessons == null) {
            lessons = new ArrayList<Lesson>();
        }
        lessons.add(lesson);
    }

    public Lesson findLesson(String lessonName) {
        Lesson toReturn = null;
        for (Lesson lesson : lessons) {
            if (lesson.name.equalsIgnoreCase(lessonName)) {
                toReturn = lesson;
            }
        }
        return toReturn;
    }
}
