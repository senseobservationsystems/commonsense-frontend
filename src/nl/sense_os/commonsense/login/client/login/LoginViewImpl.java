/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.sense_os.commonsense.login.client.login;

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

public class LoginViewImpl extends Composite implements LoginView {

	interface Binder extends UiBinder<Widget, LoginViewImpl> {
	}

	private static final Binder binder = GWT.create(Binder.class);

	@UiField
	FormPanel form;
	@UiField
	TextBox username;
	@UiField
	PasswordTextBox password;
	@UiField
	CheckBox rememberMe;
	@UiField
	Button loginBtn;
	@UiField
	Button googleBtn;

	private Presenter presenter;

    private int tabIndex;

	public LoginViewImpl() {
		initWidget(binder.createAndBindUi(this));
	}

	private boolean isValid() {
		return username.getValue().length() > 0 && password.getValue().length() > 0;
	}

	// @UiHandler("forgotPassword")
	// void onForgotPassword(ClickEvent event) {
	// if (null != presenter) {
	// presenter.forgotPassword();
	// }
	// }

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
	public void setBusy(boolean busy) {
		username.setReadOnly(busy);
		password.setReadOnly(busy);
		rememberMe.setEnabled(!busy);
		loginBtn.setEnabled(!busy);
		googleBtn.setEnabled(!busy);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

    @Override
    public int getTabIndex() {
        return tabIndex;
    }

    @Override
    public void setAccessKey(char key) {
        // do nothing
    }

    @Override
    public void setFocus(boolean focused) {
        username.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        tabIndex = index;
    }
}
