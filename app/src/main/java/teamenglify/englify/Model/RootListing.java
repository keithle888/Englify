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

    public void overrideGrade(Grade grade) {
        //locate grade
        for (int i = 0; i < grades.size(); i++) {
            Grade oldGrade = grades.get(i);
            if (oldGrade.name.equalsIgnoreCase(grade.name)) {
                grades.set(i, grade);
            }
        }
    }

    public Grade findGrade(String gradeName) {
        for (Grade grade : grades) {
            if (grade.name.equalsIgnoreCase(gradeName)) {
                return grade;
            }
        }
        return null;
    }
}
