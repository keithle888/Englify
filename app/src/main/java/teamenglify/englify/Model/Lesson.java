package teamenglify.englify.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Keith on 06-Mar-17.
 */

public class Lesson implements Serializable{
    public ArrayList<Module> modules;
    public String name;
    public String imgURL;
    public Date lastModified;
    public String description;

    public Lesson (String name, ArrayList<Module> modules, String imgURL) {
        this.modules = modules;
        this.name = name;
        this.imgURL = imgURL;
    }

    public Lesson (String name, ArrayList<Module> modules, String imgURL, Date lastModified) {
        this.modules = modules;
        this.name = name;
        this.imgURL = imgURL;
        this.lastModified = lastModified;
    }

    public Lesson (String name) {
        this.name = name;
        this.modules = new ArrayList<>();
    }

    public Lesson (String name, String description) {
        this.name = name;
        this.description = description;
        this.modules = new ArrayList<>();
    }

    public void addModule(Module module) {
        if (modules == null) {
            modules = new ArrayList<>();
        }
        modules.add(module);
    }

    public String getName(){
        return this.name;
    }

    public Module findModule(String moduleName) {
        Module toReturn = null;
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(moduleName)) {
                toReturn = module;
            }
        }
        return toReturn;
    }
}
