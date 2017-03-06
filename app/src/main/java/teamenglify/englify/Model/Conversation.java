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
}
