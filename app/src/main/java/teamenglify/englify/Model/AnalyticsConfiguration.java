package teamenglify.englify.Model;

public class AnalyticsConfiguration {
    private int vocabConfiguration;
    private int readConfiguration;
    private int exerciseConfiguration;

    public AnalyticsConfiguration(int vocabConfiguration, int readConfiguration, int exerciseConfiguration) {
        this.vocabConfiguration = vocabConfiguration;
        this.readConfiguration = readConfiguration;
        this.exerciseConfiguration = exerciseConfiguration;
    }

    public int getVocabConfiguration() {
        return vocabConfiguration;
    }

    public void setVocabConfiguration(int vocabConfiguration) {
        this.vocabConfiguration = vocabConfiguration;
    }

    public int getReadConfiguration() {
        return readConfiguration;
    }

    public void setReadConfiguration(int readConfiguration) {
        this.readConfiguration = readConfiguration;
    }

    public int getExerciseConfiguration() {
        return exerciseConfiguration;
    }

    public void setExerciseConfiguration(int exerciseConfiguration) {
        this.exerciseConfiguration = exerciseConfiguration;
    }
}
