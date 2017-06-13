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

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.LocalSave;
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
import teamenglify.englify.ModuleSelection.ModuleSelection;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.lesson;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.rootDirectory;
import static teamenglify.englify.MainActivity.s3Client;

/**
 * DownloadService class is used to download objects from the AWS S3 server and save them to local memory as the appropriate Java objects.
 * DownloadService extends AsyncTask in order to run networking calls on a separate thread while keeping the UI up-to-date with its status.
 */
public class DownloadService extends AsyncTask<Void, String, Boolean> {
    public final static int DOWNLOAD_LISTING_OF_GRADES = 0;
    public final static int DOWNLOAD_LISTING_OF_LESSONS = 1;
    public final static int DOWNLOAD_LESSON = 2;
    private int downloadType;
    private Grade grade;
    private Lesson lesson;
    private ProgressDialog pd;
    public LinkedList<String> texts;
    private String baseMessage = "Downloading:";

    /**
     * Constructor used to download the list of grades available. Parameter should be DownloadService.DOWNLOAD_LISTING.
     *
     * @param i Should be DOWNLOAD_LISTING
     */
    public DownloadService(int i) {
        this.downloadType = i;
    }

    /**
     * Constructor used to download a grade
     *
     * @param downloadType     Based on integer static variables
     * @param grade Grade object to be downloaded. Can found from RootListing object in local memory.
     */
    public DownloadService(int downloadType, Grade grade) {
        this.downloadType = downloadType;
        this.grade = grade;
    }

