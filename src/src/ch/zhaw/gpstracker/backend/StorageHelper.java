package ch.zhaw.gpstracker.backend;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageHelper {
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public File getSavePath(Context context, String filename) {
	    // Get the directory for the app's private pictures directory. 
	    File file = new File(context.getExternalFilesDir(
	            Environment.DIRECTORY_DOCUMENTS), filename);
	    
	    Log.i("getSavePath", file.toString());
	    
	    return file;
	}

}
