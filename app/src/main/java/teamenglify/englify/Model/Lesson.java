package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Lesson implements Serializable{
    public ArrayList<Module> modules;
    public String name;
    public String imgURL;
}
