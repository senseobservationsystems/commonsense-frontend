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
package nl.sense_os.commonsense.login.client;

import nl.sense_os.commonsense.login.client.application.LoginApplicationView;
import nl.sense_os.commonsense.login.client.application.LoginApplicationViewImpl;
import nl.sense_os.commonsense.login.client.forgotpassword.ForgotPasswordView;
import nl.sense_os.commonsense.login.client.forgotpassword.ForgotPasswordViewImpl;
import nl.sense_os.commonsense.login.client.login.LoginView;
import nl.sense_os.commonsense.login.client.login.LoginViewImpl;
import nl.sense_os.commonsense.login.client.loginerror.LoginErrorView;
import nl.sense_os.commonsense.login.client.loginerror.LoginErrorViewImpl;
import nl.sense_os.commonsense.login.client.newpassword.NewPasswordView;
import nl.sense_os.commonsense.login.client.newpassword.NewPasswordViewImpl;
import nl.sense_os.commonsense.login.client.openidconnect.OpenIdConnectView;
import nl.sense_os.commonsense.login.client.openidconnect.OpenIdConnectViewImpl;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class LoginClientFactoryImpl implements LoginClientFactory {

	private static final EventBus eventBus = new SimpleEventBus();
	private static final PlaceController placeController = new PlaceController(eventBus);
	private static final LoginView login = new LoginViewImpl();
	private static final NewPasswordViewImpl newPassword = new NewPasswordViewImpl();
	private static final ForgotPasswordViewImpl forgotPassword = new ForgotPasswordViewImpl();
	private static final LoginApplicationViewImpl main = new LoginApplicationViewImpl();
	private static final LoginErrorView loginError = new LoginErrorViewImpl();
	private static final OpenIdConnectViewImpl openIdConnect = new OpenIdConnectViewImpl();

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public ForgotPasswordView getForgotPasswordView() {
		return forgotPassword;
	}

	@Override
	public LoginErrorView getLoginErrorView() {
		return loginError;
	}

	@Override
	public LoginView getLoginView() {
		return login;
	}

	@Override
	public LoginApplicationView getMainView() {
		return main;
	}

	@Override
	public NewPasswordView getNewPasswordView() {
		return newPassword;
	}

	@Override
	public OpenIdConnectView getOpenIdConnectView() {
		return openIdConnect;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}
}
