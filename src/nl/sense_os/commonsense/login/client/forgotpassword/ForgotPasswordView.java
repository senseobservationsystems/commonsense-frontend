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

import nl.sense_os.commonsense.common.client.component.Resettable;

import com.google.gwt.user.client.ui.IsWidget;

public interface ForgotPasswordView extends IsWidget, Resettable {

	public interface Presenter {

		/**
		 * Cancels the activity
		 */
		void cancel();

		/**
		 * Requests a password reset at CommonSense
		 * 
		 * @param username
		 *            Username
		 * @param email
		 *            Email
		 */
		void forgotPassword(String username, String email);
	}

	void setPresenter(Presenter listener);
}
