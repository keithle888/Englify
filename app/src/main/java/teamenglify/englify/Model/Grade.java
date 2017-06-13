package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;



public class Grade implements Serializable{
    public String name;
    public ArrayList<Lesson> lessons;
    public String imgURL;
    public Date lastModified;

    public Grade(String name, ArrayList<Lesson> lessons, String imgURL) {
        this.name = name;
        this.lessons = lessons;
        this.imgURL = imgURL;
    }

    public Grade(String name, ArrayList<Lesson> lessons, String imgURL, Date lastModified) {
        this.name = name;
        this.lessons = lessons;
        this.imgURL = imgURL;
        this.lastModified = lastModified;
    }

    public Grade(String name) {
        this.name = name;
        this.lessons = new ArrayList<Lesson>();
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
        return "Grade[name:\"" + name + "\", lessons:\"" + lessons.toString() + "\",imgURL:\"" + imgURL + "\",isDownloaded:\"" + "]";
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

    public void overrideLesson(Lesson new_lesson) {
        if (new_lesson != null && lessons.size() != 0) {
            for (int i = 0; i < lessons.size(); i++) {
                Lesson lesson = lessons.get(i);
                if (lesson.name.equalsIgnoreCase(new_lesson.name)) {
                    lessons.set(i, new_lesson);
                    break;
                }
            }
        }
    }
}
