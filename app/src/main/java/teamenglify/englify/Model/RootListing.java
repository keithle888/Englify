package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;


public class RootListing implements Serializable{
    public ArrayList<Grade> grades;
    public Date lastModified;

    public RootListing() { grades = new ArrayList<>(); }

    public RootListing (ArrayList<Grade> grades) {
        this.grades = grades;
    }

    public RootListing (ArrayList<Grade> grades, Date lastModified) {
        this.grades = grades;
        this.lastModified = lastModified;
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