package li.itcc.flypostr.commentAdd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.R;
import li.itcc.flypostr.auth.UserData;
import li.itcc.flypostr.model.CommentWrapper;

/**
 * Created by Arthur on 12.09.2015.
 */
public class CommentAddActivity extends AppCompatActivity {
    private View fCancelButton;
    private View fSaveButton;
    private EditText comment;
    private UserData userData;
    private String postingId;

    public static void start(Activity parent, Intent params) {
        params.setClass(parent, CommentAddActivity.class);
        parent.startActivityForResult(params, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userData = (UserData)getIntent().getExtras().get(PoiConstants.INTENT_KEY_USER_DATA);
        this.postingId = (String)getIntent().getExtras().get(PoiConstants.INTENT_KEY_POSTING_ID);
        if (this.userData == null || this.postingId == null) {
            throw new NullPointerException();
        }
        setContentView(R.layout.comment_add_activity);
        fCancelButton = findViewById(R.id.btn_cancel);
        fCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });
        fSaveButton = findViewById(R.id.btn_save);
        fSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
        comment = (EditText) findViewById(R.id.etx_comment);
    }

    private void onSaveClick(View v) {
        // validate input
        String commentStr = comment.getText().toString().trim();
        if (commentStr.length() < 5) {
            Toast.makeText(this, R.string.txt_comment_too_short, Toast.LENGTH_LONG).show();
            return;
        }
        // validation is o.k., create new bean
        CommentWrapper detail = new CommentWrapper();
        detail.setAuthorId(this.userData.userID);
        detail.setText(commentStr);
        detail.setAuthorId(userData.userID);
        detail.setCreatedAt(new Date());
        CommentSaver saver = new CommentSaver(getApplicationContext());
        // TODO: use a listener and finish upon success
        saver.save(this.postingId, detail);
        finish();
    }

    private void onCancelClick(View v) {
        finish();
    }

}
