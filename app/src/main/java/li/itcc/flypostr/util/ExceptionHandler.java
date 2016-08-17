package li.itcc.flypostr.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import li.itcc.flypostr.util.loading.TaskAbortListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class ExceptionHandler implements TaskAbortListener {
    private final Activity fActivity;

    public ExceptionHandler(Activity parent) {
        fActivity = parent;
    }

    @Override
    public void onTaskAborted(Throwable th) {
        if (fActivity == null || fActivity.isFinishing()) {
            // context is lost
            return;
        }
        Log.e("ExceptionHandler", "Exception", th);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        String stack = sw.toString();
        String text = th.getClass().getName() + " " + th.getMessage() + "\n" + stack;
        AlertDialog alertDialog = new AlertDialog.Builder(fActivity).create();
        alertDialog.setTitle("Fehler");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
