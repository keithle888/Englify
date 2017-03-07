package teamenglify.englify.Model;

import java.util.ArrayList;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Conversation extends Module {
    public ArrayList<Read> reads;

    public Conversation(String name, String imgURL, ArrayList<Read> reads) {
        super(name, imgURL);
        this.reads = reads;
    }

    public Conversation(String name) {
        super(name);
    }

    public void addRead(Read read) {
        if (reads == null) {
            reads = new ArrayList<Read>();
        }
        reads.add(read);
    }

    public Read findRead(String readName) {
        Read toReturn = null;
        for (Read read : reads) {
            if (read.name.equalsIgnoreCase(readName)) {
                toReturn = read;
            }
        }
        return toReturn;
    }
}
