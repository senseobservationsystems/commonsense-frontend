package nl.sense_os.commonsense.client;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import com.google.gwt.user.client.ui.Label;

import nl.sense_os.commonsense.client.helper.MD5Wrapper;
import nl.sense_os.commonsense.dto.UserModel;
import nl.sense_os.commonsense.pojo.User;

public class LoginForm extends LayoutContainer {

    private AsyncCallback<UserModel> callback;
    private Label errorLbl;
    DataServiceAsync svc;
    
    public LoginForm(AsyncCallback<UserModel> callback) {
        this.callback = callback;
        this.svc = (DataServiceAsync) GWT.create(DataService.class);
    }
    
    private void checkLogin(String name,String password, final AsyncCallback<UserModel> mainCallback) {

        AsyncCallback<UserModel> callback = new AsyncCallback<UserModel>() {
            public void onSuccess(UserModel userModel) {
                if (userModel != null) {
                    setErrorText("");
                    mainCallback.onSuccess(userModel);
                } else {
                    setErrorText("Invalid UserName or Password");                    
                }
            }
            public void onFailure(Throwable ex) {
                setErrorText("Error: "+ex.getMessage());
            }
        };
        svc.checkLogin(name, password, callback);
    }
    
    private FormPanel createForm() {
        FormData formData = new FormData("-20");
        
        final FormPanel form = new FormPanel();
        form.setBodyStyle("padding: 6px");  
        form.setHeading("Login form");
        form.setFrame(true);
        form.setWidth(350);
        
        final TextField<String> email = new TextField<String>();  
        email.setFieldLabel("Email");
        email.setValue("steven@sense-os.nl");
        email.setAllowBlank(false);  
        form.add(email, formData);
        
        final TextField<String> pass = new TextField<String>();  
        pass.setFieldLabel("Password");  
        pass.setValue("1234");
        pass.setAllowBlank(false);
        pass.setPassword(true);
        form.add(pass, formData);
        
        Button b = new Button("Submit");
        b.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent be) {
                final String mailString = email.getValue();
                final String passString = MD5Wrapper.toMD5(pass.getValue());
                checkLogin(mailString, passString, callback);
            }
          });
        form.addButton(b);
      
        form.setButtonAlign(HorizontalAlignment.CENTER);  
        
        FormButtonBinding binding = new FormButtonBinding(form);  
        binding.addButton(b); 
        
        return form;
    }
    
    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        
        // prepare wrapper panel
        VerticalPanel panel = new VerticalPanel();
        
        // create error label and add to wrapper panel
        errorLbl = new Label();
        errorLbl.setVisible(false);
        panel.add(errorLbl); 
        
        // create form and add to wrapper panel
        FormPanel form = createForm();
        panel.add(form);        
        
        // add panel to layout container
        this.setLayout(new CenterLayout());
        this.add(panel);
    }
    
    private void setErrorText(String errorMessage)  {
        errorLbl.setText(errorMessage);
        errorLbl.setVisible(true);
    }
}
