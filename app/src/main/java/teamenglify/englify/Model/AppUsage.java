package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

//the class is an object to be saved in internal memory to record the app usage of the user
public class AppUsage implements Serializable {
    int userID;
    ArrayList <String> completedList;
    HashMap<String,ArrayList<String>> analyticListVocab;
    HashMap<String,ArrayList<String>> analyticListRead;
    boolean firstThreeLessonSubmitted;

    public AppUsage(int userID, HashMap<String, ArrayList<String>> analyticListVocab, HashMap<String,ArrayList<String>> analyticListRead, ArrayList<String>completedList){
        this.userID = userID;
        this.analyticListVocab =  analyticListVocab;
        this.analyticListRead = analyticListRead;
        this.completedList = completedList;
        firstThreeLessonSubmitted = false;
    }

    public boolean isFirstThreeLessonSubmitted() {
        return firstThreeLessonSubmitted;
    }

    public void setFirstThreeLessonSubmitted(boolean firstThreeLessonSubmitted) {
        this.firstThreeLessonSubmitted = firstThreeLessonSubmitted;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public ArrayList<String> getCompletedList() {
        return completedList;
    }

    public void setCompletedList(ArrayList<String> completedList) {
        this.completedList = completedList;
    }

    public HashMap<String, ArrayList<String>> getAnalyticListVocab() {
        return analyticListVocab;
    }

    public void setAnalyticListVocab(HashMap<String, ArrayList<String>> analyticListVocab) {
        this.analyticListVocab = analyticListVocab;
    }

    public HashMap<String, ArrayList<String>> getAnalyticListRead() {
        return analyticListRead;
    }

    public void setAnalyticListRead(HashMap<String, ArrayList<String>> analyticListRead) {
        this.analyticListRead = analyticListRead;
    }
}
