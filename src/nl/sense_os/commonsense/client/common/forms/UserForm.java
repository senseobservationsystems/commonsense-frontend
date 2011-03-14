package nl.sense_os.commonsense.client.common.forms;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.KeyCodes;

public class UserForm extends FormPanel {

    private class PhoneValidator implements Validator {

        @Override
        public String validate(Field<?> field, String value) {
            String validated = validatePhoneNumber(value, "NL");
            if (validated.equals("not mobile")) {
                return "not a mobile phone number";
            } else if (validated.equals("not valid")) {
                return "invalid phone number";
            } else if (validated.equals("error")) {
                return "invalid phone number";
            } else {
                mobile.setRawValue(validated);
                return null;
            }
        }
    }

    private static final String TAG = "UserForm";
    private static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[a-zA-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum)";

    private static native final String validatePhoneNumber(String phoneNumber, String regionCode) /*-{
		try {
			var phoneUtil = $wnd.i18n.phonenumbers.PhoneNumberUtil
					.getInstance();
			var number = phoneUtil
					.parseAndKeepRawInput(phoneNumber, regionCode);
			var isValid = phoneUtil.isValidNumber(number);
			if (isValid) {
				var phoneType = phoneUtil.getNumberType(number);
				var PNT = $wnd.i18n.phonenumbers.PhoneNumberType;
				if (phoneType == PNT.MOBILE) {
					var PNF = $wnd.i18n.phonenumbers.PhoneNumberFormat;
					return phoneUtil.format(number, PNF.E164);
				} else {
					return "not mobile";
				}
			} else {
				return "not valid";
			}
		} catch (e) {
			return "error";
		}
    }-*/;

    private String phoneValidatorMessage;
    private TextField<String> username;
    private TextField<String> password;
    private TextField<String> name;
    private TextField<String> surname;
    private TextField<String> email;
    private TextField<String> mobile;
    private Button submit;

    public UserForm() {
        super();

        this.setLabelSeparator("");
        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.setScrollMode(Scroll.AUTOY);

        initFields();
        initButtons();
    }

    public String getEmail() {
        return this.email.getValue();
    }

    public String getMobile() {
        return this.mobile.getValue();
    }

    public String getName() {
        return this.name.getValue();
    }

    public String getPassword() {
        return this.password.getValue();
    }

    public String getSurname() {
        return this.surname.getValue();
    }

    public String getUsername() {
        return this.username.getValue();
    }

    private void initButtons() {

        SelectionListener<ButtonEvent> l = new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
                final Button b = be.getButton();
                if (b.equals(submit)) {
                    submit();
                } else {
                    Log.w(TAG, "Unexpected button pressed!");
                }
            }
        };

        // submit button
        this.submit = new Button("Submit", IconHelper.create(Constants.ICON_BUTTON_GO), l);
        this.submit.setType("submit");

        this.setButtonAlign(HorizontalAlignment.CENTER);
        this.addButton(submit);

        final FormButtonBinding binding = new FormButtonBinding(this);
        binding.addButton(submit);

        setupSubmit();
    }

    private void initFields() {

        final FormData formData = new FormData("-10");

        // username field
        this.username = new TextField<String>();
        this.username.setFieldLabel("Username:");
        this.username.setAllowBlank(false);

        // password field
        this.password = new TextField<String>();
        this.password.setFieldLabel("Password:");
        this.password.setMinLength(4);
        this.password.setPassword(true);

        // name field
        this.name = new TextField<String>();
        this.name.setFieldLabel("First name:");
        this.name.setAllowBlank(false);

        // surname field
        this.surname = new TextField<String>();
        this.surname.setFieldLabel("Surname:");
        this.surname.setAllowBlank(false);

        // email field
        this.email = new TextField<String>();
        this.email.setFieldLabel("Email:");
        this.email.setRegex(EMAIL_REGEX);
        this.email.getMessages().setRegexText("Invalid email address");

        // mobile field
        this.mobile = new TextField<String>();
        this.mobile.setFieldLabel("Mobile:");
        this.mobile.setAllowBlank(false);
        this.mobile.setValidator(new PhoneValidator());

        this.add(this.username, formData);
        this.add(this.password, formData);
        this.add(this.password, formData);
        this.add(this.name, formData);
        this.add(this.surname, formData);
        this.add(this.email, formData);
        this.add(this.mobile, formData);
    }

    public void setBusy(boolean busy) {
        if (busy) {
            this.submit.setIcon(IconHelper.create(Constants.ICON_LOADING));
        } else {
            this.submit.setIcon(IconHelper.create(Constants.ICON_BUTTON_GO));
        }
    }

    public void setEmail(String email) {
        if (null != email) {
            this.email.setValue(email);
        } else {
            this.email.clear();
        }
    }

    public void setMobile(String mobile) {
        if (null != mobile) {
            this.mobile.setValue(mobile);
        } else {
            this.mobile.clear();
        }
    }

    public void setName(String name) {
        if (null != name) {
            this.name.setValue(name);
        } else {
            this.name.clear();
        }
    }

    public void setPassword(String password) {
        if (null != password) {
            this.password.setValue(password);
        } else {
            this.password.clear();
        }
    }

    public void setSurname(String surname) {
        if (null != surname) {
            this.surname.setValue(surname);
        } else {
            this.surname.clear();
        }
    }

    /**
     * Defines how to submit the form, and the actions to take when the form is submitted.
     */
    private void setupSubmit() {

        // ENTER-key listener to submit the form using the keyboard
        final KeyListener submitListener = new KeyListener() {
            @Override
            public void componentKeyDown(ComponentEvent event) {
                if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (isValid()) {
                        submit();
                    }
                }
            }
        };
        this.mobile.addKeyListener(submitListener);

        // form action is not a regular URL, but we listen for the submit event instead
        this.setAction("javascript:;");
    }

    public void setUsername(String username) {
        if (null != username) {
            this.username.setValue(username);
        } else {
            this.username.clear();
        }
    }
}
