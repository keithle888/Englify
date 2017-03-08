package teamenglify.englify.DataService;

import android.util.Log;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import teamenglify.englify.MainActivity;

public class ListingDataService implements Runnable{
    public String listingType;
    public static boolean s3Connection;

    public ArrayList<String> listOfChoices = new ArrayList<>();
    private List<S3ObjectSummary> s3objects;
    private String prefix;
    //arraylist for audio file keys
    private ArrayList<String> audioListOfChoices = new ArrayList<String>();
    private ArrayList<String> audioTextsToMatch = new ArrayList<>();

    public ListingDataService(String listingType) {
        this.listingType = listingType;
    }

    public ArrayList<String> getListOfChoices() {
        return listOfChoices;
    }

    public void run() {
        s3Connection = true;
        try{
        AmazonS3Client s3Client = MainActivity.s3Client;
        Log.d("ListingDataService", listingType);
        if(listingType.equalsIgnoreCase("gradeListing")) {
            prefix = "res/Grade";
            s3objects = s3Client.listObjects("englify",prefix).getObjectSummaries();
            for(S3ObjectSummary temp : s3objects){
                String key = temp.getKey();
                String [] keyArr = key.split("/");
                if(keyArr.length==2) {
                    listOfChoices.add(keyArr[1]);
                }
            }
            MainActivity.getMainActivity().setGradeListing(listOfChoices);
        } else if (listingType.equalsIgnoreCase("lessonListing")) {
            prefix = "res/" + MainActivity.getMainActivity().grade;
            s3objects = s3Client.listObjects("englify",prefix).getObjectSummaries();

            for(S3ObjectSummary temp : s3objects){
                String key = temp.getKey();
                String [] keyArr = key.split("/");
                if(keyArr.length==3) {
                    listOfChoices.add(keyArr[2]);
                }
            }
            MainActivity.getMainActivity().setLessonListing(listOfChoices);
        } else if (listingType.equalsIgnoreCase("readListing")) {
            prefix = "res/" + MainActivity.grade + "/" + MainActivity.lesson + "/Conversation";
            s3objects = s3Client.listObjects("englify",prefix).getObjectSummaries();

            for (S3ObjectSummary temp : s3objects) {
                String key = temp.getKey();
                String[] keyArr = key.split("/");
                if (keyArr.length ==5) {
                    if(!keyArr[4].endsWith(".txt")) {
                        listOfChoices.add(keyArr[4]);
                    }
                }
            }
            MainActivity.getMainActivity().setReadListing(listOfChoices);
        } else if (listingType.equalsIgnoreCase("vocabListing")) {
            //set boolean values to false so audiobar and speechrecognition wait until all the data is loaded.
            MainActivity.getMainActivity().readyForSpeechRecognitionToLoad = false;
            String s3UrlPrefix =  "https://s3-ap-southeast-1.amazonaws.com/englify/";
            prefix = "res/" + MainActivity.grade + "/" + MainActivity.lesson;
            s3objects = s3Client.listObjects("englify",prefix).getObjectSummaries();
            for(S3ObjectSummary temp : s3objects){
                String keyLine = temp.getKey();
                String[] key =keyLine.split("/");
                if(key.length>4) {
                    if (key[4].equals("Vocabulary.txt")) {
                        String vocabURL = s3UrlPrefix + prefix+"/"+"Vocabulary/"+key[4];
                        ReadTextDataService readText = new ReadTextDataService(vocabURL);
                        listOfChoices = readText.getVocabList();
                    }  else if (key[3].contains("Vocabulary") && key[4].endsWith(".mp3")) {
                        //Capture audio files URL.
                        String audioURL = s3UrlPrefix + prefix + "/Vocabulary/" + key[4];
                        audioListOfChoices.add(audioURL);
                    }
                }
            }
            //set words for in the vocab.
            MainActivity.getMainActivity().setVocabListing(listOfChoices);
            MainActivity.getMainActivity().readyForSpeechRecognitionToLoad = true;
            if (audioListOfChoices.size() != 0) {
                MainActivity.getMainActivity().audioVocabURLListing = audioListOfChoices;
                //MainActivity.getMainActivity().readyForAudioBarToLoad = true;
            }
        } else if (listingType.equalsIgnoreCase("readImageListing")){
            //set boolean values so audioBar and speechRecognition do not load until all the data is loaded.
            //MainActivity.getMainActivity().readyForAudioBarToLoad = false;
            MainActivity.getMainActivity().readyForSpeechRecognitionToLoad = false;
            Log.d("ListingDataService", "listing for read image is loaded");
            String s3UrlPrefix =  "https://s3-ap-southeast-1.amazonaws.com/englify/";
            prefix = "res/" + MainActivity.grade + "/" + MainActivity.lesson + "/Conversation/" + MainActivity.read;
            Log.d("ListingDataService", "Prefix set: " + prefix);
            s3objects = s3Client.listObjects("englify",prefix).getObjectSummaries();
            for(S3ObjectSummary temp : s3objects){
                String keyLine = temp.getKey();
                String[] key =keyLine.split("/");
                if(key.length==6) {
                    if (key[5].endsWith(".png")) {
                        String readURL = s3UrlPrefix + prefix+"/"+key[5];
                        listOfChoices.add(readURL);
                    } else if (key[5].endsWith(".mp3")) {
                        String audioURL = s3UrlPrefix + prefix + "/" + key[5];
                        audioListOfChoices.add(audioURL);
                    } else if (key[5].endsWith("Conversation.txt")) {
                        S3Object s3Object = s3Client.getObject(MainActivity.bucketName, prefix + "/" + key[5]);
                        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
                        BufferedReader in = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
                        String s;
                        try {
                            while ((s = in.readLine()) != null) {
                                audioTextsToMatch.add(s);
                            }
                        } catch (IOException e) {
                            Log.d("ListingDataService", "Error trying to capture conversation texts: " + e.toString());
                        }
                    }
                }
            }
            MainActivity.getMainActivity().setReadImageListing(listOfChoices);
            if (audioListOfChoices.size() != 0) {
                MainActivity.getMainActivity().audioConversationURLListing = audioListOfChoices;
                //MainActivity.getMainActivity().readyForAudioBarToLoad = true;

            } else {
                Log.d("ListingDataService", "Error: audioListOfChoices.size = 0");
            }
            if (audioTextsToMatch.size() != 0) {
                MainActivity.getMainActivity().audioConversationTextsToMatch = audioTextsToMatch;
                Log.d("ListingDataService", "Found and set audioConversationTextsToMatch: " + audioTextsToMatch.toString());
                MainActivity.getMainActivity().readyForSpeechRecognitionToLoad = true;
            } else {
                Log.d("ListingDataService", "Error: audioConversationTextToMatch.size = 0");
            }
        }

        } catch (Exception e){
            Log.d("ListingDataService", e.getMessage());
            s3Connection = false;
        }
    }
}
