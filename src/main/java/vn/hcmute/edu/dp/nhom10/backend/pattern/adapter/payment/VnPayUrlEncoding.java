package vn.hcmute.edu.dp.nhom10.backend.pattern.adapter.payment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class VnPayUrlEncoding {

    private VnPayUrlEncoding() {
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
