package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import nl.sense_os.commonsense.client.services.DataService;
import nl.sense_os.commonsense.client.services.DataServiceAsync;

/**
 * @see <a href=
 *      "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 *      >blobstore image service</a>
 */
public class ImageUploadForm extends LayoutContainer {

    @SuppressWarnings("unused")
    private static final String TAG = "ImageUploadForm";
    private Button btn;
    private final FormPanel panel = new FormPanel();
    private DataServiceAsync service = GWT.create(DataService.class);

    public ImageUploadForm() {

        // Disable the button until we get the URL to POST to
        panel.setHeading("Upload Image");
        panel.setFrame(true);
        panel.setEncoding(Encoding.MULTIPART);
        panel.setMethod(Method.POST);
        panel.setButtonAlign(HorizontalAlignment.CENTER);
        panel.setWidth(350);

        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setName("image");
        file.setFieldLabel("File");
        panel.add(file);

        btn = new Button("Loading...");
        btn.setEnabled(false);
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {

                if (!panel.isValid()) {
                    return;
                }

                panel.submit();
            }
        });
        panel.addButton(btn);

        // Now we use out GWT-RPC service and get an URL
        startNewBlobstoreSession();

        // Once we've hit submit and it's complete, let's set the form to a new session.
        // We could also have probably done this on the onClick handler
        panel.addListener(Events.Submit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {

                final PopupPanel imagePopup = new PopupPanel(true);
                imagePopup.setAnimationEnabled(true);
                imagePopup.setWidget(new Image(be.getResultHtml()));
                imagePopup.setGlassEnabled(true);
                imagePopup.setAutoHideEnabled(true);

                imagePopup.center();
            }

        });

        setLayout(new CenterLayout());
        add(panel);
    }

    private void startNewBlobstoreSession() {
        service.getBlobstoreUploadUrl(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                // We probably want to do something here
            }

            @Override
            public void onSuccess(String result) {
                panel.setAction(result);
                btn.setText("Upload");
                btn.setEnabled(true);
            }
        });
    }
}
