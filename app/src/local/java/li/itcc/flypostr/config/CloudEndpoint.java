package li.itcc.flypostr.config;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Arthur on 17.09.2015.
 */
public class CloudEndpoint {
    // see https://cloud.google.com/datastore/docs/tools/devserver
    // maybe use env variable DATASTORE_HOST for that
    //
    // and see also JVM argument -Ddatastore.backing_store="D:\Temp\local_db.bin"

    private static final String DEBUG_SERVER = "192.168.0.132";
    public static final String URL = "http://" + DEBUG_SERVER + ":8080";

    /**
     * For local testing only
     * @param urlStr
     * @return
     */
    public static String dnsHack(String urlStr) {
        String debugServer = System.getProperty("debugServer");
        if (debugServer == null) {
            debugServer = DEBUG_SERVER;
        }
        try {
            URL oldUrl = new URL(urlStr);
            URL newURL = new URL(oldUrl.getProtocol(), debugServer, oldUrl.getPort(), oldUrl.getFile());
            return newURL.toString();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return urlStr;
    }
}
