package li.itcc.flypostr.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import li.itcc.flypostr.R;

/**
 * Created by Arthur on 13.09.2015.
 */
public class ThumbnailCache {
    private final Context fContext;
    private HashMap<String, Bitmap> fCache = new HashMap<>();
    private Bitmap fBrokenBitmap;

    public ThumbnailCache(Context context) {
        fContext = context;
    }

    public void clearCache() {
        fCache.clear();
    }

    public void deleteAllThumbnails() {
        // TODO
    }

    public Bitmap getBitmap(String key) {
        Bitmap result = fCache.get(key);
        if (result == null) {
            InputStream in = null;
            try {
                in = fContext.openFileInput(getFileName(key));
                result = BitmapFactory.decodeStream(in);
            }
            catch (Exception x) {
                result = getBrokenBitmap();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (Exception x) {
                        // silent
                    }
                }
            }
            fCache.put(key, result);
        }
        return result;
    }

    private Bitmap getBrokenBitmap() {
        if (fBrokenBitmap == null) {
            fBrokenBitmap = BitmapFactory.decodeResource(fContext.getResources(), R.drawable.default_thump);
        }
        return fBrokenBitmap;
    }

    public void storeBitmap(String key, byte[] raw) throws Exception {
        if (raw == null) {
            fContext.deleteFile(getFileName(key));
        }
        else {
            OutputStream out = fContext.openFileOutput(getFileName(key), Context.MODE_PRIVATE);
            out.write(raw);
            out.flush();
            out.close();
        }
    }

    private String getFileName(String key) {
        return "thumb_" + key;
    }

    public boolean existsBitmap(String uuid) {
        return fContext.getFileStreamPath(getFileName(uuid)).exists();
    }
}
