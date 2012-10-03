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
package nl.sense_os.commonsense.login.client.newpassword;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewPasswordViewImpl extends Composite implements NewPasswordView {

	interface Binder extends UiBinder<Widget, NewPasswordViewImpl> {
	}

	private static final Binder binder = GWT.create(Binder.class);

	@UiField
	FormPanel form;
	@UiField
	PasswordTextBox password;
	@UiField
	PasswordTextBox passwordCheck;

	private Presenter presenter;

	public NewPasswordViewImpl() {
		initWidget(binder.createAndBindUi(this));
	}

	private boolean isValid() {
		return password.getValue().length() > 0
				&& password.getValue().equals(passwordCheck.getValue());
	}

	@UiHandler("cancelBtn")
	void onCancelClick(ClickEvent event) {
		if (null != presenter) {
			presenter.cancel();
		}
	}

	@UiHandler("form")
	void onSubmit(SubmitEvent event) {
		if (null != presenter) {
			presenter.submit(password.getValue());
		}
	}

	@UiHandler("submitBtn")
	void onSubmitClick(ClickEvent event) {
		if (null != presenter && isValid()) {
			form.submit();
		}
	}

	@Override
	public void reset() {
		password.setValue("");
		passwordCheck.setValue("");
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
