package teamenglify.englify.DataService;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.AnalyticsConfiguration;
import timber.log.Timber;

public class S3Properties {
    public static String IDENTITYPOOLID;
    public static String ANALYTICSID;
    public static int analyticsPercentage;
    public static int vocabConfiguration;
    public static int readConfiguration;
    public static int exerciseConfiguration;
    private static boolean isLoadedAnalytics = true;
    public static String s3BucketName = "englifybucket";

    static {
        IDENTITYPOOLID = "us-east-1:763fc062-6363-4f20-baab-e4b850aecabb";
        ANALYTICSID = "5f65141e2d474ab8a32b34e35e927750";
        analyticsPercentage = 80;
    }


    public S3Properties() {
        try{
            S3Object s3AnalyticsConfiguration = MainActivity.s3Client.getObject(s3BucketName, "readme/configuration.csv");
            List<String> configurationList = DownloadService.readTextFile(s3AnalyticsConfiguration);
            Timber.d(configurationList.toString());
            vocabConfiguration = Integer.parseInt(configurationList.get(2));
            readConfiguration = Integer.parseInt(configurationList.get(4));
            exerciseConfiguration = Integer.parseInt(configurationList.get(6));
            AnalyticsConfiguration analyticsConfiguration = new AnalyticsConfiguration(vocabConfiguration,readConfiguration,exerciseConfiguration);
            LocalSave.saveObject("AnalyticsConfiguration", analyticsConfiguration);
        } catch (Exception e){
            Timber.d( "failed to save analyticsConfiguration");
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

    public static CognitoCachingCredentialsProvider getCredentialsProvider(Context context) {
        return new CognitoCachingCredentialsProvider(
                context,
                IDENTITYPOOLID,
                Regions.US_EAST_1
        );
    }
}
