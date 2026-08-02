package com.gtnewhorizons.wdmla.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Converts between integer ARGB colors and values edited in the config GUI. */
public final class ConfigColorCodec {

    public static final Pattern INPUT_PATTERN = Pattern.compile("(?i)^(?:#|0x)?(?:[0-9a-f]{6}|[0-9a-f]{8})$");

    /** Prevents utility class instantiation. */
    private ConfigColorCodec() {}

    /** Formats ARGB as uppercase hexadecimal, omitting an opaque alpha channel. */
    public static String format(int argb) {
        if ((argb >>> 24) == 0xFF) {
            return String.format(Locale.ROOT, "#%06X", argb & 0xFFFFFF);
        }
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    /** Parses RGB or ARGB text and returns null without changing state when invalid. */
    public static Integer parse(String input) {
        if (input == null || !INPUT_PATTERN.matcher(input).matches()) {
            return null;
        }

        String digits = input;
        if (digits.startsWith("#")) {
            digits = digits.substring(1);
        } else if (digits.regionMatches(true, 0, "0x", 0, 2)) {
            digits = digits.substring(2);
        }

        long value = Long.parseLong(digits, 16);
        if (digits.length() == 6) {
            value |= 0xFF000000L;
        }
        return (int) value;
    }
}
