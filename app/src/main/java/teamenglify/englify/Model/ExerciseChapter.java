package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;

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

}
