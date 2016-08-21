package li.itcc.flypostr.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseUser;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.commentAdd.CommentAddActivity;
import li.itcc.flypostr.postingAdd.PostingAddActivity;

/**
 * Created by Arthur on 21.08.2016.
 */

public class AuthUtil {
    public static final int REQUEST_CODE_ADD_POSTING = 51;
    public static final int REQUEST_CODE_ADD_COMMENT = 52;

    public static final int AUTHENTICATION_OK = 5;

    public static void finishWithUser(Activity activity, FirebaseUser user) {
        finishWithUser(activity, user, new Intent());
    }

     public static void finishWithUser(Activity activity, FirebaseUser user, Intent params) {
        UserData userData = new UserData();
        userData.displayName = user.getDisplayName();
        userData.userID = user.getUid();
        params.putExtra(PoiConstants.INTENT_KEY_USER_DATA, userData);
        activity.setResult(AUTHENTICATION_OK, params);
        activity.finish();
    }

    public static void onActivityResult(Activity parent, int requestCode, int resultCode, Intent data) {
        if (resultCode == AUTHENTICATION_OK) {
            UserData userData = (UserData)data.getExtras().get(PoiConstants.INTENT_KEY_USER_DATA);
            if (requestCode == REQUEST_CODE_ADD_POSTING) {
                PostingAddActivity.start(parent, userData);
            }
            else if (requestCode == REQUEST_CODE_ADD_COMMENT) {
                CommentAddActivity.start(parent, data);
            }
        }

    }
}
