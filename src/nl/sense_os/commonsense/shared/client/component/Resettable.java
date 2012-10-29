package nl.sense_os.commonsense.shared.client.component;

/**
 * Interface for views that can be reset to default if necessary.
 */
public interface Resettable {

    /**
     * Resets all view settings to default and removes all user-specific data.
     */
    void reset();
}
