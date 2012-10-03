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

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class NewPasswordPlace extends Place {

	/**
	 * PlaceTokenizer knows how to serialize the Place's state to a URL token.
	 */
	@Prefix("newpassword")
	public static class Tokenizer implements PlaceTokenizer<NewPasswordPlace> {

		@Override
		public NewPasswordPlace getPlace(String token) {
			return new NewPasswordPlace(token);
		}

		@Override
		public String getToken(NewPasswordPlace place) {
			return place.getToken();
		}

	}

	private String token;

	public NewPasswordPlace() {
		this("");
	}

	public NewPasswordPlace(String token) {
		this.token = token;
	}

	public String getToken() {
		return token != null ? token : "";
	}
}
