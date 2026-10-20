package com.velora.smp.util;

import java.util.ArrayList;
import java.util.List;

public final class CommandUtil {
    private CommandUtil() {}

    public static List<String> completePrefix(String prefix, List<String> values) {
        String lower = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(lower)) {
                out.add(value);
            }
        }
        return out;
    }
}
