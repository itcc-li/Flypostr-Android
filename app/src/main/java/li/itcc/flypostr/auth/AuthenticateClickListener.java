package li.itcc.flypostr.auth;

import android.app.Activity;
import android.view.View;

/**
 * Created by Arthur on 12.09.2015.
 */
public class AuthenticateClickListener implements View.OnClickListener {
    private final Activity fParent;
    private final int requestCode;


    public AuthenticateClickListener(Activity parent, int requestCode) {
        fParent = parent;
        this.requestCode = requestCode;
    }

    @Override
    public void onClick(View v) {
        ChooserActivity.start(fParent, this.requestCode);
    }
}
