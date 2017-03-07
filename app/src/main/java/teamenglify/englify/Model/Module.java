package teamenglify.englify.Model;

import java.io.Serializable;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Module implements Serializable{
    public String name;
    public String imgURL;

    public Module (String name, String imgURL) {
        this.name = name;
        this.imgURL = imgURL;
    }

    public Module (String name) {
        this.name = name;
    }
}
