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
package nl.sense_os.commonsense.main.client.sensormanagement;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * View base interface. Extends IsWidget so a view impl can easily provide its container widget.
 */
public interface SensorListView extends IsWidget, HasBusyState {

    public interface Presenter {

        void loadData(AsyncCallback<ListLoadResult<GxtSensor>> callback, boolean force);

        void onAlertClick();

        void onDeleteClick();

        void onPublishClick();

        void onShareClick();

        void onUnshareClick();

        void onVisualizeClick();
    }

    List<GxtSensor> getSelection();

    void onLibChanged();

    void onListUpdate();

    void refreshLoader(boolean force);

    void setPresenter(Presenter presenter);
}
