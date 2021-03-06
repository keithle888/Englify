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

    public Lesson (String name, Date lastModified) {
        this.name = name;
        this.modules = new ArrayList<>();
        this.lastModified = lastModified;
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

    public <T> Module findModuleByType(Class<T> moduleClass) {
        for (Module module: modules) {
            if (moduleClass.isInstance(module)) {
                return module;
            }
        }
        return null;
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
