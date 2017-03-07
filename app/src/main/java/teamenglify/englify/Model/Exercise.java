package teamenglify.englify.Model;

import java.util.ArrayList;

/**
 * Created by Keith on 07-Mar-17.
 */

public class Exercise extends Module {
    public ArrayList<String> exercises;

    public Exercise(String name, String imgURL, ArrayList<String> exercises) {
        super(name, imgURL);
        this.exercises = exercises;
    }

    public Exercise (String name) {
        super(name);
    }
}
