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

    public Lesson (String name, ArrayList<Module> modules, String imgURL) {
        this.modules = modules;
        this.name = name;
        this.imgURL = imgURL;
    }

    public Lesson (String name) {
        this.name = name;
    }

    public void addModule(Module module) {
        if (modules == null) {
            modules = new ArrayList<Module>();
        }
        modules.add(module);
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
