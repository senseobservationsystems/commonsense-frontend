package nl.sense_os.commonsense.shared.constants;


public class Constants {

    public static final boolean TEST_MODE = false;

    public static final boolean TED_MODE = TEST_MODE && false;

    /**
     * Registry key for the list of all devices for the current user
     */
    public static final String REG_DEVICE_LIST = "DevicesList";
    /**
     * Registry key for the list of all environments for the current user
     */
    public static final String REG_ENVIRONMENT_LIST = "EnvironmentList";

    /**
     * Registry key for the list of groups for the current user
     */
    public static final String REG_GROUPS = "Groups";

    /**
     * Registry key for the list of all sensors for the current user
     */
    public static final String REG_SENSOR_LIST = "SensorsList";

    /**
     * Registry key for the list of services for the current user
     */
    public static final String REG_SERVICES = "Services";

    /**
     * Registry key for the session ID, stored as String
     */
    public static final String REG_SESSION_ID = "SessionId";

    /**
     * Registry key for the current User
     */
    public static final String REG_USER = "User";

    private Constants() {
        // Private constructor to make sure this class is not instantiated.
    }

}
