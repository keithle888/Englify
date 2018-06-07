package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import timber.log.Timber;

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
            Timber.d("Saved audio path: %s to chapterPart: %s", audioAbsolutePath, exerciseChapterPartName);
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, audioAbsolutePath, null));
            Timber.d("Created new exerciseChapterPart = name: %s", exerciseChapterPartName);
        }
    }

    public void addExerciseChapterPartImg(String exerciseChapterPartName, String imgAbsolutePath) {
        if (doesExerciseChapterPartExist(exerciseChapterPartName)) {
            ExerciseChapterPart exerciseChapterPart = findExerciseChapterPart(exerciseChapterPartName);
            exerciseChapterPart.imageURL = imgAbsolutePath;
            Timber.d("Saved img path: %s to chapterPart: %s", imgAbsolutePath, exerciseChapterPartName);
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, null, imgAbsolutePath));
            Timber.d("Created new exerciseChapterPart = name: %s", exerciseChapterPartName);
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
