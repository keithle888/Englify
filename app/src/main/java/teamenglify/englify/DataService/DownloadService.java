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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import teamenglify.englify.LocalSave;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Exercise;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Module;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.ReadPart;
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
    private String baseMessage = "Downloading:";

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
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(baseMessage);
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    public Boolean doInBackground(Void...voids) {
        //change download depending on downloadType
        if (downloadType == DOWNLOAD_LISTING) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading listing.");
            publishProgress( "Downloading listings.");
            boolean success = downloadListing();
            if (success) {
                publishProgress( "Download complete.");
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else if (downloadType == DOWNLOAD_GRADE && grade != null) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading " + grade.name + " .");
            publishProgress(grade.name);
            try {
                downloadGrade();
            } catch (Exception e) {
                return false;
            }
            publishProgress(grade.name + " download complete.");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onProgressUpdate(String...progress) {
        if (progress != null) {
            pd.setMessage(baseMessage + "\n" + progress[0]);
        }
        pd.setProgress(pd.getProgress() + 1);
    }

    @Override
    public void onPostExecute(Boolean result) {
        pd.dismiss();
        if (result == true) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Download finish.");
            if (downloadType == DOWNLOAD_LISTING) {
                Log.d("Englify", "Class DownloadService: Method doInBackground(): Updating downloadedObject in MainActivity.");
                mainActivity.downloadedObject = LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
            } else if (downloadType == DOWNLOAD_GRADE) {
                Log.d("Englify", "Class DownloadService: Method doInBackground(): Updating downloadedObject in MainActivity.");
                mainActivity.downloadedObject = grade;
            }
        } else {
            if (downloadType == DOWNLOAD_LISTING) {
                new DeleteService().deleteRootListing();
            } else if (downloadType == DOWNLOAD_GRADE) {
                new DeleteService(grade).deleteGrade();
            }
            mainActivity.onBackPressed();
            Toast.makeText(mainActivity, R.string.Download_Failed, Toast.LENGTH_LONG).show();
        }
    }

    public boolean downloadListing() {
        //download all objects
        try {
            List<S3ObjectSummary> summaries = getSummaries(mainActivity.rootDirectory);
            pd.setMax(summaries.size());
            pd.setProgress(0);
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Downloaded object listing from AWS S3.");
            //identify grades
            HashMap<String, String> identifiedGrades = new HashMap<>(); //the key is the grade name , the value is the URL for the image.
            for (S3ObjectSummary summary : summaries) {
                String key = summary.getKey();
                publishProgress(key);
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
                Grade newGrade = new Grade(gradeName, new ArrayList<Lesson>(), null, false);
                Log.d("Englify", "Class DownloadService: Method downloadListing(): Created grade -> " + newGrade.toString());
                grades.add(newGrade);
            }
            //save the grades to internal storage
            LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), new RootListing(grades));
        } catch (Exception e) {
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Caught Exception: " + e.toString());
            throw e;
        }
        return true;
    }

    public void downloadGrade() {
        try {
            //download grade tree from S3.
            List<S3ObjectSummary> summaries = getSummaries(mainActivity.getString(R.string.Root_Directory) + "/" + grade.name);
            pd.setMax(summaries.size());
            pd.setProgress(0);
            //Iterate through all summaries and use each entry to generate the correct Model.
            for (S3ObjectSummary summary : summaries) {
                String key = summary.getKey();
                String[] delimitedKey = key.split("/");
                int delimitedKeyLength = delimitedKey.length;
                if (delimitedKeyLength == 3) { //it is a Lesson!
                    Lesson lesson = new Lesson(delimitedKey[2]);
                    Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.name + " <- " + lesson.name + " added.");
                    publishProgress(key);
                    grade.addLesson(lesson);
                } else if (delimitedKeyLength == 4) { //It is a module
                    //Need to identify which module it is
                    if (delimitedKey[3].equalsIgnoreCase("Conversation")) { // It is a conversation
                        Conversation conversation = new Conversation(delimitedKey[3]);
                        Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + conversation.name + " added.");
                        publishProgress(key);
                        grade.findLesson(delimitedKey[2]).addModule(conversation);
                    } else if (delimitedKey[3].equalsIgnoreCase("Vocabulary")) {//It is a vocabulary
                        Vocab vocab = new Vocab(delimitedKey[3]);
                        Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + vocab.name + " added.");
                        publishProgress(key);
                        grade.findLesson(delimitedKey[2]).addModule(vocab);
                    } else if (delimitedKey[3].equalsIgnoreCase("Exercise")) {//It is an exercise
                        Exercise exercise = new Exercise(delimitedKey[3]);
                        Log.d("Englify", "Class DownloadService: Method downloadGrade(): " + grade.findLesson(delimitedKey[2]).name + " <- " + exercise.name + " added.");
                        publishProgress(key);
                        grade.findLesson(delimitedKey[2]).addModule(exercise);
                    } else { //unidentified module
                        publishProgress(key);
                        Log.d("Englify", "Class DownloadService: Method downloadGrade(): Unknown module found -> " + key);
                    }
                }
            }
            //download all the data in each module (it is seperated from the iterator above due to key naming overlaps.
            downloadGradeModules(grade);
            //once all the data has been downloaded. Set isDownloaded in grade to true and save the grade to internal memory
            grade.isDownloaded = true;
            updateGradeModificationDate();
            RootListing rootListing = (RootListing) LocalSave.loadObject(R.string.S3_Object_Listing);
            rootListing.overrideGrade(grade);
            LocalSave.saveObject(R.string.S3_Object_Listing, rootListing);
            Log.d("Englify", "Class DownloadService: Method downloadGrade(): Finished downloading " + grade.name + " and saved to internal memory.");
        } catch (Exception e) {
            //data downloading failed. Delete all downloaded Data.
            Log.d("Englify", "Class DownloadService: Method downloadGrade(): Caught Exception -> " + e.toString());
            throw e;
        }
    }

    public void downloadGradeModules(Grade grade) {
        try {
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
                                String[] delimitedKey = key.split("/");
                                if (delimitedKey.length == 5 && key.contains("Conversation")) { // Its a read!
                                    Read read = new Read(delimitedKey[4], new ArrayList<ReadPart>());
                                    publishProgress(key);
                                    Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): " + conversation.name + " <- " + read.name + " added.");
                                    conversation.addRead(read);
                                }
                            }
                        }
                    } else if (module instanceof Vocab) {
                        Vocab vocab = (Vocab) module;
                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Going through Vocab -> " + vocab.name);
                        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, vocab.name);
                        if (summaries != null && summaries.size() != 0) {
                            for (S3ObjectSummary summary : summaries) {
                                String key = summary.getKey();
                                Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Running key for Conversation -> " + key);
                                if (key.contains(vocab.name)) {
                                    String[] delimitedKey = key.split("/");
                                    if (delimitedKey.length == 5) { // Its a vocab part
                                        if (isTextFile(delimitedKey[4])) { //Text for all vocab parts
                                            String prefix = generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]);
                                            publishProgress(key);
                                            texts = readTextFile(s3Client.getObject(bucketName, prefix));
                                            Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Text file found for " + prefix);
                                        } else if (isAudioFile(delimitedKey[4])) { //Audio for vocabPart
                                            S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                            LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]), s3Object);
                                            publishProgress(key);
                                            vocab.addVocabPartAudio(removeExtension(delimitedKey[4]), createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                            Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Audio file for " + generatePrefix(grade.name, lesson.name, vocab.name) + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                        } else if (isImg(delimitedKey[4])) {//Img for vocabPart
                                            S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                            LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]), s3Object);
                                            publishProgress(key);
                                            vocab.addVocabPartImg(removeExtension(delimitedKey[4]), createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                            Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): Image file for " + generatePrefix(grade.name, lesson.name, vocab.name) + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, vocab.name, delimitedKey[4]));
                                        }
                                    }
                                }
                            }
                        }
                        //overwrite the texts in VocabParts from the txt file
                        Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): LinkedTextArray contents before overwriting Vocab -> " + texts.toString());
                        vocab.overwriteTexts(texts);
                        texts = null;
                    } else if (module instanceof Exercise) {
                        Exercise exercise = (Exercise) module;
                        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, exercise.name);
                        for (S3ObjectSummary summary : summaries) {
                            String key = summary.getKey();
                            if (key.contains(grade.name) && key.contains(lesson.name) && key.contains(exercise.name)) { //ensures the correct keys for the correct exercises are used. If not the app may save the wrong exercises.
                                String[] dKey = key.split("/");
                                if (dKey.length == 5) { //It is a Exercise Chapter
                                    exercise.chapters.add(new ExerciseChapter(dKey[4]));
                                    publishProgress(key);
                                }
                            }
                        }
                    }
                }
            }
            downloadReadParts(grade);
            downloadExerciseChapterParts(grade);
        } catch (Exception e) {
            Log.d(bucketName, "Class DownloadService: Method downloadGradeModules(): Exception caught -> " + e.toString());
            e.printStackTrace();
            throw e;
        }
    }

    public void downloadReadParts(Grade grade) {
        try {
            for (Lesson lesson : grade.lessons) {
                for (Module module : lesson.modules) {
                    if (module instanceof Conversation) {
                        Conversation conversation = (Conversation) module;
                        for (Read read : conversation.reads) { //FOR EACH READ
                            List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name));
                            Log.d(bucketName, "Class DownloadService: Method downloadReadParts(): Downloading ReadParts for => " + generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name));
                            for (S3ObjectSummary summary : summaries) { // DO EACH READ PART ONE BY ONE (ANGRY CODING!)
                                String key = summary.getKey();
                                String[] dKey = key.split("/");
                                if (dKey.length == 6) {
                                    if (isTextFile(key)) {
                                        texts = readTextFile(s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5])));
                                        publishProgress(key);
                                        Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Text file found for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name));
                                    } else if (isAudioFile(key)) {
                                        S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                        LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                        publishProgress(key);
                                        read.addReadPartAudio(removeExtension(dKey[5]), createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                        Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Audio file for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name) + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    } else if (isImg(key)) {
                                        S3Object s3Object = s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                        LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                        publishProgress(key);
                                        read.addReadPartImg(removeExtension(dKey[5]), createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                        Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Image file for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name) + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    }
                                }
                            }
                            //Overwrite the readParts name
                            if (texts != null && texts.size() != 0) {
                                read.overwriteTexts(texts);
                            }
                            texts = null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(bucketName, "Class DownloadService: Method downloadReadParts(): Exception caught -> " + e.toString());
            throw e;
        }
    }

    public void downloadExerciseChapterParts(Grade grade) {
        for (Lesson lesson : grade.lessons) {
        }
    }

    public void updateGradeModificationDate() {
        List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name));
        for (S3ObjectSummary summary : summaries) {     //Iterate through all the summaries for that grade, to find out what was the latest modification for the grade, and its lessons.
            String key = summary.getKey();
            String[] sKey = key.split("/");
            Date date = summary.getLastModified();
            if (sKey.length >= 2 && sKey[1].equalsIgnoreCase(grade.name)) { //It is part of the grade, we want to capture the latest date of all components under that grade
                if (grade.lastModified == null) {
                    grade.lastModified = date;
                } else if (grade.lastModified.before(date)){
                    grade.lastModified = date;
                }
                if (sKey.length >= 3) {     //It is a lesson, or a component of a lesson, we want to capture the latest date of all components under that lesson.
                    Lesson lesson = grade.findLesson(sKey[2]);
                    if (lesson.lastModified == null) {
                        lesson.lastModified = date;
                    } else if (lesson.lastModified.before(date)) {
                        lesson.lastModified = date;
                    }
                }
            }
        }
        //Print the latest modified dates for grade and its lessons.
        Log.d(bucketName, "Class DownloadService: Method updateGradeModificationDatE(): Latest modifications date for " + grade.name + " -> " + grade.lastModified.toString());
        for (Lesson lesson : grade.lessons) {
            Log.d(bucketName, "Class DownloadService: Method updateGradeModificationDatE(): Latest modifications date for " + lesson.name + " -> " + lesson.lastModified.toString());
        }
    }

    public static List<S3ObjectSummary> getSummaries(String prefix) {
        ObjectListing listing = s3Client.listObjects(mainActivity.getString(R.string.Bucket_Name), prefix);
        return listing.getObjectSummaries();
    }

    public static List<S3ObjectSummary> getSummaries(String...params) {
        String prefix = generatePrefix(params);
        ObjectListing listing = s3Client.listObjects(mainActivity.getString(R.string.Bucket_Name), prefix);
        return listing.getObjectSummaries();
    }

    public static String generatePrefix(String...params) {
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

    public String createMediaFileName(String...params) {
        String toReturn = "";
        for (String param : params) {
            toReturn = toReturn + param + "_";
        }
        //get rid of last "/"
        toReturn = toReturn.substring(0, toReturn.length()-1);
        return toReturn;
    }

    public String removeExtension(String name) {
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf('.'));
        } else {
            return name;
        }
    }
}
