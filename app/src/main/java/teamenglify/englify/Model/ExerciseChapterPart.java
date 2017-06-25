package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by keith on 29-Mar-17.
 */

public class ExerciseChapterPart implements Serializable{
    public String name;
    public String question;
    public String answer;
    public String[] choices;
    public String imageURL;
    public String audioURL;

    public ExerciseChapterPart(String name, LinkedList<String> details) {
        this.name = name;
        for (String detail : details) {
            String[] delimited_detail = detail.split(":");
            if (delimited_detail[0].equalsIgnoreCase("Question")) {
                this.question = delimited_detail[1];
            } else if (delimited_detail[0].equalsIgnoreCase("Answer")) {
                this.answer = delimited_detail[1];
            } else if (delimited_detail[0].equalsIgnoreCase("Choices")) {
                this.choices = delimited_detail[1].split(",");
            }
        }
    }

    public ExerciseChapterPart(String name, String audioURL, String imgURL) {
        this.name = name;
        this.imageURL = imageURL;
        this.audioURL = audioURL;
    }
}