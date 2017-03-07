package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Keith on 07-Mar-17.
 */

public class RootListing implements Serializable{
    public ArrayList<Grade> grades;

    public RootListing (ArrayList<Grade> grades) {
        this.grades = grades;
    }
}