    public DownloadService(int downloadType, Grade grade, Lesson lesson) {
        this.downloadType = downloadType;
        this.grade = grade;
        this.lesson = lesson;
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
    public Boolean doInBackground(Void... voids) {
        //change download depending on downloadType
        if (downloadType == DOWNLOAD_LISTING_OF_GRADES) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading listing of grades.");
            publishProgress("Downloading listings.");
            return download_list_of_grades();
        } else if (downloadType == DOWNLOAD_LISTING_OF_LESSONS && grade != null) {
            Log.d("Englify", "Class DownloadService: Method doInBackground(): Downloading " + grade.name + " list of lessons.");
            publishProgress(grade.name);
            try {
                download_list_of_lessons();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            publishProgress(grade.name + " download complete.");
            return Boolean.TRUE;
        } else if (downloadType == DOWNLOAD_LESSON){
            Log.d(bucketName, "Class DownloadService: Method doInBackground(): Downloading " + grade.name + "/" + lesson.name + ".");
            try {
                download_lesson();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
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
            if (downloadType == DOWNLOAD_LISTING_OF_GRADES) {
                ((ListingFragment)mainActivity.getSupportFragmentManager().findFragmentByTag("GRADE_LISTING")).mUpdateUIAfterDataLoaded();
            } else if (downloadType == DOWNLOAD_LISTING_OF_LESSONS) {
                ((ListingFragment)mainActivity.getSupportFragmentManager().findFragmentByTag("LESSON_LISTING")).mUpdateUIAfterDataLoaded(grade);
            } else if (downloadType == DOWNLOAD_LESSON) {
                Toast.makeText(mainActivity, "Download Successful.", Toast.LENGTH_LONG).show();
                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_main_container, ModuleSelection.newInstance(lesson),"MODULE_LISTING")
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            if (downloadType == DOWNLOAD_LISTING_OF_GRADES) {
                new DeleteService().deleteRootListing();
            } else if (downloadType == DOWNLOAD_LISTING_OF_LESSONS) {
                mainActivity.clearBackStack();
                mainActivity.loadLoginFragment();
            } else if (downloadType == DOWNLOAD_LESSON) {
                new DeleteService(grade, lesson).execute();
            }
            mainActivity.onBackPressed();
            if (mainActivity.hasInternetConnection == false) {
                Toast.makeText(mainActivity, R.string.Download_Failed_No_Internet, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, R.string.Download_Failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method to download listing for grades available.
     * Is called by AsyncTask implementation in DownloadService.
     * Creates the RootListing object and empty Grade objects (with only names) and saves it to local memory)
     *
     * @return True; if method executes correctly. False; if an exception is caught.
     */
    public boolean download_list_of_grades() {
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
                if (isFolder(key) && key.contains("Grade")) { //Ensure that we are looking at a folder and that the folder name has "grade"
                    String[] dKeys = key.split("/");            //Split key to extract specifically the grade name
                    identifiedGrades.put(dKeys[1], null);
                }
                //Update progress in progress dialog
                pd.setProgress(pd.getProgress() + 1);
            }
            //Generate grades based on identified grades
            Set<String> listOfGrades = identifiedGrades.keySet();
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Identified grades: " + listOfGrades.toString());
            List<String> sortedListOfGrades = new ArrayList(listOfGrades);
            Collections.sort(sortedListOfGrades);
            ArrayList<Grade> grades = new ArrayList<Grade>();
            for (String gradeName : sortedListOfGrades) {
                //save each grade into internal storage
                Grade newGrade = new Grade(gradeName, new ArrayList<Lesson>(), null);
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

    public void download_list_of_lessons() throws Exception{
        //Set progress dialog type
        pd.setIndeterminate(true);
        //Create variables for method
        LinkedList<String> lessonDescriptions = null;
        //get the S3ObjectSummaries for the grade
        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name);
        for (S3ObjectSummary summary : summaries) {
            String path = summary.getKey();
            String[] delimited_path = path.split("/");
            if (isFolder(path) && delimited_path.length > 2) {       //only lessons should be a folder at this directory depth
                String lessonName = delimited_path[2];
                if (grade.findLesson(lessonName) == null) {
                    publishProgress(lessonName);
                    Log.d(bucketName, "Class DownloadService: Method download_list_of_lessons(): Creating " + lessonName + ".");
                    grade.lessons.add(new Lesson(lessonName));
                }
            } else if (isTextFile(path, "LessonDescription")) {              //find the lesson descriptions and handle after all lessons have been created
                lessonDescriptions = readTextFile(s3Client.getObject(bucketName, path));
                Log.d(bucketName, "Class DownloadService: Method download_list_of_lessons(): Description for " + grade.name + " lessons. -> " + lessonDescriptions.toString());
            }
        }
        //Assign lesson descriptions
        if (lessonDescriptions != null) {
            for (String s : lessonDescriptions) {
                //Split the text between associated lesson and description
                String[] sSplit = s.split(":");
                if (!(sSplit.length < 2)) {     //Each line in the description should have the "LessonName" and "Description"
                    String lessonName = sSplit[0];
                    String description = sSplit[1];
                    if (grade.findLesson(lessonName) != null) {
                        grade.findLesson(lessonName).description = description;
                    }
                }
            }
        }
        //Save to local memory
        RootListing rootListing = (RootListing) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        rootListing.overrideGrade(grade);
        LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), rootListing);
    }

    public void download_lesson() {
        //Update Progress Dialog
        pd.setMax(getSummaries(rootDirectory, grade.name, lesson.name).size());
        //Download components in lesson
        download_lesson_vocab();
        download_lesson_conversation();
        //download_lesson_exercise();
        //Save to local memory
        RootListing rootListing = (RootListing) LocalSave.loadObject(mainActivity.getString(R.string.S3_Object_Listing));
        rootListing.overrideGrade(grade);
        LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), rootListing);
    }

    public void download_lesson_vocab() {
        //Create variables
        LinkedList<String> vocabDescriptions = null;
        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, "Vocabulary");
        if (summaries != null) {
            Vocab vocab = new Vocab("Vocabulary");
            if (lesson.findModule("Vocabulary") == null) {
                publishProgress(lesson.name + "/" + "Vocabulary");
                lesson.addModule(vocab);
            } else {
                vocab = (Vocab) lesson.findModule("Vocabulary");
            }
            for (S3ObjectSummary summary : summaries) {
                String path = summary.getKey();
                String[] delimited_path = path.split("/");
                if (delimited_path.length >= 4) {
                    if (isAudioFile(path)) {
                        String vocabPartName = removeExtension(delimited_path[4]);
                        String vocabPartMediaFileName = createMediaFileName(grade.name, lesson.name, vocab.name, delimited_path[4]);
                        publishProgress(vocabPartMediaFileName);
                        LocalSave.saveMedia(vocabPartMediaFileName, s3Client.getObject(bucketName, path));
                        vocab.addVocabPartAudio(vocabPartName, vocabPartMediaFileName);
                        Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Audio file for " + path + " saved to " + vocabPartMediaFileName);
                    } else if (isImg(path)) {
                        String vocabPartName = removeExtension(delimited_path[4]);
                        String vocabPartMediaFileName = createMediaFileName(grade.name, lesson.name, vocab.name, delimited_path[4]);
                        publishProgress(vocabPartMediaFileName);
                        LocalSave.saveMedia(vocabPartMediaFileName, s3Client.getObject(bucketName, path));
                        vocab.addVocabPartImg(vocabPartName, vocabPartMediaFileName);
                        Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Img file for " + path + " saved to " + vocabPartMediaFileName);
                    } else if (isTextFile(path)) {
                        vocabDescriptions = readTextFile(s3Client.getObject(bucketName, path));
                        publishProgress(path);
                    } else {
                        Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Unknown file -> " + path);
                    }
                }
            }
            //Deal with the vocab part descriptions
            if (vocabDescriptions != null) {
                vocab.overwriteTexts(vocabDescriptions);
            }
            //Print vocab contents for debugging
            Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Vocab downloaded with contents => " + vocab.toString());
        } else {
            Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Vocabulary folder for " + lesson.name + " not found.");
        }
    }

