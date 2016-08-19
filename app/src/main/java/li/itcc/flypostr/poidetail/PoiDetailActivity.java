package li.itcc.flypostr.poidetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import li.itcc.flypostr.R;
import li.itcc.flypostr.backend.PoiOverviewBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailActivity extends AppCompatActivity {
    private static final String KEY_ID = "KEY_ID";
    private TextView fName;
    private RatingBar fRating;
    private ImageView fImage;
    private ProgressBar fProgressBar;
    private TextView fDescription;

    public static void start(Activity parent, String poiId) {
        Intent i = new Intent(parent, PoiDetailActivity.class);
        i.putExtra(KEY_ID, poiId);
        parent.startActivityForResult(i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_activity);

        fName = (TextView)findViewById(R.id.txv_name);
        fDescription = (TextView)findViewById(R.id.txv_description);
        fRating = (RatingBar)findViewById(R.id.rbr_rating);
        fRating.setIsIndicator(true);
        fImage = (ImageView)findViewById(R.id.img_image);
        fProgressBar = (ProgressBar)findViewById(R.id.prb_progress);
        fProgressBar.setIndeterminate(true);
        fProgressBar.setVisibility(View.VISIBLE);
        String id = getIntent().getExtras().getString(KEY_ID); // todo
    }


}
