package li.itcc.flypostr.util;

/**
 * Created by Arthur on 23.08.2016.
 */

public class RequiredStringSize {
    public final int minLen;
    public final int maxLen;

    public RequiredStringSize(int minLen, int maxLen) {
        if (maxLen < minLen) {
            throw new IllegalArgumentException();
        }
        this.minLen = minLen;
        this.maxLen = maxLen;
    }
}
