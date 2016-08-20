package li.itcc.flypostr.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Arthur on 17.09.2015.
 */
public class StreamUtil {

    public static final long pumpAllAndClose(InputStream in, OutputStream out) throws IOException {
        return pumpAllAndClose(in, out, true);
    }

    public static final long pumpAllAndClose(InputStream in, OutputStream out, boolean closeOut) throws IOException {
        long totalSize = 0;
        try {
            byte[] buffer = new byte[10000];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
                totalSize += n;
            }
            out.flush();
        }
        finally {
            in.close();
            if (closeOut) {
                out.close();
            }
        }
        return totalSize;
    }

}
