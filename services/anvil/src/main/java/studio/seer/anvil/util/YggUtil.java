package studio.seer.anvil.util;

import java.util.Base64;

public final class YggUtil {

    private YggUtil() {}

    public static String basicAuth(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }

    public static String escape(String s) {
        return s == null ? "" : s.replace("'", "\\'");
    }
}
