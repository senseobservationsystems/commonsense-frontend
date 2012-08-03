package nl.sense_os.commonsense.common.client.component;

/**
 * Interface for views that can be reset to default if necessary.
 */
public interface Resettable {

    /**
     * Resets all view settings to default and removes all user-specific data.
     */
    void reset();
}
