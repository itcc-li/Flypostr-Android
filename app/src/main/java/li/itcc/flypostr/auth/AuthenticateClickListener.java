package li.itcc.flypostr.auth;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * Created by Arthur on 12.09.2015.
 */
public class AuthenticateClickListener implements View.OnClickListener {
    private final Activity fParent;
    private final int requestCode;
    private final Intent params;


    public AuthenticateClickListener(Activity parent, int requestCode, Intent params) {
        fParent = parent;
        this.requestCode = requestCode;
        this.params = params;
    }

    @Override
    public void onClick(View v) {
        ChooserActivity.start(fParent, this.requestCode, params);
    }
}
