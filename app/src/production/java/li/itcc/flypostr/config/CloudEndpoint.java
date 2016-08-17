package li.itcc.flypostr.config;

/**
 * Created by Arthur on 17.09.2015.
 */
public class CloudEndpoint {
    public static final String URL = "https://flypostr.appspot.com";

    public static String dnsHack(String url) {
        // no hack in release mode
        return url;
    }
}
