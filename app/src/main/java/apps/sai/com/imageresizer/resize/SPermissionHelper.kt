package apps.sai.com.imageresizer.resize

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class SPermissionHelper {

    companion object {
        private val TAG = SPermissionHelper::class.java.simpleName

        // requested permissions
        const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val REQUEST_PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
        const val REQUEST_PERMISSION_CAMERA = Manifest.permission.CAMERA
        const val REQUEST_PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS

        /**
         * method to check whether the permissions has been granted to the app
         * @param context the context_menu of the requesting component
         * @param permissions list of permissions requested
         * @return boolean whether app has permission or not
         */

        @JvmStatic fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            for (permission in permissions) {
                context?.let {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }

        /**
         * method to request permissions
         * @param activity the activity requesting permissions
         * @param permission list of the requested permissions
         * @param requestCode int request code
         */
        @JvmStatic  fun requestPermissions(activity: Activity, permission: Array<String>, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, permission, requestCode)
        }
    }
}