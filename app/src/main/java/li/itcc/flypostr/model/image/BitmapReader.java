package li.itcc.flypostr.model.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by Arthur on 22.08.2016.
 *
 * Reads a bitmap form local file in a dedicated thread.
 */

public class BitmapReader {
    private final Context context;

    public BitmapReader(Context context) {
        this.context = context;
    }

    public BitmapLoaderStatus load(String imageId, File srcFile, BitmapLoaderCallback callback) {
        LoaderAsyncTask task = new LoaderAsyncTask(imageId, srcFile, callback);
        task.execute((Void)null);
        return task;
    }


    private class LoaderAsyncTask extends AsyncTask<Void, Void, Bitmap> implements BitmapLoaderStatus {
        private final BitmapLoaderCallback callback;
        private final File srcFile;
        private final String imageId;
        private boolean isInProgress = true;
        private Exception exception;

        public LoaderAsyncTask(String imageId, File srcFile, BitmapLoaderCallback callback) {
            this.imageId = imageId;
            this.srcFile = srcFile;
            this.callback = callback;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                Bitmap result = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
                return result;
            }
            catch (Exception x) {
                this.exception = x;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            isInProgress = false;
            if (this.exception != null) {
                this.callback.onError(this.exception);
            }
            else {
                this.callback.onBitmapLoaded(this.imageId, bitmap);
            }
        }

        @Override
        public boolean isInProgress() {
            return this.isInProgress;
        }

        @Override
        public boolean cancel() {
            return false;
        }
    }
}
