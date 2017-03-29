package teamenglify.englify.Model;

import java.io.Serializable;

/**
 * Created by keith on 29-Mar-17.
 */

public class ExerciseChapterPart implements Serializable{
    public String text;
    public String imageURL;
    public String audioURL;

    public ExerciseChapterPart(String text) {
        this.text = text;
    }
}
