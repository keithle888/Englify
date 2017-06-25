package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by keith on 29-Mar-17.
 */

public class ExerciseChapter implements Serializable {
    public String name;
    public ArrayList<ExerciseChapterPart> chapterParts;

    public ExerciseChapter(String name) {
        this.name = name;
        this.chapterParts = new ArrayList<>();
    }

    public void addExerciseChapterPartAudio(String exerciseChapterPartName, String audioAbsolutePath) {
        if (doesExerciseChapterPartExist(exerciseChapterPartName)) {
            ExerciseChapterPart exerciseChapterPart = findExerciseChapterPart(exerciseChapterPartName);
            exerciseChapterPart.audioURL = audioAbsolutePath;
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, null, audioAbsolutePath));
        }
    }

    public void addExerciseChapterPartImg(String exerciseChapterPartName, String audioAbsolutePath) {
        if (doesExerciseChapterPartExist(exerciseChapterPartName)) {
            ExerciseChapterPart exerciseChapterPart = findExerciseChapterPart(exerciseChapterPartName);
            exerciseChapterPart.imageURL = audioAbsolutePath;
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, audioAbsolutePath, null));
        }
    }

    public void addExerciseChapterPartDetails(String exerciseChapterPartName, LinkedList<String> details) {
        if (!doesExerciseChapterPartExist(exerciseChapterPartName)) {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, details));
        }
    }


    public boolean doesExerciseChapterPartExist(String exerciseChapterPartName) {
        if (chapterParts != null && chapterParts.size() != 0) {
            for (ExerciseChapterPart exerciseChapterPart : chapterParts) {
                if (exerciseChapterPart.name.equalsIgnoreCase(exerciseChapterPartName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ExerciseChapterPart findExerciseChapterPart(String exerciseChapterPartName) {
        ExerciseChapterPart toReturn = null;
        for (ExerciseChapterPart exerciseChapterPart : chapterParts) {
            if (exerciseChapterPart.name.equalsIgnoreCase(exerciseChapterPartName)) {
                toReturn = exerciseChapterPart;
            }
        }
        return toReturn;
    }


}
