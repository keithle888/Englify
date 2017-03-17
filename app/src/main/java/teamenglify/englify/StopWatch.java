package teamenglify.englify;

/**
 * Created by keith on 22-Feb-17.
 * test
 */

public class StopWatch {
    private long start;

    public StopWatch() {
        start = 0;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public long lapTime() {
        long lapTime = System.currentTimeMillis() - start;
        return lapTime;
    }

    public long stop() {
        long elapsedTime = System.currentTimeMillis() - start;
        start = 0;
        return elapsedTime;
    }

    public boolean isRunning() {
        if (start != 0) {
            return true;
        } else {
            return false;
        }
    }
}
