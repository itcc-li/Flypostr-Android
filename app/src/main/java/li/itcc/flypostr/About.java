package li.itcc.flypostr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by stefa on 21.08.2016.
 */

public class About extends MainActivity {

    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, About.class);
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
}