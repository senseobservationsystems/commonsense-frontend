package nl.sense_os.commonsense.main.client;

/**
 * Interface for classes that asynchronously load some data before the application can continue.
 * 
 * @author Steven Mulder <steven@sense-os.nl>
 */
public interface PreLoader {

    public interface Callback {

        void onSuccess();

        void onFailure(int code, Throwable error);
    }

    void load(Callback callback);
}
