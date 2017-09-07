package teamenglify.englify.DataService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.com.google.gson.Gson;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import teamenglify.englify.Listing.ListingFragment;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Exercise;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.ExerciseChapterPart;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.ModuleSelection.ModuleSelection;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;
import static teamenglify.englify.MainActivity.rootDirectory;
import static teamenglify.englify.MainActivity.s3Client;

/**
 * DownloadService class is used to download objects from the AWS S3 server and save them to local memory as the appropriate Java objects.
 * DownloadService extends AsyncTask in order to run networking calls on a separate thread while keeping the UI up-to-date with its status.
 */
public class DownloadService extends AsyncTask<Void, String, Boolean> {
    private static final String TAG = DownloadService.class.getSimpleName();
    public final static int DOWNLOAD_LISTING_OF_GRADES = 0;
    public final static int DOWNLOAD_LISTING_OF_LESSONS = 1;
    public final static int DOWNLOAD_LESSON = 2;
    private int downloadType;
    private Grade grade;
    private Lesson lesson;
    private ProgressDialog pd;
    public LinkedList<String> texts;
    private String baseMessage = "Downloading:";
    static final String mediaFileDelimiter = "_";


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
            try {
                return download_list_of_grades();
            } catch (Exception e) {
                return false;
            }
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
                        .replace(R.id.activity_main_container,
                                ModuleSelection.newInstance(lesson,
                                        mainActivity.getSupportActionBar().getTitle().toString()),
                                "MODULE_LISTING")
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            if (downloadType == DOWNLOAD_LISTING_OF_GRADES) {
                DeleteService.INSTANCE.deleteRootListingRx(mainActivity.getApplicationContext());
            } else if (downloadType == DOWNLOAD_LISTING_OF_LESSONS) {
                mainActivity.clearBackStack();
                mainActivity.loadLoginFragment();
            } else if (downloadType == DOWNLOAD_LESSON) {
                DeleteService.INSTANCE.deleteLessonRx(mainActivity.getApplicationContext(),
                        grade.name,
                        lesson.name);
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
    public boolean download_list_of_grades() throws Exception{
        //download all objects
        try {
            List<S3ObjectSummary> summaries = getSummaries(mainActivity.rootDirectory);
            pd.setMax(summaries.size());
            pd.setProgress(0);
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Downloaded object listing from AWS S3.");
            //identify grades
            HashMap<Integer, Date> identifiedGrades = new HashMap<>(); //the key is the grade name , the value is the URL for the image.
            for (S3ObjectSummary summary : summaries) {
                String key = summary.getKey();
                if (key.split("/").length == 2 && isFolder(key)) { //Ensure that we are looking at a folder
                    String[] dKeys = key.split("/");            //Split key to extract specifically the grade name
                    identifiedGrades.put(Integer.parseInt(dKeys[1]), summary.getLastModified());
                }
                //Update progress in progress dialog
                pd.setProgress(pd.getProgress() + 1);
            }
            //Generate grades based on identified grades
            Set<Integer> listOfGrades = identifiedGrades.keySet();
            Log.d("Englify", "Class DownloadService: Method downloadListing(): Identified grades: " + listOfGrades.toString());
            List<Integer> sortedListOfGrades = new ArrayList(listOfGrades);
            Collections.sort(sortedListOfGrades);
            ArrayList<Grade> grades = new ArrayList<Grade>();
            for (Integer gradeName : sortedListOfGrades) {
                //save each grade into internal storage
                Grade newGrade = new Grade(gradeName.toString(), new ArrayList<Lesson>(), identifiedGrades.get(gradeName));
                Log.d("Englify", "Class DownloadService: Method downloadListing(): Created grade -> " + newGrade.toString());
                grades.add(newGrade);
            }
            //save the grades to internal storage
            LocalSave.saveObject(mainActivity.getString(R.string.S3_Object_Listing), new RootListing(grades));
        } catch (Exception e) {
            e.printStackTrace();
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
            Date lastModified = summary.getLastModified();
            String[] delimited_path = path.split("/");
            if (isFolder(path) && delimited_path.length == 3) {       //only lessons should be a folder at this directory depth
                String lessonName = delimited_path[2];
                if (grade.findLesson(lessonName) == null) {
                    publishProgress(lessonName);
                    Log.d(bucketName, "Class DownloadService: Method download_list_of_lessons(): Creating " + lessonName + ".");
                    grade.lessons.add(new Lesson(lessonName, lastModified));
                    //Update lastModified Date of grade if the lesson is newer.
                    if (grade.lastModified.before(lastModified)) {
                        grade.lastModified = lastModified;
                    }
                }
            } else if (isTextFile(path, "LessonDescription")) {              //find the lesson descriptions and handle after all lessons have been created
                lessonDescriptions = readTextFile(s3Client.getObject(bucketName, path));
                Log.d(bucketName, "Class DownloadService: Method download_list_of_lessons(): Description for Grade " + grade.name + " lessons. -> " + lessonDescriptions.toString());
                //Update lastModified date of Grade if the description is the latest thing to be updated in S3.
                if (grade.lastModified.before(lastModified)) {
                    grade.lastModified = lastModified;
                }
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
        Log.d(TAG, "Starting download of vocabulary.");
        download_lesson_vocab();
        Log.d(TAG, "Starting download of conversation.");
        download_lesson_conversation();
        Log.d(TAG, "Starting download of exercise.");
        download_lesson_exercise();
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
                        this.lesson.updateLastModifiedDate(summary.getLastModified());
                    } else if (isImg(path)) {
                        String vocabPartName = removeExtension(delimited_path[4]);
                        String vocabPartMediaFileName = createMediaFileName(grade.name, lesson.name, vocab.name, delimited_path[4]);
                        publishProgress(vocabPartMediaFileName);
                        LocalSave.saveMedia(vocabPartMediaFileName, s3Client.getObject(bucketName, path));
                        vocab.addVocabPartImg(vocabPartName, vocabPartMediaFileName);
                        Log.d(bucketName, "Class DownloadService: Method download_lesson_vocab(): Img file for " + path + " saved to " + vocabPartMediaFileName);
                        this.lesson.updateLastModifiedDate(summary.getLastModified());
                    } else if (isTextFile(path)) {
                        vocabDescriptions = readTextFile(s3Client.getObject(bucketName, path));
                        publishProgress(path);
                        this.lesson.updateLastModifiedDate(summary.getLastModified());
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
            } else {
                conversation = (Conversation) lesson.findModule("Conversation");
            }
            publishProgress(lesson.name + "/" + "Conversation");
            //Run through summaries
            for (S3ObjectSummary summary : summaries) {
                String path = summary.getKey();
                String[] delimited_path = path.split("/");
                if (delimited_path.length == 5) {
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
                                    Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Text file found for " + key);
                                    this.lesson.updateLastModifiedDate(summary.getLastModified());
                                } else if (isAudioFile(key)) {
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
                                    LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                    publishProgress(key);
                                    read.addReadPartAudio(removeExtension(dKey[5]), createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Audio file for " + key + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    this.lesson.updateLastModifiedDate(summary.getLastModified());
                                } else if (isImg(key)) {
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
                                    LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]), s3Object);
                                    publishProgress(key);
                                    read.addReadPartImg(removeExtension(dKey[5]), createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    Log.d("Englify", "Class DownloadService: Method downloadReadParts(): Image file for " + key + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, conversation.name, read.name, dKey[5]));
                                    this.lesson.updateLastModifiedDate(summary.getLastModified());
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

    public void download_lesson_exercise() {
        List<S3ObjectSummary> summaries = getSummaries(rootDirectory, grade.name, lesson.name, "Exercise");
        //Download exercise chapter
        if (summaries != null && summaries.size() != 0) {
            //Create exercise class
            Exercise exercise = new Exercise("Exercise");
            if (lesson.findModule("Exercise") == null) {
                lesson.addModule(exercise);
            } else {
                exercise = (Exercise) lesson.findModule("Exercise");
            }
            //Check what exercise chapters are available.
            HashSet<String> exerciseChapters = new HashSet<>();
            for (S3ObjectSummary summary: summaries) {
                String[] delimited_key = summary.getKey().split("/");
                if (delimited_key.length == 5) {
                    if (!exerciseChapters.contains(delimited_key[4])) {
                        exerciseChapters.add(delimited_key[4]);
                    }
                }
            }
            //Iterate through the exerciseChapters and see if they have exercise.json available. If not, do not bother creating the execise chapter.
            for (String exerciseChapterName : exerciseChapters) {
                summaries = getSummaries(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapterName);
                Log.d(TAG,"Looking for JSON file within exercise chapter.");
                String jsonFilePath = null;
                for (S3ObjectSummary summary: summaries) {
                    if (isJsonFile(summary.getKey())) {
                        jsonFilePath = summary.getKey();
                        Log.d(TAG,"JSON file found for exercise chapter.");
                    }
                }
                //If Json file is found, create the exerciseChapter and create exerciseChapterParts
                if (jsonFilePath != null) {
                    try {
                        ExerciseChapter exerciseChapter = new ExerciseChapter(exerciseChapterName);
                        //Get JSON Object for exercise chapter
                        S3Object jsonObject = s3Client.getObject(bucketName, jsonFilePath);
                        String json = "";
                        for (String s : readTextFile(jsonObject)) {
                            json += s;
                        }
                        Log.d(TAG, "JSON String retrieved: " + json);
                        ExerciseChapterPart[] exerciseChapterParts = new Gson().fromJson(json, ExerciseChapterPart[].class);
                        exerciseChapter.chapterParts = new ArrayList<>(Arrays.asList(exerciseChapterParts));
                        //Add media files to ExerciseChapterPart
                        for (S3ObjectSummary summary : summaries) {
                            String key = summary.getKey();
                            String[] dKey = summary.getKey().split("/");
                            if (isAudioFile(summary.getKey())) {
                                ExerciseChapterPart exerciseChapterPart = exerciseChapter.findExerciseChapterPart(removeExtension(dKey[5]));
                                if (exerciseChapterPart != null) {
                                    exerciseChapterPart.audioURL = createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]);
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
                                    LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]), s3Object);
                                    publishProgress(key);
                                    Log.d("Englify", "Class DownloadService: Method download_lesson_exercise: Audio file for " + key + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]));
                                }
                            } else if (isImg(summary.getKey())) {
                                ExerciseChapterPart exerciseChapterPart = exerciseChapter.findExerciseChapterPart(removeExtension(dKey[5]));
                                if (exerciseChapterPart != null) {
                                    exerciseChapterPart.imageURL = createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]);
                                    S3Object s3Object = s3Client.getObject(bucketName, key);
                                    LocalSave.saveMedia(createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]), s3Object);
                                    publishProgress(key);
                                    Log.d("Englify", "Class DownloadService: Method download_lesson_exercise: Img file for " + key + " saved to " + createMediaFileName(rootDirectory, grade.name, lesson.name, exercise.name, exerciseChapter.name, dKey[5]));
                                }
                            }
                        }
                        exercise.addChapter(exerciseChapter);
                    } catch (Exception e) {
                        Log.e(TAG,"Error trying to create ExerciseChapterPart: " + exerciseChapterName);
                        e.printStackTrace();
                    }
                }
            }
            exercise.printAllChapters();
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
    public static boolean isFolder(String folderName) {
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
    public static boolean isTextFile(String path) {
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
    public static boolean isTextFile(String path, String fileName) {
        if (path.contains(".txt") && path.toLowerCase().contains(fileName.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAudioFile(String fileName) {
        if (fileName.contains(".mp3")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isImg(String fileName) {
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
            toReturn = toReturn + param + mediaFileDelimiter;
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

    public boolean isJsonFile(String path) {
        if (path.contains(".json")) {
            return true;
        }
        return false;
    }
}