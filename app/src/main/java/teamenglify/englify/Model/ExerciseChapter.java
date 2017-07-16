package teamenglify.englify.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by keith on 29-Mar-17.
 */

public class ExerciseChapter implements Serializable {
    private static final String TAG = ExerciseChapter.class.getSimpleName();
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
            Log.d(TAG, "Added audio file: " + audioAbsolutePath + " to existing chapter part: " + exerciseChapterPart.name);
            Log.d(TAG, "ExerciseChapterPart contents after modification: " + exerciseChapterPart.toString());
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, audioAbsolutePath, null));
            Log.d(TAG, "Created new ExerciseChapterPart with audio file: " + audioAbsolutePath);
        }
    }

    public void addExerciseChapterPartImg(String exerciseChapterPartName, String imgAbsolutePath) {
        if (doesExerciseChapterPartExist(exerciseChapterPartName)) {
            ExerciseChapterPart exerciseChapterPart = findExerciseChapterPart(exerciseChapterPartName);
            exerciseChapterPart.imageURL = imgAbsolutePath;
            Log.d(TAG, "Added img file: " + imgAbsolutePath + " to existing chapter part: " + exerciseChapterPart.name);
            Log.d(TAG, "ExerciseChapterPart contents after modification: " + exerciseChapterPart.toString());
        } else {
            chapterParts.add(new ExerciseChapterPart(exerciseChapterPartName, null, imgAbsolutePath));
            Log.d(TAG, "Created new ExerciseChapterPart with img file: " + imgAbsolutePath);
        }
    }

    public void addExerciseChapterPartDetails(LinkedList<String> details) {
        if (details != null && details.size() != 0) {
            for (String detail : details) {
                String[] delimited_detail = detail.split(":");
                if (delimited_detail.length == 4) {
                    String name = delimited_detail[0];
                    String question = delimited_detail[1];
                    String[] choices = delimited_detail[2].split(",");
                    String answer = delimited_detail[3];

                    if (doesExerciseChapterPartExist(name)) {
                        ExerciseChapterPart exerciseChapterPart = findExerciseChapterPart(name);
                        exerciseChapterPart.question = question;
                        exerciseChapterPart.choices = choices;
                        exerciseChapterPart.answer = answer;
                        Log.d(TAG, "Found existing exercise chapter part: " + exerciseChapterPart.name + ", and added details: " + detail);
                    } else {
                        chapterParts.add(new ExerciseChapterPart(name,
                                question,
                                answer,
                                choices));
                        Log.d(TAG, "Created new ExerciseChapterPart: " + detail);
                    }
                } else {
                    Log.d(TAG, "ExerciseChapterPart detail format has an error: " + detail);
                }
            }
        } else {
            Log.d(TAG, "Error trying to add ExerciseChapterPart details.");
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

    public void printAllChapterParts() {
        if (chapterParts != null && chapterParts.size() != 0) {
            for (ExerciseChapterPart exerciseChapterPart : chapterParts) {
                Log.d(TAG, "Printing ExerciseChapterPart: " + exerciseChapterPart.name);
                Log.d(TAG, exerciseChapterPart.toString());
            }
        } else {
            Log.d(TAG, "chapterParts is null or size() == 0");
        }
    }
}
