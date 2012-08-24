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

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * View base interface. Extends IsWidget so a view impl can easily provide its container widget.
 */
public interface StateListView extends IsWidget {

	public interface Presenter {

		void disconnectService(GxtSensor sensor, GxtSensor stateSensor);

		void loadData(AsyncCallback<List<GxtSensor>> callback, Object loadConfig);
	}

	void onDisconnectFailure();

	void onLibChanged();

	void onListUpdate();

	void refreshLoader(boolean force);

	void setBusy(boolean busy);

	void setPresenter(Presenter presenter);
}
