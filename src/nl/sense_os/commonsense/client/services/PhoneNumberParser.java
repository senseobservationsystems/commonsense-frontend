package nl.sense_os.commonsense.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("phonenumbers")
public interface PhoneNumberParser extends RemoteService {

    String getOutputForSingleNumber(String phoneNumber, String defaultCountry);
}