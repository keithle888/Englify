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
    public ArrayList<String> answer;
    public ArrayList<ArrayList<String>> choices;  //Outer array is a list for each "blank" in the question. Inner array is a list of choices for "blank" in question at index n.
    public String imageURL;
    public String audioURL;

    public ExerciseChapterPart(String name) {
        this.name = name;
    }

    public ExerciseChapterPart(String name, String audioURL, String imgURL) {
        this.name = name;
        this.imageURL = imageURL;
        this.audioURL = audioURL;
    }
}