package gew.nodemanager.common.model;


import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
public class ConstantParams {

    public static final String UUID_KEY = "uuid";

    public static final String HEARTBEAT_KEY = "heartBeatKey";

    public static final String REDIS_HASH_PREFIX = "Node";

    public static final String REDIS_KEY_PREFIX_DELIMITER = ":";

    public static final Integer DEFAULT_HEARTBEAT_TTL = 10;

    public static final TimeUnit DEFAULT_HEARTBEAT_TIME_UNIT = TimeUnit.MINUTES;

    public static final Integer DEFAULT_PAGE_SIZE = 20;

    public static final String DEFAULT_JSON_TYPE = "application/json;charset=UTF-8";

    public static final DateTimeFormatter INTEGER_DT_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final String PATTERN_TOPIC_SYMBOL = "*";

    public static final char[] INVALID_TOPIC_SYMBOLS = new char[]{'~', '·' , '!', '#', '$', '￥', '\"',
            '{', '}', '^', '.', '”', '`', '\\', '\n', '\t', '\r'};


    public static final String GET_AND_DELETE = "local lockValue = redis.call('get', KEYS[1]);\n" +
            "if (lockValue) then\n"             +
            "    redis.call('del', KEYS[1]);\n" +
            "end\n"                             +
            "return lockValue;";

    public static final String DELETE_IF_EQUAL = "local lockValue = redis.call('get', KEYS[1]);\n" +
            "if (ARGV[1] == lockValue) then\n"  +
            "    redis.call('del', KEYS[1]);\n" +
            "    return true;\n"                +
            "else\n"                            +
            "    return false;\n"               +
            "end";


}
