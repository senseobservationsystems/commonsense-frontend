package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.DND.Operation;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.services.DataService;
import nl.sense_os.commonsense.client.services.DataServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.TagModel;
import nl.sense_os.commonsense.dto.building.Floor;

public class BuildingMgmt extends LayoutContainer {

    private static final String TAG = "BuildingMgmt";
    private DataServiceAsync service = GWT.create(DataService.class);
    private Image building;

    public BuildingMgmt() {
        setLayout(new CenterLayout());
        add(new Text("Loading..."));

        service.getRecentImages(new AsyncCallback<List<Floor>>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert("CommonSense Web Application", "Failure getting building images",
                        null);

                BuildingMgmt.this.removeAll();
                BuildingMgmt.this.add(new Text("No building images found..."));
            }

            @Override
            public void onSuccess(List<Floor> result) {

                BuildingMgmt.this.removeAll();

                if (result.size() > 0) {
                    onImageLoad(result.get(0));
                } else {
                    BuildingMgmt.this.add(new Text("No building images found..."));
                }
            }
        });
    }

    private void onImageLoad(Floor img) {
        building = new Image(img.getServingUrl());
        building.setTitle(img.getKey());
        LayoutContainer lc = new LayoutContainer();
        lc.add(building);

        final DropTarget dropTarget = new DropTarget(lc);
        dropTarget.setOperation(Operation.COPY);
        dropTarget.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                super.dragDrop(e);

                int x = e.getClientX() - building.getAbsoluteLeft();
                int y = e.getClientY() - building.getAbsoluteTop();

                final ArrayList<TreeStoreModel> data = e.<ArrayList<TreeStoreModel>> getData();
                onTagsDropped(data, x, y);
            }
        });

        BuildingMgmt.this.add(lc);
    }

    private void onTagsDropped(ArrayList<TreeStoreModel> tags, final int x, final int y) {
        for (TreeStoreModel tsm : tags) {
            final TagModel tag = (TagModel) tsm.getModel();
            Log.d(TAG, "tag " + tag.get("text") + " dropped on building at: (" + x + ", " + y + ")");
            
            service.addLocationValues(x, y, tag.getTaggedId(), building.getTitle(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                    MessageBox.alert("Fail", "Failed to store the location for the sensor " + tag.get("text"), null);
                }

                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "tag " + tag.get("text") + " location (" + x + ", " + y + ") stored");
                }                
            });
        }
    }
}
