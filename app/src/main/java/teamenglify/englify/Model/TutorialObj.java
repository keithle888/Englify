package teamenglify.englify.Model;
import java.io.Serializable;

public class TutorialObj implements Serializable {
    private boolean firstTime;

    public TutorialObj(boolean firstTime){
        this.firstTime = firstTime;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }
}
