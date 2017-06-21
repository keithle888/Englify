package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Module implements Serializable{
    public String name;
    public String imgURL;
    public Date lastModified;

    public Module (String name, String imgURL) {
        this.name = name;
        this.imgURL = imgURL;
    }

    public Module (String name, String imgURL, Date lastModified) {
        this.name = name;
        this.imgURL = imgURL;
        this.lastModified = lastModified;
    }

    public Module (String name) {
        this.name = name;
    }

    public boolean updateLastModifiedDate(Date lastModified) {
        if (this.lastModified == null) {
            this.lastModified = lastModified;
            return true;
        } else if (this.lastModified.before(lastModified)) {
            this.lastModified = lastModified;
            return true;
        }
        return false;
    }
}
