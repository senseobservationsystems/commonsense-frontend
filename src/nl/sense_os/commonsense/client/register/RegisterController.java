package nl.sense_os.commonsense.client.register;

import nl.sense_os.commonsense.client.ajax.AjaxEvents;
import nl.sense_os.commonsense.client.login.LoginEvents;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.client.utility.Md5Hasher;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;

public class RegisterController extends Controller {

    private static final String TAG = "RegisterController";
    private View form;

    public RegisterController() {
        registerEventTypes(RegisterEvents.Show);
        registerEventTypes(RegisterEvents.RegisterRequest, RegisterEvents.AjaxRegisterSuccess,
                RegisterEvents.AjaxRegisterFailure);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(RegisterEvents.RegisterRequest)) {
            // Log.d(TAG, "RegisterRequest");
            final String username = event.<String> getData("username");
            final String password = event.<String> getData("password");
            final String name = event.<String> getData("name");
            final String surname = event.<String> getData("surname");
            final String email = event.<String> getData("email");
            final String mobile = event.<String> getData("mobile");
            register(username, password, name, surname, email, mobile);

        } else if (type.equals(RegisterEvents.AjaxRegisterSuccess)) {
            // Log.d(TAG, "AjaxRegisterSuccess");
            final String response = event.<String> getData("response");
            final String username = event.<String> getData("username");
            final String password = event.<String> getData("password");
            registerCallback(response, username, password);

        } else if (type.equals(RegisterEvents.AjaxRegisterFailure)) {
            Log.w(TAG, "AjaxRegisterFailure");
            registerFailure();

        } else {
            forwardToView(this.form, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        this.form = new RegisterPanel(this);
    }

    private void register(String username, String password, String name, String surname,
            String email, String mobile) {

        String hashPass = Md5Hasher.hash(password);

        // prepare request properties
        final String method = "POST";
        final String url = Constants.URL_USERS + ".json";
        final AppEvent onSuccess = new AppEvent(RegisterEvents.AjaxRegisterSuccess);
        onSuccess.setData("username", username);
        onSuccess.setData("password", password);
        final AppEvent onFailure = new AppEvent(RegisterEvents.AjaxRegisterFailure);

        // create request body
        String userJson = "\"user\":{";
        userJson += "\"username\":\"" + username + "\"";
        userJson += "," + "\"password\":\"" + hashPass + "\"";
        userJson += "," + "\"name\":\"" + name + "\"";
        userJson += "," + "\"surname\":\"" + surname + "\"";
        userJson += "," + "\"email\":\"" + email + "\"";
        userJson += "," + "\"mobile\":\"" + mobile + "\"";
        userJson += "}";
        final String body = "{" + userJson + "}";

        // send request to AjaxController
        final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
        ajaxRequest.setData("method", method);
        ajaxRequest.setData("url", url);
        ajaxRequest.setData("body", body);
        ajaxRequest.setData("onSuccess", onSuccess);
        ajaxRequest.setData("onFailure", onFailure);
        Dispatcher.forwardEvent(ajaxRequest);
    }

    private void registerCallback(String response, String username, String password) {
        AppEvent loginRequest = new AppEvent(LoginEvents.LoginRequest);
        loginRequest.setData("username", username);
        loginRequest.setData("password", password);
        Dispatcher.forwardEvent(loginRequest);

        forwardToView(this.form, new AppEvent(RegisterEvents.RegisterSuccess));
    }

    private void registerFailure() {
        forwardToView(this.form, new AppEvent(RegisterEvents.RegisterFailure));
    }

}
