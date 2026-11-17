package app.cuevision;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class License {

    private static final String SECRET = "CV1x7Qz9Lm2Rt5Yb8Nw3Kd6Pf4Hs0Gj";
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final long EPOCH = 1767225600000L;

    public static final int VALID = 1;
    public static final int EXPIRED = 2;
    public static final int INVALID = 3;

    public static String normalize(String raw) {
        return raw.toUpperCase().replace("-", "").replace(" ", "").trim();
    }

    public static long todayDay() {
        return (System.currentTimeMillis() - EPOCH) / TimeUnit.DAYS.toMillis(1);
    }

    private static long decode(String s) {
        long v = 0;
        for (char c : s.toCharArray()) {
            int i = ALPHABET.indexOf(c);
            if (i < 0) return -1;
            v = (v << 5) | i;
        }
        return v;
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) sb.append(ALPHABET.charAt((b & 0xFF) % 32));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static int validate(String raw) {
        String k = normalize(raw);
        if (!k.startsWith("CV")) return INVALID;
        String body = k.substring(2);
        if (body.length() != 14) return INVALID;
        String payload = body.substring(0, 4);
        if (!sign(payload).substring(0, 10).equals(body.substring(4))) return INVALID;
        long day = decode(payload);
        if (day < 0) return INVALID;
        return day < todayDay() ? EXPIRED : VALID;
    }

    public static long remainingDays(String raw) {
        String k = normalize(raw);
        if (k.length() != 16) return 0;
        long day = decode(k.substring(2, 6));
        return Math.max(0, day - todayDay());
    }
}