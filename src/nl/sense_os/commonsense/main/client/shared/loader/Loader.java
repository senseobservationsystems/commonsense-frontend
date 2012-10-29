package nl.sense_os.commonsense.main.client.shared.loader;

/**
 * Interface for classes that asynchronously load some data before the application can continue.
 * 
 * @author Steven Mulder <steven@sense-os.nl>
 */
public interface Loader {

    public interface Callback {

        void onSuccess(Object result);

        void onFailure(int code, Throwable error);
    }

    void load(Callback callback);
}
