package nl.sense_os.commonsense.login.client.login.component;

import nl.sense_os.commonsense.login.client.login.LoginView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginForm extends Composite implements LoginView {

	interface LoginFormUiBinder extends UiBinder<Widget, LoginForm> {
	}

	private static LoginFormUiBinder uiBinder = GWT.create(LoginFormUiBinder.class);

	private Presenter presenter;
	private int tabIndex;

	@UiField
	FormPanel form;
	@UiField
	Button googleBtn;
	@UiField
	Button loginBtn;
	@UiField
	PasswordTextBox password;
	@UiField
	CheckBox rememberMe;
	@UiField
	TextBox username;


	public LoginForm() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public int getTabIndex() {
		return tabIndex;
	}

	private boolean isValid() {
		return username.getValue().length() > 0 && password.getValue().length() > 0;
	}

	@UiHandler("form")
	void onFormSubmit(SubmitEvent event) {
		if (null != presenter) {
			presenter.login(username.getValue(), password.getValue());
		}
	}

	@UiHandler("googleBtn")
	void onGoogleClick(ClickEvent event) {
		if (null != presenter) {
			presenter.googleLogin();
		}
	}

	private void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			onLoginClick(null);
		}
	}

	@UiHandler("loginBtn")
	void onLoginClick(ClickEvent event) {
		if (isValid()) {
			form.submit();
		}
	}

	@UiHandler("password")
	void onPasswordKeyUp(KeyUpEvent event) {
		onKeyUp(event);
	}

	@UiHandler("username")
	void onUsernameKeyUp(KeyUpEvent event) {
		onKeyUp(event);
	}

	@Override
	public void reset() {
		setBusy(false);
		username.setText("");
		password.setValue(null);
		rememberMe.setValue(true);
	}

	@Override
	public void setAccessKey(char key) {
		// do nothing
	}

	@Override
	public void setBusy(boolean busy) {
		username.setReadOnly(busy);
		password.setReadOnly(busy);
		rememberMe.setEnabled(!busy);
		loginBtn.setEnabled(!busy);
		googleBtn.setEnabled(!busy);
	}

	@Override
	public void setFocus(boolean focus) {
		username.setFocus(focus);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTabIndex(int index) {
		tabIndex = index;
	}
}
