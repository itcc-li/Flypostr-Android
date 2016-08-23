package li.itcc.flypostr.util;

import android.content.Context;
import android.widget.EditText;

import li.itcc.flypostr.R;

/**
 * Created by Arthur on 23.08.2016.
 */

public class ValidationHelper {
    private final Context fContext;
    private boolean fHasErrors = false;

    public ValidationHelper(Context context) {
        fContext = context;
    }

    public String validateText(EditText editText, RequiredStringSize requirement) {
        int minLen = requirement.minLen;
        int maxLen = requirement.maxLen;
        String text = editText.getText().toString().trim();
        int textLength = text.length();
        int textId = 0;
        if (minLen == 0) {
            if (textLength > maxLen) {
                textId = R.string.txt_input_too_long;
            }
        }
        else {
            if (textLength == 0) {
                textId = R.string.txt_input_missing;
            } else if (textLength < minLen) {
                textId = R.string.txt_input_too_short;
            } else if (textLength > maxLen) {
                textId = R.string.txt_input_too_long;
            }
        }
        if (textId != 0) {
            editText.setError(fContext.getText(textId));
            fHasErrors = true;
        }
        return text;
    }

    public boolean hasErrors() {
        return fHasErrors;
    }
}