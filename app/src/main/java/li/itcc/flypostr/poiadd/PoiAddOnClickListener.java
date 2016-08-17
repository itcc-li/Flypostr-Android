package li.itcc.flypostr.poiadd;

import android.app.Activity;
import android.view.View;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddOnClickListener implements View.OnClickListener {
    private final Activity fParent;

    public PoiAddOnClickListener(Activity parent) {
        fParent = parent;
    }

    @Override
    public void onClick(View v) {
        PoiAddActivity.start(fParent);
    }
}
