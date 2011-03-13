package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PhoneNumberParserAsync {

    void getOutputForSingleNumber(String phoneNumber, String defaultCountry,
            AsyncCallback<String> callback);
}
