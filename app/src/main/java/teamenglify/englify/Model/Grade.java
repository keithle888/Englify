package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Grade implements Serializable{
    public String name;
    public ArrayList<Lesson> lessons;
    public String imgURL;
    public boolean isDownloaded;
    public Date lastModified;

    public Grade(String name, ArrayList<Lesson> lessons, String imgURL, boolean isDownloaded) {
        this.name = name;
        this.lessons = lessons;
        this.imgURL = imgURL;
        this.isDownloaded = isDownloaded;
    }

    public Grade(String name, ArrayList<Lesson> lessons, String imgURL, boolean isDownloaded, Date lastModified) {
        this.name = name;
        this.lessons = lessons;
        this.imgURL = imgURL;
        this.isDownloaded = isDownloaded;
        this.lastModified = lastModified;
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

    @Override
    public String toString() {
        return "Grade[name:\"" + name + "\", lessons:\"" + lessons.toString() + "\",imgURL:\"" + imgURL + "\",isDownloaded:\"" + isDownloaded + "\"]";
    }

    public void overwriteLessonDescriptions(LinkedList<String> texts) {
        if (lessons != null && lessons.size() != 0) {
            for (String description : texts) {
                String[] keys = description.split(":");
                for (String k : keys) {
                    k.trim();
                }
                Lesson lesson = findLesson(keys[0]);
                if (lesson != null) {
                    lesson.description = keys[1];
                } else {
                    Log.d("Englify", "Class Grade: Method overwriteLessonDescriptions(): Description found without matching lesson -> " + description);
                }
            }
        }
    }
}
