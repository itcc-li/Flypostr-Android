package li.itcc.flypostr.util.loading;

/**
 * Created by Arthur on 19.09.2015.
 */
public interface TaskCompletionListener<RESULT> {

    void onTaskCompleted(RESULT result);
}
