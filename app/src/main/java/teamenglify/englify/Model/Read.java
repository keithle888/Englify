package teamenglify.englify.Model;

import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Read {
    public ArrayList<String> listOfReads;
    public ArrayList<String> audioPaths;
    public ArrayList<String> imgPaths;

    public Read(ArrayList<String> listOfReads, ArrayList<String> audioPaths, ArrayList<String> imgPaths) {
        this.listOfReads = listOfReads;
        this.audioPaths = audioPaths;
        this.imgPaths = imgPaths;
    }
}
