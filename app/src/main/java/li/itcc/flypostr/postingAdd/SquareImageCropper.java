package li.itcc.flypostr.postingAdd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;

/**
 * Created by Arthur on 15.09.2015.
 */
public class SquareImageCropper {
    private final Context fContext;
    private final File fDestFile;
    private final int fDestSize;

    public SquareImageCropper(Context context, File destFile, int destSize) {
        fContext = context;
        fDestFile = destFile;
        fDestSize = destSize;
    }

    public void crop(File srcFile) throws IOException {
        FileOutputStream out = null;
        try {
            String filePath = srcFile.getAbsolutePath();
            ExifInterface ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotateAngle = getRotateAngle(orientation);
            Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
            Bitmap normalizedBitmap;
            if (rotateAngle == 0) {
                normalizedBitmap = bitmap;
            }
            else {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotateAngle);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                normalizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }
            Rect cropRect = getCropRect(normalizedBitmap.getWidth(), normalizedBitmap.getHeight());
            Bitmap clipped = Bitmap.createBitmap(normalizedBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
            Bitmap scaled = Bitmap.createScaledBitmap(clipped, fDestSize, fDestSize, true);
            out = new FileOutputStream(fDestFile);
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private int getRotateAngle(int orientation) {
        if (orientation == ExifInterface.ORIENTATION_NORMAL) {
            return 0;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


    private Rect getCropRect(int width, int height) {
        Rect result = new Rect();
        if (width == height) {
            result.left = 0;
            result.top = 0;
            result.right = width;
            result.bottom = height;
        }
        else if (width < height) {
            result.left = 0;
            result.top = (height - width) / 2;
            result.right = width;
            result.bottom = result.top + width;
        }
        else {
            result.left = (width - height) / 2;
            result.top = 0;
            result.right = result.left + height;
            result.bottom = height;
        }
        return result;
    }


}
