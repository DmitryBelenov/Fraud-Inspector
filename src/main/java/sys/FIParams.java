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
    public static final int CPU_USAGE_ACTIVITY_REPORT_FREQ_MIN = Integer.parseInt(System.getProperty("fi.sys.report_cpu_usage.activity_freq", "5"));
    public static final int CPU_USAGE_TOTAL_REPORT_FREQ_MIN = Integer.parseInt(System.getProperty("fi.sys.report_cpu_usage.ttl_freq", "20"));
    public static final String NOT_AVAILABLE = "N/A";
    public static final int TTL_WEIGHT_LIMIT = Integer.parseInt(System.getProperty("fi.sys.ttl_weight_high", "100"));
    public static final int LOWER_TTL_WEIGHT_LIMIT = Integer.parseInt(System.getProperty("fi.sys.ttl_weight_low", "-100"));

    /**
     * Kafka
     * */
    public static String kafkaBrokerHost = System.getProperty("fi.sys.kafka.broker", "localhost:9092");
    public static String statTopicName = "statistics";
    public static String paymentsTopicName = "transactions";
}
