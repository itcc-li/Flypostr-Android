package li.itcc.flypostr.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseUser;

import li.itcc.flypostr.postingAdd.PostingAddActivity;

/**
 * Created by Arthur on 21.08.2016.
 */

public class AuthUtil {
    public static final int REQUEST_CODE_ADD_POSTING = 51;

    public static final int AUTHENTICATION_OK = 5;
    private static String USER_DATA = "USER_DATA";

    public static void finishWithUser(Activity activity, FirebaseUser user) {
        Intent resultData = new Intent();
        UserData userData = new UserData();
        userData.displayName = user.getDisplayName();
        userData.userID = user.getUid();
        resultData.putExtra(USER_DATA, userData);
        activity.setResult(AUTHENTICATION_OK, resultData);
        activity.finish();
    }

    public static void onActivityResult(Activity parent, int requestCode, int resultCode, Intent data) {
        if (resultCode == AUTHENTICATION_OK) {
            UserData userData = (UserData)data.getExtras().get(USER_DATA);
            if (requestCode == REQUEST_CODE_ADD_POSTING) {
                PostingAddActivity.start(parent, userData);
            }
        }

    }
}
