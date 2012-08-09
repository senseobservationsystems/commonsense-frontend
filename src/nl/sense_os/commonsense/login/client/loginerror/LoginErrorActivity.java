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
package nl.sense_os.commonsense.login.client.loginerror;

import nl.sense_os.commonsense.login.client.LoginClientFactory;
import nl.sense_os.commonsense.login.client.login.LoginPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Activities are started and stopped by an ActivityManager associated with a container Widget.
 */
public class LoginErrorActivity extends AbstractActivity implements LoginErrorView.Presenter {

	/**
	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
	 */
	private LoginClientFactory clientFactory;

	/**
	 * Error message
	 */
	private String message;

	public LoginErrorActivity(LoginErrorPlace place, LoginClientFactory clientFactory) {
		this.message = place.getToken();
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {

		LoginErrorView view = clientFactory.getLoginErrorView();
		view.setMessage(message);
		view.setPresenter(this);
		containerWidget.setWidget(view.asWidget());
	}

	@Override
	public void done() {
		clientFactory.getPlaceController().goTo(new LoginPlace(""));
	}
}
