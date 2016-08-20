package li.itcc.flypostr.postingAdd;

import android.app.Activity;
import android.view.View;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PostingAddOnClickListener implements View.OnClickListener {
    private final Activity fParent;

    public PostingAddOnClickListener(Activity parent) {
        fParent = parent;
    }

    @Override
    public void onClick(View v) {
        PostingAddActivity.start(fParent);
    }
}
