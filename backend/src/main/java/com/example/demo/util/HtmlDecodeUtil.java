package com.example.demo.util;

import org.springframework.web.util.HtmlUtils;

public class HtmlDecodeUtil {
    private HtmlDecodeUtil() {}

    public static String decode(String input) {
        return input == null ? null : HtmlUtils.htmlUnescape(input);
    }
}
