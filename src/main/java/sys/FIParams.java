package sys;

public class FIParams {
    public static final String DB_TYPE = System.getProperty("fi.sys.db_type");

    public static final String URL = System.getProperty("fi.sys.db.url");
    public static final String USER = System.getProperty("fi.sys.db.user");
    public static final String PASSWORD = System.getProperty("fi.sys.db.password");

    public static final String httpEncoding = "UTF-8";

    public static String statTopicName = "statistics";
    public static String paymentsTopicName = "transactions";
}
