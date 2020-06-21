package gew.nodemanager.common.util;

import java.util.Random;
import java.util.UUID;

/**
 * @author Jason/GeW
 * @since 2019-03-24
 */
public class UidGenerator {

    public static final String DELIMITER = "-";

    private static final Random RANDOM = new Random();

    private UidGenerator() {
        // Static Class Private Constructor
    }


    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generate(int length) {
        if (length < 1) {
            return "";
        }
        String uuid = generate();
        if (uuid.length() <= length) {
            return uuid;
        } else {
            return uuid.substring(0, length - 1);
        }
    }

    public static String generate(String prefix) {
        return prefix + DELIMITER + generate();
    }

    public static String generate(String prefix, int maxLength) {
        if (maxLength < 1) {
           throw new IllegalArgumentException("Invalid Max Length");
        }
        String uuid = generate(prefix);
        if (uuid.length() <= maxLength) {
            return uuid;
        } else {
            return uuid.substring(0, maxLength - 1);
        }
    }
}
