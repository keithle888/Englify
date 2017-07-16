package teamenglify.englify.Model;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Keith on 07-Mar-17.
 */

public class Exercise extends Module {
    private static final String TAG = Exercise.class.getSimpleName();
    public ArrayList<ExerciseChapter> chapters;

    public Exercise(String name, String imgURL, ArrayList<ExerciseChapter> chapters) {
        super(name, imgURL);
        this.chapters = chapters;
    }

    public Exercise (String name) {
        super(name);
        this.chapters = new ArrayList<>();
    }

    public ExerciseChapter findExerciseChapter(String chapterName) {
        ExerciseChapter toReturn = null;
        for (ExerciseChapter chapter : chapters) {
            if (chapter.name.equalsIgnoreCase(chapterName)) {
                toReturn = chapter;
            }
        }
        return toReturn;
    }

    public void addChapter(ExerciseChapter exerciseChapter) {
        if (chapters == null) {
            chapters = new ArrayList<>();
        }
        chapters.add(exerciseChapter);
    }

    public void printAllChapters() {
        if (chapters != null && chapters.size() != 0) {
            Log.d(TAG, "Printing all Exercise Chapters.");
            for (ExerciseChapter exerciseChapter : chapters) {
                Log.d(TAG, "Printing chapter: " + exerciseChapter.name);
                exerciseChapter.printAllChapterParts();
            }
        } else {
            Log.d(TAG, "chapters are null or 0");
        }
    }
}
