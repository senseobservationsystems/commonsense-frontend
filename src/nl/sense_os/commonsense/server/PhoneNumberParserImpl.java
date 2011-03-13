/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Shaopeng Jia
 */

package nl.sense_os.commonsense.server;

import nl.sense_os.commonsense.client.services.PhoneNumberParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * A servlet that accepts requests that contain strings representing a phone number and a default
 * country, and responds with results from parsing, validating and formatting the number. The
 * default country is a two-letter region code representing the country that we are expecting the
 * number to be from.
 */
public class PhoneNumberParserImpl extends RemoteServiceServlet implements PhoneNumberParser {

    private static final long serialVersionUID = 1L;
    private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public String getOutputForSingleNumber(String phoneNumber, String defaultCountry) {
        try {
            PhoneNumber number = phoneUtil.parseAndKeepRawInput(phoneNumber, defaultCountry);

            boolean isNumberValid = phoneUtil.isValidNumber(number);
            if (isNumberValid) {
                PhoneNumberType type = phoneUtil.getNumberType(number);
                if (type.equals(PhoneNumberType.MOBILE)) {
                    return phoneUtil.format(number, PhoneNumberFormat.INTERNATIONAL);
                } else {
                    return "not mobile";
                }
            } else {
                return "not valid";
            }
        } catch (NumberParseException e) {
            return "not valid";
        }
    }
}
