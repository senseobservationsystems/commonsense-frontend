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
import nl.sense_os.commonsense.login.client.forgotpassword.ForgotPasswordView;
import nl.sense_os.commonsense.login.client.login.LoginView;
import nl.sense_os.commonsense.login.client.loginerror.LoginErrorView;
import nl.sense_os.commonsense.login.client.newpassword.NewPasswordView;
import nl.sense_os.commonsense.login.client.openidconnect.OpenIdConnectView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

/**
 * ClientFactory helpful to use a factory or dependency injection framework like GIN to obtain
 * references to objects needed throughout your application like the {@link EventBus},
 * {@link PlaceController} and views.
 */
public interface LoginClientFactory {

	EventBus getEventBus();

	ForgotPasswordView getForgotPasswordView();

	LoginErrorView getLoginErrorView();

	LoginView getLoginView();

	LoginApplicationView getMainView();

	NewPasswordView getNewPasswordView();

	OpenIdConnectView getOpenIdConnectView();

	PlaceController getPlaceController();
}
