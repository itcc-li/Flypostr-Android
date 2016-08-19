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
public class PoiDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<PoiDetailBean> {
    private static final String KEY_ID = "KEY_ID";
    private TextView fName;
    private EditText fComment;
    private RatingBar fRating;
    private Location fLocation;
    private ImageView fImage;
    private ProgressBar fProgressBar;
    private TextView fDescription;
    private Loader<PoiDetailBean> fLoader;

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
        String id = getIntent().getExtras().getString(KEY_ID);
        getSupportLoaderManager().initLoader(0, PoiDetailLoader.createArgs(id), this);
    }

    @Override
    public Loader<PoiDetailBean> onCreateLoader(int id, Bundle args) {
        return new PoiDetailLoader(this, id, args);
    }

    @Override
    public void onLoadFinished(Loader<PoiDetailBean> loader, PoiDetailBean data) {
        fProgressBar.setVisibility(View.GONE);
        if (data == null) {

        }
        else {
            if (data.getImageUrl() != null) {
                // the image has been downloaded
                ImageStore store = new ImageStore(this);
                ImageStore.Key key = store.createKey(data.getOverview().getUuid(), data.getImageUpdateTime());
                Bitmap bmp = BitmapFactory.decodeFile(store.getImageFilePath(key));
                fImage.setImageBitmap(bmp);
            } else {
                fImage.setImageDrawable(null);
            }
            PoiOverviewBean overviewData = data.getOverview();
            fRating.setRating(overviewData.getRating());
            fName.setText(overviewData.getName());
            fDescription.setText(data.getDescription());
        }
    }

    @Override
    public void onLoaderReset(Loader<PoiDetailBean> loader) {

    }
}
