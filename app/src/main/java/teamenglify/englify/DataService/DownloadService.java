package teamenglify.englify.DataService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Exercise;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Module;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.lesson;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.read;
import static teamenglify.englify.MainActivity.rootDirectory;
import static teamenglify.englify.MainActivity.s3Client;

/**
 * Created by Keith on 05-Mar-17.
 */

public class DownloadService extends AsyncTask<Void, String, Boolean>{
    public final static int DOWNLOAD_LISTING = 0;
    public final static int DOWNLOAD_GRADE = 1;
    private int downloadType;
    private Grade grade;
    private ProgressDialog pd;
    public LinkedList<String> texts;

    public DownloadService(int i) {
        this.downloadType = i;
    }

    public DownloadService (int i, Grade grade) {
        this.downloadType = i;
        this.grade = grade;
    }

    @Override
    public void onPreExecute() {
        Log.d("Englify", "Class DownloadService: Method onPreExecute(): Download Service starting, opening ProgressDialog.");
        pd = new ProgressDialog(mainActivity);
        pd.setTitle("Download");
        pd.setCancelable(false);
        pd.setMax(1);
        pd.show();
    }

    @Override
    public Boolean doInBackground(Void...voids) {
        //change download depending on downloadType
        if (downloadType == DOWNLOAD_LISTING) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading listing.");
            publishProgress("0", "Downloading listings.");
            boolean success = downloadListing();
            if (success) {
                publishProgress("1", "Download complete.");
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else if (downloadType == DOWNLOAD_GRADE && grade != null) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading " + grade.name + " .");
            publishProgress("0", "Downloading " + grade.name + ".");
            downloadGrade();
            publishProgress("3", grade.name + " download complete.");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onProgressUpdate(String...progress) {
        pd.setProgress(Integer.parseInt(progress[0]));
        pd.setMessage(progress[1]);
    }

    @Override
    public void onPostExecute(Boolean result) {
        pd.dismiss();
        Log.d("Englify", "Class DownloadService: Method doInBackground(): Download finish.");
        if (downloadType == DOWNLOAD_LISTING) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Updating downloadedObject in MainActivity.");
            mainActivity.downloadedObject = LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        }
    }

    public boolean downloadListing() {
        //download all objects
        try {
            List<S3ObjectSummary> summaries = getSummaries(mainActivity.rootDirectory);
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Downloaded object listing from AWS S3.");
            //identify grades
            HashMap<String, String> identifiedGrades = new HashMap<>(); //the key is the grade name , the value is the URL for the image.
            for (S3ObjectSummary summary : summaries) {
                String key = summary.getKey();
                String[] keyParts = key.split("/");
                if (keyParts.length == 2) { //the listing for grades will always only have 2 parts ("res/grade01/")
                    //identify if its a grade or a grade picture
                    if (isFolder(keyParts[1])) {
                        //it is a Grade
                        identifiedGrades.put(keyParts[1], "");
                    } else {
                        //it is a grade picture
                        //locate the grade (key) it belongs to
                        Set<String> keySet = identifiedGrades.keySet();
                        String foundKey = "";
                        for (String gradeKey : keySet) {
                            if (keyParts[1].contains(gradeKey)) {
                                foundKey = gradeKey;
                            }
                        }
                        //if key is found, add img URL as the value of the key (grade) , else discard the image, the folder does not exist.
                        if (!foundKey.isEmpty()) {
                            identifiedGrades.put(foundKey, keyParts[1]);
                        }
                    }
                }
            }
            //Generate grades based on identified grades
            Set<String> listOfGrades = identifiedGrades.keySet();
            List<String> sortedListOfGrades = new ArrayList(listOfGrades);
            Collections.sort(sortedListOfGrades);
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Identified grades: " + listOfGrades.toString());
            ArrayList<Grade> grades = new ArrayList<Grade>();
            for (String gradeName : sortedListOfGrades) {
                //save each grade into internal storage
                Grade newGrade = new Grade(gradeName, null, null, false);
                LocalSave.saveObject(newGrade.name, newGrade);
                grades.add(newGrade);
            }
            //save the grades to internal storage
            LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), new RootListing(grades));
        } catch (Exception e) {
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Caught Exception: " + e.toString());
            return false;
        }
        return true;
    }

    public void downloadGrade() {
        //download grade tree from S3.
        List<S3ObjectSummary> summaries = getSummaries(mainActivity.getString(R.string.Root_Directory) + "/" + grade.name);
        //Iterate through all summaries and use each entry to generate the correct Model.
        for (S3ObjectSummary summary : summaries) {
            String key = summary.getKey();
            String[] delimitedKey = key.split("/");
            int delimitedKeyLength = delimitedKey.length;
            if (delimitedKeyLength == 3) { //it is a Lesson!
                Lesson lesson = new Lesson(delimitedKey[2]);
                Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.name + " <- " + lesson.name  + " added.");
                grade.addLesson(lesson);
            } else if (delimitedKeyLength == 4) { //It is a module
                //Need to identify which module it is
                if (delimitedKey[3].equalsIgnoreCase("Conversation")) { // It is a conversation
                    Conversation conversation = new Conversation(delimitedKey[3]);
                    Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + conversation.name  + " added.");
                    grade.findLesson(delimitedKey[2]).addModule(conversation);
                } else if (delimitedKey[3].equalsIgnoreCase("Vocabulary")) {//It is a vocabulary
                    Vocab vocab = new Vocab(delimitedKey[3]);
                    Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + vocab.name  + " added.");
                    grade.findLesson(delimitedKey[2]).addModule(vocab);
                } else if (delimitedKey[3].equalsIgnoreCase("Exercise")) {//It is an exercise
                    Exercise exercise = new Exercise(delimitedKey[3]);
                    Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + exercise.name  + " added.");
                    grade.findLesson(delimitedKey[2]).addModule(exercise);
                } else { //unidentified module
                    Log.d("Englify", "Class DownloadService: Method downloadGrade(): Unknown module found -> " + key);
                }
            }
        }
        //download all the data in each module (it is seperated from the iterator above due to key naming overlaps.
        downloadGradeModules(grade);
        //once all the data has been downloaded. Set isDownloaded in grade to true and save the grade to internal memory
        grade.isDownloaded = true;
        LocalSave.saveObject(grade.name, grade);
        Log.d("Englify", "Class DownloadService: Method downloadGrade(): Finished downloading " + grade.name + " and saved to internal memory.");
    }

    public void downloadGradeModules(Grade grade) {
        for (Lesson lesson : grade.lessons) {
            for (Module module : lesson.modules) {
                if (module instanceof Conversation) {
                    Conversation conversation = (Conversation) module;
                    //get the reads
                    Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): " + rootDirectory + grade.name + lesson.name + conversation.name);
                    String prefix = generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name);
                    List<S3ObjectSummary> summaries = getSummaries(prefix);
                    for (S3ObjectSummary summary : summaries) {
                        String key = summary.getKey();
                        if (!key.isEmpty()) {
                            Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Running key for Conversation -> " + key);
                            String[] delimitedKey = key.split("/");
                            if (delimitedKey.length == 5 && key.contains("Conversation")) { // Its a read!
                                Read read = new Read(delimitedKey[4]);
                                Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): " + conversation.name + " <- " + read.name + " added.");
                                conversation.addRead(read);
                            }
                        }

                    }
                } else if (module instanceof Vocab) {
                    Vocab vocab = (Vocab) module;
                    Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Going through Vocab -> " + vocab.name);
                    List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name));
                    for (S3ObjectSummary summary : summaries) {
                        String key = summary.getKey();
                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Running key for Conversation -> " + key);
                        if (!key.isEmpty()) {
                            if (key.contains(vocab.name)) {
                                String[] delimitedKey = key.split("/");
                                if (delimitedKey.length == 5) { // Its a vocab part
                                    if (isTextFile(delimitedKey[4])) { //Text for all vocab parts
                                        String prefix = generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]);
                                        System.out.println("Prefix -> " + prefix);
                                        texts = readTextFile(s3Client.getObject(bucketName, prefix));
                                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Text file found for " + prefix);
                                    } else if (isAudioFile(delimitedKey[4])) { //Audio for vocabPart
                                        S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                        String audioAbsolutePath = LocalSave.saveMedia(generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]) ,s3Object);
                                        vocab.addVocabPartAudio(delimitedKey[4], audioAbsolutePath);
                                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Audio file for " + generatePrefix(grade.name, lesson.name, vocab.name) + " saved to " + audioAbsolutePath);
                                    } else if (isImg(delimitedKey[4])) {//Img for vocabPart
                                        S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                        String imgAbsolutePath = LocalSave.saveMedia(generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]) ,s3Object);
                                        vocab.addVocabPartImg(delimitedKey[4], imgAbsolutePath);
                                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Image file for " + generatePrefix(grade.name, lesson.name, vocab.name) + " saved to " + imgAbsolutePath);
                                    }
                                }
                            }
                        }
                    }
                    //overwrite the texts in VocabParts from the txt file
                    vocab.overwriteTexts(texts);
                    texts = null;
                } else if (module instanceof  Exercise) {
                    //Implement when exercise is implemented.
                }
            }
        }
        downloadReadParts(grade);
    }

    public void downloadReadParts(Grade grade) {
        for (Lesson lesson : grade.lessons) {
            for (Module module : lesson.modules) {
                if (module instanceof Conversation) {
                    Conversation conversation = (Conversation) module;
                    for (Read read : conversation.reads) { //FOR EACH READ
                        List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name));
                        for (S3ObjectSummary summary : summaries) { // DO EACH READ PART ONE BY ONE (ANGRY CODING!)
                            String key = summary.getKey();
                            String[] dKey = key.split("/");
                            if (isTextFile(key)) {
                                texts = readTextFile(s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5])));
                                Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Text file found for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name));
                            } else if (isAudioFile(key)) {
                                S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                String audioAbsolutePath = LocalSave.saveMedia(generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                read.addReadPartAudio(dKey[5], audioAbsolutePath);
                                Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Audio file for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name) + " saved to " + audioAbsolutePath);
                            } else if (isImg(key)) {
                                S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                String imgAbsolutePath = LocalSave.saveMedia(generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                read.addReadPartImg(dKey[5], imgAbsolutePath);
                                Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Image file for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name) + " saved to " + imgAbsolutePath);
                            }
                        }
                        //Overwrite the readParts name
                        read.overwriteTexts(texts);
                        texts = null;
                    }
                }
            }
        }
    }

    public List<S3ObjectSummary> getSummaries(String prefix) {
        ObjectListing listing = s3Client.listObjects(mainActivity.getString(R.string.Bucket_Name), prefix);
        return listing.getObjectSummaries();
    }

    public String generatePrefix(String...params) {
        String toReturn = "";
        for (String param : params) {
            toReturn = toReturn + param + "/";
        }
        //get rid of last "/"
        toReturn = toReturn.substring(0, toReturn.length()-1);
        return toReturn;
    }

    public boolean isFolder(String folderName) {
        if (folderName.contains(".txt") || folderName.contains(".png") || folderName.contains(".jpg") || folderName.contains(".mp3")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isTextFile(String fileName) {
        if (fileName.contains(".txt")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAudioFile(String fileName) {
        if (fileName.contains(".mp3")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isImg(String fileName) {
        if (fileName.contains(".png") || fileName.contains(".jpg")) {
            return true;
        } else {
            return false;
        }
    }

    public LinkedList<String> readTextFile(S3Object s3Object) {
        LinkedList<String> toReturn = new LinkedList<>();
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
        String s;
        try {
            while ((s = in.readLine()) != null) {
                toReturn.add(s);
            }
        } catch (IOException e) {
            Log.d("Englify", "Class DownloadService: Method readTextFile: Caught exception -> " + e.toString());
        }
        return toReturn;
    }
}
