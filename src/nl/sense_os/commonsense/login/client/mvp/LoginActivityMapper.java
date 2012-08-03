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
package nl.sense_os.commonsense.login.client.mvp;

import nl.sense_os.commonsense.login.client.ClientFactory;
import nl.sense_os.commonsense.login.client.forgotpassword.ForgotPasswordActivity;
import nl.sense_os.commonsense.login.client.login.LoginActivity;
import nl.sense_os.commonsense.login.client.loginerror.LoginErrorActivity;
import nl.sense_os.commonsense.login.client.newpassword.NewPasswordActivity;
import nl.sense_os.commonsense.login.client.openidconnect.OpenIdConnectActivity;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class LoginActivityMapper implements ActivityMapper {

	private ClientFactory clientFactory;

	public LoginActivityMapper(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public Activity getActivity(Place place) {

		if (place instanceof LoginPlace) {
			return new LoginActivity((LoginPlace) place, clientFactory);
		} else if (place instanceof NewPasswordPlace) {
			return new NewPasswordActivity((NewPasswordPlace) place, clientFactory);
		} else if (place instanceof ForgotPasswordPlace) {
			return new ForgotPasswordActivity((ForgotPasswordPlace) place, clientFactory);
		} else if (place instanceof LoginErrorPlace) {
			return new LoginErrorActivity((LoginErrorPlace) place, clientFactory);
		} else if (place instanceof OpenIdConnectPlace) {
			return new OpenIdConnectActivity((OpenIdConnectPlace) place, clientFactory);
		}
		return null;
	}
}
