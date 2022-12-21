package org.dataspread.sheetanalyzer.dependency.util;

public class RedisGraphConstants {
    public static String cellAttr = "cell";
    public static String precAttr = "prec";
    public static String depAttr = "dep";
    public static String relName = "ISPREC";
    public static String nodeName = "Cell";
    public static String relFile =  relName + ".csv";
    public static String nodeFile = nodeName + ".csv";
    public static String bulkLoadCmd = "redisgraph-bulk-insert";
}
