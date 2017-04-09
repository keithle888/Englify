package teamenglify.englify.DataService;

import android.util.Log;

import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.amazonaws.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.AnalyticsConfiguration;

public class S3Properties {
    public static String IDENTITYPOOLID;
    public static String ANALYTICSID;
    public static int analyticsPercentage;
    public static int vocabConfiguration;
    public static int readConfiguration;
    public static int exerciseConfiguration;
    private static boolean isLoadedAnalytics = true;

    static {
        IDENTITYPOOLID = "ap-northeast-1:bb71a448-f550-493f-b62e-71c6ecdcd6cb";
        ANALYTICSID = "5f65141e2d474ab8a32b34e35e927750";
        analyticsPercentage = 80;
    }


    public S3Properties() {
        try{
            S3Object s3AnalyticsConfiguration = MainActivity.s3Client.getObject("Englify", "readme/configuration.csv");
            List<String> configurationList = DownloadService.readTextFile(s3AnalyticsConfiguration);
            Log.d("S3Pro", configurationList.toString());
            vocabConfiguration = Integer.parseInt(configurationList.get(2));
            readConfiguration = Integer.parseInt(configurationList.get(4));
            exerciseConfiguration = Integer.parseInt(configurationList.get(6));
            AnalyticsConfiguration analyticsConfiguration = new AnalyticsConfiguration(vocabConfiguration,readConfiguration,exerciseConfiguration);
            LocalSave.saveObject("AnalyticsConfiguration", analyticsConfiguration);
        } catch (Exception e){
            Log.d("S3Properties", "failed to save analyticsConfiguration");
            e.printStackTrace();
            isLoadedAnalytics = false;
        }

        AnalyticsConfiguration analyticsConfiguration = (AnalyticsConfiguration) LocalSave.loadObject("AnalyticsConfiguration");

        if(analyticsConfiguration==null){
            vocabConfiguration = 80;
            readConfiguration = 80;
            exerciseConfiguration = 80;
        }

        if(analyticsConfiguration!=null && isLoadedAnalytics){
            vocabConfiguration = analyticsConfiguration.getVocabConfiguration();
            readConfiguration = analyticsConfiguration.getReadConfiguration();
            exerciseConfiguration = analyticsConfiguration.getExerciseConfiguration();
        }


    }
}
