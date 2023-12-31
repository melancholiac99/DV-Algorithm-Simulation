package config;

import java.util.regex.Pattern;

public class Constant {
    public static final int PING_MSG = 0;
    public static final int PING_REPLY_MSG = 1;
    public static final int PATH_DISTANCE_MSG = 2;
    public static final String DEST_IP_ADDR = "127.0.0.1";
    public static final String SRC_IP_ADDR = "127.0.0.1";
    public static final int MAX_CMD_LEN = 100;
    /** 规定定时ping的间隔是8s*/
    public static final int INTERVAL = 8000;
    public static final Pattern EX_FIELD = Pattern.compile("#");
    public static final Pattern EX_PATHCOST = Pattern.compile("/");

}
