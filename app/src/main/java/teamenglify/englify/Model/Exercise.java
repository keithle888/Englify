package teamenglify.englify.Model;

import java.util.ArrayList;

/**
 * Created by Keith on 07-Mar-17.
 */

public class Exercise extends Module {
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
}
