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

import nl.sense_os.commonsense.common.client.component.Resettable;

import com.google.gwt.user.client.ui.IsWidget;

public interface LoginView extends IsWidget, Resettable {

	public interface Presenter {

		/**
		 * Starts the forgot password activity
		 */
		void forgotPassword();

		/**
		 * Requests a session ID by authentication through Google's OpenID server
		 */
		void googleLogin();

		/**
		 * Sends a login request to the CommonSense API
		 * 
		 * @param username
		 *            Username
		 * @param password
		 *            Password (unhashed)
		 */
		void login(String username, String password);
	}

	void setBusy(boolean busy);

	void setPresenter(Presenter presenter);
}
