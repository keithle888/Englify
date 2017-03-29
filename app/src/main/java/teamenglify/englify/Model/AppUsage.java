package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

//the class is an object to be saved in internal memory to record the app usage of the user
public class AppUsage implements Serializable {
    int userID;
    HashMap<String,ArrayList<String>> analyticList;

    public AppUsage(int userID, HashMap<String,ArrayList<String>> analyticList){
        this.userID = userID;
        this.analyticList =  analyticList;
    }
}