    public void download_lesson_conversation(){
        //Create variables
        LinkedList<String> texts = null;
        Conversation conversation = new Conversation("Conversation");
        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, "Conversation");
        if (summaries != null) {
            //Get module if lesson contains it already contains it, if not add it in.
            if (lesson.findModule("Conversation") == null) {
                lesson.addModule(conversation);
                publishProgress(lesson.name + "/" + "Conversation");
            } else {
                conversation = (Conversation) lesson.findModule("Conversation");
            }
            //Run through summaries
            for (S3ObjectSummary summary : summaries) {
                String path = summary.getKey();
                String[] delimited_path = path.split("/");
                if (delimited_path.length == 5 && path.contains("Read")) {
                    conversation.addRead(new Read(delimited_path[4]));
                    publishProgress(path);
                }
            }
            downloadReadParts();
        } else {
            Log.d(bucketName, "Class DownloadService: Method download_lesson_conversation(): No conversation folder found.");
        }
    }

    /**
     * Called by downloadGradeModules().
     * Iterates through all the Reads in the Grade to be downloaded.
     * Creates all the ReadPart (Audio, Images and Text) objects and saves it to local memory.
     *
     */
    public void downloadReadParts() {
        try {
                Conversation conversation = (Conversation) lesson.findModule("Conversation");
                if (conversation != null) {
                    for (Read read : conversation.reads) { //FOR EACH READ
                        List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name));
                        Log.d(bucketName, "Class DownloadService: Method downloadReadParts(): Downloading ReadParts for => " + generatePrefix(rootDirectory, grade.name, lesson.name, conversation.name, read.name));
                        for (S3ObjectSummary summary : summaries) { // DO EACH READ PART ONE BY ONE (ANGRY CODING!)
                            String key = summary.getKey();
                            String[] dKey = key.split("/");
                            if (dKey.length == 6) {
                                if (isTextFile(key)) {
                                    texts = readTextFile(s3Client.getObject(bucketName, key));
                                    publishProgress(key);
                                    Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Text file found for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name));
                                } else if (isAudioFile(key)) {
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
                                    LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                    publishProgress(key);
                                    read.addReadPartAudio(removeExtension(dKey[5]), createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Audio file for " + generatePrefix(grade.name, lesson.name, conversation.name, read.name) + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                } else if (isImg(key)) {
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
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
        } catch (Exception e) {
            Log.d(bucketName, "Class DownloadService: Method downloadReadParts(): Exception caught -> " + e.toString());
            throw e;
        }
    }

    /**
     * Used to insert the lastModified date for the Grade being downloaded as well as all the lessons the grade has.
     * The lastModified date is to be used when checking for updates in the S3 Server.
     */
    public void updateGradeModificationDate() {
        List<S3ObjectSummary> summaries = getSummaries(generatePrefix(rootDirectory, grade.name));
        for (S3ObjectSummary summary : summaries) {     //Iterate through all the summaries for that grade, to find out what was the latest modification for the grade, and its lessons.
            String key = summary.getKey();
            String[] sKey = key.split("/");
            Date date = summary.getLastModified();
            if (sKey.length >= 2 && sKey[1].equalsIgnoreCase(grade.name)) { //It is part of the grade, we want to capture the latest date of all components under that grade
                if (grade.lastModified == null) {
                    grade.lastModified = date;
                } else if (grade.lastModified.before(date)) {
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

    /**
     * Helper method for getting the list of objects from S3 Server
     *
     * @param prefix Folder path to be "listed".
     * @return List of objects in the specified folder.
     */
    public static List<S3ObjectSummary> getSummaries(String prefix) {
        ObjectListing objectListing = s3Client.listObjects(bucketName, prefix);
        List<S3ObjectSummary> originalSummaries = objectListing.getObjectSummaries();
        while (objectListing.isTruncated()) {
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
            originalSummaries.addAll(objectListing.getObjectSummaries());
        }
        return originalSummaries;
    }

    /**
     * Get S3Summaries (paths of all objects in a folder) from S3
     *
     * @param params Name of each folder in descending order.
     * @return Java List object of S3ObjectSummaries
     */
    public static List<S3ObjectSummary> getSummaries(String... params) {
        String prefix = generatePrefix(params);
        return getSummaries(prefix);
    }

    /**
     * Turns folder names into a full path (eg. Grade 4, Lesson 3 to Grade 4/Lesson 3)
     *
     * @param params Folder names in descending order
     * @return Full folder path based on order of params
     */
    public static String generatePrefix(String... params) {
        String toReturn = "";
        for (String param : params) {
            toReturn = toReturn + param + "/";
        }
        //get rid of last "/"
        toReturn = toReturn.substring(0, toReturn.length() - 1);
        return toReturn;
    }

    /**
     * Check whether the S3Object key/path is a folder (does not have an extension eg. .txt)
     *
     * @param folderName path/key of S3ObjectSummary
     * @return true: if it is a folder
     */
    public boolean isFolder(String folderName) {
        if (folderName.contains(".txt") || folderName.contains(".png") || folderName.contains(".jpg") || folderName.contains(".mp3")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check whether S3ObjectSummary path/key is a text file (ends with .txt)
     *
     * @param path
     * @return true; if it is a text file
     */
    public boolean isTextFile(String path) {
        if (path.contains(".txt")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether the S3ObjectSummary path/key is a text file and has the same name as specified in fileName
     *
     * @param path     S3ObjectSummary path/key
     * @param fileName File name you are looking for (eg. Exercise). It is NOT case-sensitive
     * @return true; if there is a match.
     */
    public boolean isTextFile(String path, String fileName) {
        if (path.contains(".txt") && path.toLowerCase().contains(fileName.toLowerCase())) {
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

    public static LinkedList<String> readTextFile(S3Object s3Object) {
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

    /**
     * Turn folder names into an internal device storage path (eg. Grade 1, Lesson 4, 1b.png to Grade 1_Lesson 4_ 1b.png)
     *
     * @param params folder names and file name
     * @return Path to be used for internal storage
     */
    public String createMediaFileName(String... params) {
        String toReturn = "";
        for (String param : params) {
            toReturn = toReturn + param + "_";
        }
        //get rid of last "/"
        toReturn = toReturn.substring(0, toReturn.length() - 1);
        return toReturn;
    }

    /**
     * Removes the extension at the end of file names (.txt) by searching for the last "." in the string and returning everything before it.
     *
     * @param name path/key
     * @return path/key
     */
    public String removeExtension(String name){
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf('.'));
        } else {
            return name;
        }
    }

/********************************************************************************************
 DEPRECATED CODE SECTION
 *********************************************************************************************/


    /**
     * (Deprecated in favor of downloading lesson by lesson) Method for downloading content for a specific grade
     * Called by the AsyncTask implementation in DownloadService.
     * Creates Lesson objects (Lesson and its description) inside each grade and the different Modules (Vocab, Conversation, Exercise) in each lesson.
     */
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
                    if (isTextFile(delimitedKey[2])) {
                        texts = readTextFile(s3Client.getObject(bucketName, generatePrefix(rootDirectory, grade.name, delimitedKey[2])));


                    }
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
            //Insert lesson descriptions
            if (texts != null & texts.size() != 0) {
                grade.overwriteLessonDescriptions(texts);
                texts = null;
            }
            //download all the data in each module (it is seperated from the iterator above due to key naming overlaps.
            downloadGradeModules(grade);
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

    /**
     * (Deprecated along with downloadGrade) Called by downloadGrade()
     * Iterates through all the lessons and its modules.
     * Creates the VocabPart objects, ExerciseChapter objects and Read objects.
     *
     * @param grade Grade to be downloaded.
     */

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
                        if (texts != null && texts.size() != 0) {
                            Log.d("Englify", "Class DownloadService: Method downloadGradeModules(): LinkedTextArray contents before overwriting Vocab -> " + texts.toString());
                            vocab.overwriteTexts(texts);
                        }
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
            downloadReadParts();
            downloadExerciseChapterParts(grade);
        } catch (Exception e) {
            Log.d(bucketName, "Class DownloadService: Method downloadGradeModules(): Exception caught -> " + e.toString());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Called by downloadeGradeModules().
     * Iterates through all the ExerciseChapters for the Grade object.
     * Creates the ExerciseChapterPart (Audio, Image and Text) objects and saves to local memory.
     *
     * @param grade Grade to be downloaded.
     */
    public void downloadExerciseChapterParts(Grade grade) {
        for (Lesson lesson : grade.lessons) {
            for (Module module : lesson.modules) {
                if (module instanceof Exercise) {
                    Exercise exercise = (Exercise) module;
                    for (ExerciseChapter exerciseChapter : exercise.chapters) {
                        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name);
                        for (S3ObjectSummary summary : summaries) {
                            String key = summary.getKey();
                            if (key.contains(grade.name) && key.contains(lesson.name) && key.contains(exercise.name) && key.contains(exerciseChapter.name)) {
                                String[] dKey = key.split("/");
                                if (dKey.length == 6) { //Length 6 is only for ExerciseChapterPart
                                    if (isTextFile(key)) {
                                        publishProgress(key);
                                        texts = readTextFile(s3Client.getObject(bucketName, key));
                                        Log.d(bucketName, "Class DownloadService: Method downloadExerciseChapterParts(): Found text file -> " + key);
                                    } else if (isAudioFile(key)) {
                                        publishProgress(key);
                                        String localAbsolutePath = createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]);
                                        LocalSave.saveMedia(localAbsolutePath, s3Client.getObject(bucketName, key));
                                        Log.d(bucketName, "Class DownloadService: Method downloadExerciseChapterParts(): Audio file -> " + key + " saved to " + localAbsolutePath);
                                        exerciseChapter.addExerciseChapterPartAudio(removeExtension(key), localAbsolutePath);
                                    } else if (isImg(key)) {
                                        publishProgress(key);
                                        String localAbsolutePath = createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]);
                                        LocalSave.saveMedia(localAbsolutePath, s3Client.getObject(bucketName, key));
                                        Log.d(bucketName, "Class DownloadService: Method downloadExerciseChapterParts(): Audio file -> " + key + " saved to " + localAbsolutePath);
                                        exerciseChapter.addExerciseChapterPartImg(removeExtension(key), localAbsolutePath);
                                    } else {
                                        Log.d(bucketName, "Class DownloadService: Method downloadExerciseChapterParts(): Unknown key -> " + key);
                                    }
                                }
                            }
                        }
                        //Overwrite ExerciseChapterPartNames
                        if (texts != null && texts.size() != 0) {
                            exerciseChapter.overwriteExerciseChapterPartsText(texts);
                            texts = null;
                        }
                    }
                }
            }
        }
    }
}