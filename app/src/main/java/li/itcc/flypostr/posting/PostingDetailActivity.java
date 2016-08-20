package li.itcc.flypostr.posting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import li.itcc.flypostr.R;
import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PostingDetailActivity extends AppCompatActivity implements LoadDetailCallbackIfc, LoadDetailImageCallbackIfc {
    private static final String KEY_ID = "KEY_ID";
    private String id;
    private TextView title;
    private ImageView image;
    private TextView text;

    private LoadDetailTask loadTask;
    private LoadDetailImageTask loadImageTask;

    public static void start(Activity parent, String poiId) {
        Intent i = new Intent(parent, PostingDetailActivity.class);
        i.putExtra(KEY_ID, poiId);
        parent.startActivityForResult(i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_detail_activity);

        if (getIntent().getExtras() == null) {
            // close activity if extras not available
            finish();
            return;
        }

        title = (TextView)findViewById(R.id.txv_title);
        text = (TextView)findViewById(R.id.txv_text);
        image = (ImageView)findViewById(R.id.img_image);

        id = getIntent().getExtras().getString(KEY_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDetail(id);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadTask != null) {
            loadTask.detach();
        }
    }

    private void loadDetail(String id) {
        loadTask = new LoadDetailTask(this, this);
        loadTask.load(id);
    }

    @Override
    public void onPostingChanged(PostingWrapper posting) {
        title.setText(posting.getTitle());
        text.setText(posting.getText());

        loadImage(posting.getImageId());
    }

    private void loadImage(String filename) {
        loadImageTask = new LoadDetailImageTask(this, this);
        loadImageTask.loadImage(filename);
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onImageReceived(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }
}
