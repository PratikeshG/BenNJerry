package urbanspace;

public class Constants {
    public static final String DEPLOYMENT = "deployment";
    public static final String NAME = "name";
    public static final String ACTIVE = "active";
    public static final String TIMEZONE = "timeZone";
    public static final String RANGE = "dateRange";
    public static final String OFFSET = "dateOffset";
    public static final String EMAILS = "emails";

    public static final String ACTIVE_DEPLOYMENTS_QUERY = "SELECT deployment, name, active, timeZone, dateRange, dateOffset, emails FROM urbanspace_deployments WHERE active = 1";
}
