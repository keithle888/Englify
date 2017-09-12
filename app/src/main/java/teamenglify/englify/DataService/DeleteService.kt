package teamenglify.englify.DataService

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.s3.transfermanager.Download
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import teamenglify.englify.MainActivity
import teamenglify.englify.Model.Grade
import teamenglify.englify.Model.Lesson
import teamenglify.englify.Model.RootListing
import teamenglify.englify.R

/**
 * Created by keith on 02-Sep-17.
 */
object DeleteService {
    private val TAG = javaClass.simpleName
    private val isProgressDialogCancellable = false

    fun deleteGrade(context: Context, gradeName: String): Boolean {
        try {
            //Open progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle(R.string.Deletion_Prompt_Title)
            progressDialog.isIndeterminate = false
            progressDialog.setCancelable(isProgressDialogCancellable)
            progressDialog.max = context.fileList().size
            progressDialog.show()

            //Delete all media files associated with the grade.
            Log.d(TAG, "Starting deletion process for grade: $gradeName")
            for (file in context.fileList()) {
                //Increment progress dialog.
                progressDialog.progress = progressDialog.progress + 1
                //Check if file should be deleted.
                val delimited_key = file.split(DownloadService.mediaFileDelimiter)
                if (delimited_key.isNotEmpty()) {
                    val gradeID = delimited_key[0]
                    if (gradeID == gradeName) {
                        Log.d(TAG, "Deleting file: $file")
                        context.deleteFile(file)
                    }
                }
            }

            //Get root listing and replace the grade inside.
            Log.d(TAG,"Replacing grade object in root listing.")
            val rootListing = LocalSave.loadObject(R.string.S3_Object_Listing) as RootListing
            val oldGrade = rootListing.findGrade(gradeName)
            rootListing.overrideGrade(Grade(gradeName,oldGrade.lastModified))
            LocalSave.saveObject(R.string.S3_Object_Listing, rootListing)

            //Close progres dialog
            progressDialog.dismiss()
        } catch (e: Exception) {
            Log.e(TAG,"Failed to delete grade: $gradeName")
            e.printStackTrace()
            return false
        }

        //end the process
        return true
    }

    fun deleteLesson(context: Context, gradeName: String, lessonName: String): Boolean {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(R.string.Deletion_Prompt_Title)
        progressDialog.isIndeterminate = false
        progressDialog.setCancelable(isProgressDialogCancellable)
        progressDialog.max = context.fileList().size
        progressDialog.show()

        //Insert code here.
        Log.d(TAG,"Removing media files for lesson $lessonName.")
        for (file in context.fileList()) {
            if (file.split(DownloadService.mediaFileDelimiter).size > 1 && file.split(DownloadService.mediaFileDelimiter)[1].equals(lessonName)) {
                Log.d(TAG,"Deleting file: $file")
                context.deleteFile(file)
            }
        }

        //Get rootlisting and override the old lesson
        Log.d(TAG,"Replacing grade object in root listing.")
        val rootListing = LocalSave.loadObject(R.string.S3_Object_Listing) as RootListing
        val oldLesson = rootListing.findGrade(gradeName).findLesson(lessonName)
        rootListing.findGrade(gradeName).overrideLesson(Lesson(lessonName, oldLesson.description))
        LocalSave.saveObject(R.string.S3_Object_Listing, rootListing)

        //Close progres dialog
        progressDialog.dismiss()

        //End the process
        return true
    }

    fun deleteRootListing(context: Context): Boolean {
        val progressDialog = ProgressDialog(context)
        Single.create({ emitter: SingleEmitter<Unit> ->
            //Open progress dialog (On main thread)
            progressDialog.setTitle(R.string.Deletion_Prompt_Title)
            progressDialog.isIndeterminate = false
            progressDialog.setCancelable(isProgressDialogCancellable)
            progressDialog.max = context.fileList().size
            progressDialog.setMessage("Deleting Root Listing.")
            progressDialog.show()
            emitter.onSuccess(Unit)
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe()

        //Delete all media files associated with the grade.
        Log.d(TAG,"Starting deletion process for root listing.")
        for (file in context.fileList()) {
            //Increment progress dialog.
            progressDialog.progress = progressDialog.progress + 1
            //Check if file should be deleted.
            if (file.contains(MainActivity.rootDirectory)) {
                Log.d(TAG,"Deleting file: $file")
                context.deleteFile(file)
            }
        }
        //Re-insert new rootlisting into db
        val newRootListing = RootListing()
        LocalSave.saveObject(R.string.S3_Object_Listing, newRootListing)

        //Close progress dialog
        progressDialog.dismiss()

        //End the process
        return true
    }
}