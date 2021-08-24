package sys;

public class FIParams {
    /**
    * Database
    * */
    public static final String DB_TYPE = System.getProperty("fi.sys.db_type");
    public static final String URL = System.getProperty("fi.sys.db.url");
    public static final String USER = System.getProperty("fi.sys.db.user");
    public static final String PASSWORD = System.getProperty("fi.sys.db.password");

    /**
     * System
     * */
    public static final String httpEncoding = "UTF-8";
    public static final String systemIdentifier = "PC";
    public static final int gwTopCPUUsagesCount = Integer.parseInt(System.getProperty("fi.sys.report_top.cpu.usages", "10"));

    /**
     * Kafka
     * */
    public static String kafkaBrokerHost = System.getProperty("fi.sys.kafka.broker", "localhost:9092");
    public static String statTopicName = "statistics";
    public static String paymentsTopicName = "transactions";
}
