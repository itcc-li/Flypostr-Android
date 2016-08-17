package li.itcc.flypostr.config;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Arthur on 17.09.2015.
 */
public class CloudEndpoint {
    public static final String URL = "https://flypostr-staging.appspot.com";

    public static String dnsHack(String url) {
        // no hack in release mode
        return url;
    }
}