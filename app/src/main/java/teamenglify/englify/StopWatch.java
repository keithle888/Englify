package teamenglify.englify;

/**
 * Created by keith on 22-Feb-17.
 * test
 */

public class StopWatch {
    private long start;

    public StopWatch() { }

    public void start() {
        start = System.currentTimeMillis();
    }

    public long lapTime() {
        long lapTime = start - System.currentTimeMillis();
        return lapTime;
    }

    public long stop() {
        long elapsedTime = start - System.currentTimeMillis();
        return elapsedTime;
    }
}
