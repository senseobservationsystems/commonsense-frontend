package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.sql.Timestamp;
import java.util.Date;

import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.MD5Wrapper;
import nl.sense_os.commonsense.dto.UserModel;

public class Login extends LayoutContainer {

    private final AsyncCallback<UserModel> mainCallback;
    private Text errorTxt;
    private DataServiceAsync service;

    public Login(AsyncCallback<UserModel> callback) {
        this.mainCallback = callback;
        this.service = (DataServiceAsync) GWT.create(DataService.class);
    }
    
    private void checkLogin(String name, String password,
            final AsyncCallback<UserModel> mainCallback) {

        // show progress dialog
        final MessageBox box = MessageBox.progress("Please wait", "Logging in...", "");
        final ProgressBar bar = box.getProgressBar();
        bar.auto();
        box.show();

        final AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
            @Override
            public void onFailure(Throwable ex) {
                setErrorText("Error: " + ex.getMessage());

                box.close();
            }

            @Override
            public void onSuccess(UserModel userModel) {
                if (userModel != null) {
                    setErrorText("");
                    mainCallback.onSuccess(userModel);
                } else {
                    setErrorText("Invalid UserName or Password");
                }
                
                box.close();
            }
        };
        this.service.checkLogin(name, password, callback);
    }

    private FormPanel createForm() {
        final FormData formData = new FormData("-20");

        final FormPanel form = new FormPanel();
        form.setBodyStyle("padding: 6px");
        form.setHeading("Login form");
        form.setFrame(true);
        form.setWidth(350);

        final TextField<String> email = new TextField<String>();
        email.setFieldLabel("Email");
        email.setValue("vestia@sense-os.nl");
        email.setAllowBlank(false);
        form.add(email, formData);

        final TextField<String> pass = new TextField<String>();
        pass.setFieldLabel("Password");
        pass.setValue("vestia_delfgauw");
        pass.setAllowBlank(false);
        pass.setPassword(true);
        form.add(pass, formData);

        final Button b = new Button("Submit");
        b.addListener(Events.Select, new Listener<ButtonEvent>() {
            @Override
            public void handleEvent(ButtonEvent be) {
                final String mailString = email.getValue();
                final String passString = MD5Wrapper.toMD5(pass.getValue());
                checkLogin(mailString, passString, Login.this.mainCallback);
            }
        });
        form.addButton(b);

        form.setButtonAlign(HorizontalAlignment.CENTER);

        final FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(b);

        return form;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        
        Timestamp start = new Timestamp((new Date().getTime() - (365 * 24 * 60 * 60 * 1000)));
        Log.d("Login", "Timestamp: " + start.toString() + " microEpoch: " + timestampToMicroEpoch(start));
        Timestamp end = new Timestamp(new Date().getTime());
        
        // prepare wrapper panel
        final VerticalPanel panel = new VerticalPanel();

        // create error label and add to wrapper panel
        this.errorTxt = new Text();
        this.errorTxt.setVisible(false);
        panel.add(this.errorTxt);

        // create form and add to wrapper panel
        final FormPanel form = createForm();
        panel.add(form);

        // add panel to layout container
        setLayout(new CenterLayout());
        this.add(panel);
    }
    
    public static String timestampToMicroEpoch(Timestamp t) {
        Long l = t.getTime() / 1000;
        Double d = ((double) t.getNanos()) / 1000000000; 
//        String s = String.format("%.8f", d) + "%20" + l.toString();
        String s = (t.getTime() / 1000) + "";
        return s;
    }

    private void setErrorText(String errorMessage) {
        this.errorTxt.setText(errorMessage);
        this.errorTxt.setVisible(true);
    }
}
