package li.itcc.flypostr.util.loading;

import android.os.AsyncTask;

/**
 * Created by Arthur on 21.09.2015.
 * Implements progress and exception handling
 */
public abstract class GenericTask<PARAMS, RESULT> extends AsyncTask<PARAMS, Integer, RESULT>  implements TaskProgressListener {
    private final TaskProgressListener fProgressListener;
    private final TaskAbortListener fThrowableListener;
    private final TaskCompletionListener<RESULT> fResultListener;
    private Throwable fThrowable;

    public GenericTask(TaskExecutionListener<RESULT> executionListener) {
        this(executionListener, executionListener, executionListener);
    }

    public GenericTask(TaskProgressListener progressListener, TaskAbortListener throwableListener, TaskCompletionListener<RESULT> resultListener) {
        fProgressListener = progressListener;
        fThrowableListener = throwableListener;
        fResultListener = resultListener;
    }

    @Override
    public void onTaskProgress(int percentage) {
        // called in dedicated thread
        publishProgress(percentage);
    }

    @Override
    protected final RESULT doInBackground(PARAMS... params) {
        try {
            return doInBackgroundOrThrow(params);
        }
        catch (Throwable th) {
            fThrowable = th;
        }
        return null;
    }

    @Override
    protected final void onPostExecute(RESULT result) {
        super.onPostExecute(result);
        if (fThrowable != null) {
            if (fThrowableListener != null) {
                fThrowableListener.onTaskAborted(fThrowable);
            }
            else {
                fThrowable.printStackTrace();
            }
        }
        else {
            if (fResultListener != null) {
                fResultListener.onTaskCompleted(result);
            }
        }
    }

    protected abstract RESULT doInBackgroundOrThrow(PARAMS... params) throws Exception;

    @Override
    protected void onProgressUpdate(Integer... values) {
        // called in UI Thread
        super.onProgressUpdate(values);
        if (fProgressListener != null) {
            fProgressListener.onTaskProgress(values[0]);
        }
    }
}
