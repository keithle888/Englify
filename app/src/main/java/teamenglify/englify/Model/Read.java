package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Read implements Serializable{
    public String name;
    public ArrayList<ReadPart> readParts;

    public Read (String name, ArrayList<ReadPart> readParts) {
        this.name = name;
        this.readParts = readParts;
    }
}
