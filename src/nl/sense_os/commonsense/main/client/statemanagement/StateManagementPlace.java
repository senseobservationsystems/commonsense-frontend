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
package nl.sense_os.commonsense.main.client.statemanagement;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * A place object representing a particular state of the UI. A Place can be converted to and from a
 * URL history token by defining a {@link PlaceTokenizer} for each {@link Place}, and the
 * {@link PlaceHistoryHandler} automatically updates the browser URL corresponding to each
 * {@link Place} in your app.
 */
public class StateManagementPlace extends Place {

	/**
	 * PlaceTokenizer knows how to serialize the Place's state to a URL token.
	 */
	@Prefix("statemanagement")
	public static class Tokenizer implements PlaceTokenizer<StateManagementPlace> {

		@Override
		public StateManagementPlace getPlace(String token) {
			return new StateManagementPlace(token);
		}

		@Override
		public String getToken(StateManagementPlace place) {
			return place.getToken();
		}

	}

	private String token;

	public StateManagementPlace() {
		this("");
	}

	public StateManagementPlace(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}
}
