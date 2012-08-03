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

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ForgotPasswordPlace extends Place {

	@Prefix("forgotpassword")
	public static class Tokenizer implements PlaceTokenizer<ForgotPasswordPlace> {

		@Override
		public ForgotPasswordPlace getPlace(String token) {
			return new ForgotPasswordPlace(token);
		}

		@Override
		public String getToken(ForgotPasswordPlace place) {
			return place.getToken();
		}
	}

	private String token;

	public ForgotPasswordPlace(String token) {
		this.token = token;
	}

	public String getToken() {
		return this.token != null ? token : "";
	}
}
