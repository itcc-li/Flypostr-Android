package li.itcc.flypostr.postingDetail;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.R;
import li.itcc.flypostr.auth.AuthUtil;
import li.itcc.flypostr.auth.AuthenticateClickListener;
import li.itcc.flypostr.model.PostingWrapper;
import li.itcc.flypostr.util.ImageLoader;

import static android.support.customtabs.CustomTabsIntent.KEY_ID;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PostingDetailActivity extends AppCompatActivity implements PostingDetailLoader.PostingDetailLoaderCallback, ImageLoader.ImageLoaderCallback {
    private String id;
    private TextView title;
    private ImageView image;
    private TextView text;
    private ProgressBar progressBar;
    private TextView progressText;

    private PostingDetailLoader loadTask;
    private ImageLoader loadImageTask;
    private View button;

    public static void start(Activity parent, String poiId) {
        Intent i = new Intent(parent, PostingDetailActivity.class);
        i.putExtra(KEY_ID, poiId);
        parent.startActivityForResult(i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posting_detail_activity);
        id = getIntent().getExtras().getString(KEY_ID);

        if (getIntent().getExtras() == null) {
            onError(new IllegalArgumentException("Marker not found."));

            // close activity if extras not available
            finish();
            return;
        }

        loadImageTask = new ImageLoader(this, ImageLoader.ImageCacheType.IMAGES);
        loadTask = new PostingDetailLoader(this);

        title = (TextView)findViewById(R.id.txv_title);
        text = (TextView)findViewById(R.id.txv_text);
        image = (ImageView)findViewById(R.id.img_image);
        progressBar = (ProgressBar)findViewById(R.id.prg_progressLoading);
        progressText = (TextView)findViewById(R.id.txv_progressText);
        this.button = findViewById(R.id.button);
        Intent commentIntent = new Intent();
        commentIntent.putExtra(PoiConstants.INTENT_KEY_POSTING_ID, id);
        this.button.setOnClickListener(new AuthenticateClickListener(this, AuthUtil.REQUEST_CODE_ADD_COMMENT, commentIntent));
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
        if (loadImageTask != null) {
            loadImageTask.detach();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AuthUtil.onActivityResult(this, requestCode, resultCode, data);
    }


    private void loadDetail(String id) {
        // enable ui elements
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        loadTask.load(id, this);
    }

    private void loadImage(String filename) {
        loadImageTask.startProgress(filename, this);
    }

    @Override
    public void onPostingChanged(PostingWrapper posting) {
        title.setText(posting.getTitle());
        text.setText(posting.getText());

        loadImage(posting.getImageId());
    }

    @Override
    public void onPostingDeleted(String id) {
        finish();
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageLoaded(String filename, Bitmap bitmap) {
        image.setImageBitmap(bitmap);

        // disable ui elements
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        progressText.setText("");
        progressBar.setProgress(0);
    }

    @Override
    public void onUpdateProgressDownload(String filename, long bytesReceived, long totalByteCount) {
        Log.i("FlyPostr", "Bytes received: " + bytesReceived + ", Bytes to Transfare: " + totalByteCount);

        if (totalByteCount <= 0) {
            progressText.setText("");
            progressBar.setProgress(0);
            return;
        }

        float progress = ((bytesReceived / (float)totalByteCount) * 10.0f);
        progressBar.setProgress((int)progress);
        try {
            progressText.setText(String.format(getResources().getConfiguration().locale, "%1$.0f%%", progress)); // Example: 10.215 -> "10%"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
