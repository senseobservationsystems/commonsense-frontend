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
package nl.sense_os.commonsense.login.client.forgotpassword;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ForgotPasswordViewImpl extends Composite implements ForgotPasswordView {

	interface Binder extends UiBinder<Widget, ForgotPasswordViewImpl> {
	}

	private static final Binder binder = GWT.create(Binder.class);

	private Presenter presenter;
	@UiField
	TextBox username;
	@UiField
	TextBox email;
	@UiField
	RadioButton emailRadio;
	@UiField
	Button submit;
	@UiField
	FormPanel form;

	public ForgotPasswordViewImpl() {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("submit")
	void onSubmitClick(ClickEvent event) {
		if (isValid()) {
			form.submit();
		}
	}

	/**
	 * @return true is the form is filled correctly
	 */
	private boolean isValid() {
		return (username.isEnabled() && username.getValue().length() > 0)
				|| (email.isEnabled() && email.getValue().length() > 0);
	}

	@UiHandler("cancel")
	void onCancelClick(ClickEvent event) {
		if (null != presenter) {
			presenter.cancel();
		}
	}

	@UiHandler("form")
	void onFormSubmit(SubmitEvent event) {
		if (null != presenter) {
			presenter.forgotPassword(username.isEnabled() ? username.getValue() : null,
					email.isEnabled() ? email.getValue() : null);
		}
	}

	@Override
	public void reset() {
		emailRadio.setValue(true);
		username.setValue("");
		email.setValue("");
	}

	@UiHandler("emailRadio")
	void onEmailRadioValueChange(ValueChangeEvent<Boolean> event) {
		boolean value = event.getValue();
		username.setEnabled(!value);
		email.setEnabled(value);
	}

	@UiHandler("usernameRadio")
	void onUsernameRadioValueChange(ValueChangeEvent<Boolean> event) {
		boolean value = event.getValue();
		username.setEnabled(value);
		email.setEnabled(!value);
	}
}
