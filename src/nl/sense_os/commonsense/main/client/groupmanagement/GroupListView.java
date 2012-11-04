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
package nl.sense_os.commonsense.main.client.groupmanagement;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtUser;
import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * View base interface. Extends IsWidget so a view impl can easily provide its container widget.
 */
public interface GroupListView extends IsWidget, HasBusyState {

    public interface Presenter {

        void loadData(AsyncCallback<List<GxtUser>> callback, Object loadConfig);

        void onAddUserClick();

        void onCreateClick();

        void onJoinClick();

        void onLeaveClick();
    }

    GxtGroup getSelectedGroup();

    void onLibChanged();

    void onListUpdate();

    void setPresenter(Presenter presenter);
}
