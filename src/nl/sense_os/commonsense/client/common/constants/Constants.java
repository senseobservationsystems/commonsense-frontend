package nl.sense_os.commonsense.client.common.constants;

import com.google.gwt.core.client.GWT;

public class Constants {

    /**
     * true if the app is deployed to common-sense-test.appspot.com.
     */
    public static final boolean TEST_MODE = GWT.getModuleBaseURL().contains("common-sense-test");

    /**
     * true if the app is deployed to commonsense-test.appspot.com.
     */
    public static final boolean TED_MODE = GWT.getModuleBaseURL().contains("commonsense-test");

    /**
     * true if the app is deployed to common-sense-dev.appspot.com.
     */
    public static final boolean DEV_MODE = GWT.getModuleBaseURL()
            .contains("common.dev.sense-os.nl");

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
