/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package li.itcc.flypostr.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import li.itcc.flypostr.R;

/**
 * Simple list-based Activity to redirect to one of the other Activities. This Activity does not
 * contain any useful code related to Firebase Authentication. You may want to start with
 * one of the following Files:
 *     {@link GoogleSignInActivity}
 *     {@link FacebookLoginActivity}
 *     {@link TwitterLoginActivity}
 *     {@link EmailPasswordActivity}
 */
public class ChooserActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener {

    private static final String TAG = "Chooser";
    private static final boolean START_MAIN_ACTIVITY = true;

    private static final Class[] CLASSES = new Class[]{
            GoogleSignInActivity.class,
            FacebookLoginActivity.class,
            TwitterLoginActivity.class,
            EmailPasswordActivity.class,
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_google_sign_in,
            R.string.desc_facebook_login,
            R.string.desc_twitter_login,
            R.string.desc_emailpassword,
    };

    private FirebaseAuth mAuth;

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, ChooserActivity.class);
        return in;
    }

    public static void start(Activity parent, int requestCode) {
        Intent i = createIntent(parent);
        parent.startActivityForResult(i, requestCode);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            AuthUtil.finishWithUser(this, currentUser);
            return;
        }
        setContentView(R.layout.activity_chooser);

        // Set up ListView and Adapter
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setBackgroundResource(R.drawable.custom);

        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked = CLASSES[position];
        startActivityForResult(new Intent(this, clicked), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AuthUtil.AUTHENTICATION_OK) {
            setResult(resultCode, data);
            finish();
        }
    }

    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;
        private String[] mClassesname = {"Google Login", "Facebook login", "Twitter login", "Email login"};

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(mClassesname[position]);
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);

            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }
}
