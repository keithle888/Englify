package teamenglify.englify.Model;

import com.amazonaws.com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by keith on 29-Mar-17.
 */

public class ExerciseChapterPart implements Serializable{
    @Expose public String name;
    @Expose public String question;
    @Expose public List<String> answer;
    @Expose public List<List<String>> choices;
    @Expose public String translation;
    @Expose public String imageURL;
    @Expose public String audioURL;

    public ExerciseChapterPart(String name, String question, List<String> answer, List<List<String>> choices, String translation) {
        this.name = name;
        this.question = question;
        this.answer = answer;
        this.choices = choices;
        this.translation = translation;
    }

    public ExerciseChapterPart(String name, String audioURL, String imgURL) {
        this.name = name;
        this.imageURL = imageURL;
        this.audioURL = audioURL;
    }

    @Override
    public String toString() {
        return "ExerciseChapterPart[name:" +
                name + ", " +
                "question:" + question + ", " +
                "answer:" + answer + ", " +
                "choices:" + choices + ", " +
                "audioURL:" + audioURL + ", " +
                "imgURL:" + imageURL + "]";
    }
}