package nl.sense_os.commonsense.client.auth.registration;

import java.util.logging.Logger;

import nl.sense_os.commonsense.client.auth.login.LoginEvents;
import nl.sense_os.commonsense.client.common.constants.Urls;
import nl.sense_os.commonsense.client.common.utility.Md5Hasher;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class RegisterController extends Controller {

    private static final Logger LOG = Logger.getLogger(RegisterController.class.getName());
    private View form;

    public RegisterController() {
        registerEventTypes(RegisterEvents.Show);
        registerEventTypes(RegisterEvents.RegisterRequest);
    }

    @Override
    public void handleEvent(AppEvent event) {
        final EventType type = event.getType();

        if (type.equals(RegisterEvents.RegisterRequest)) {
            // LOG.fine("RegisterRequest");
            final String username = event.<String> getData("username");
            final String password = event.<String> getData("password");
            final String name = event.<String> getData("name");
            final String surname = event.<String> getData("surname");
            final String email = event.<String> getData("email");
            final String mobile = event.<String> getData("mobile");
            register(username, password, name, surname, email, mobile);

        } else {
            forwardToView(form, event);
        }

    }

    @Override
    protected void initialize() {
        super.initialize();
        form = new RegisterPanel(this);
    }

    private void onRegisterFailure(int code) {
        AppEvent failure = new AppEvent(RegisterEvents.RegisterFailure);
        failure.setData("code", code);
        forwardToView(form, failure);
    }

    private void onRegisterSuccess(String response, String username, String password) {
        AppEvent loginRequest = new AppEvent(LoginEvents.LoginRequest);
        loginRequest.setData("username", username);
        loginRequest.setData("password", password);
        Dispatcher.forwardEvent(loginRequest);

        forwardToView(form, new AppEvent(RegisterEvents.RegisterSuccess));
    }

    private void register(final String username, final String password, String name,
            String surname, String email, String mobile) {

        // prepare request properties
        final String url = Urls.USERS + ".json";

        // create request body
        String userJson = "\"user\":{";
        userJson += "\"username\":\"" + username + "\"";
        userJson += "," + "\"password\":\"" + Md5Hasher.hash(password) + "\"";
        userJson += "," + "\"name\":\"" + name + "\"";
        userJson += "," + "\"surname\":\"" + surname + "\"";
        userJson += "," + "\"email\":\"" + email + "\"";
        userJson += "," + "\"mobile\":\"" + mobile + "\"";
        userJson += "}";
        final String body = "{" + userJson + "}";

        // prepare request callback
        RequestCallback callback = new RequestCallback() {

            @Override
            public void onError(Request request, Throwable exception) {
                LOG.warning("POST registration onError callback: " + exception.getMessage());
                onRegisterFailure(0);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
                LOG.finest("POST registration response received: " + response.getStatusText());
                int statusCode = response.getStatusCode();
                if (Response.SC_CREATED == statusCode) {
                    onRegisterSuccess(response.getText(), username, password);
                } else {
                    LOG.warning("POST registration returned incorrect status: " + statusCode);
                    onRegisterFailure(statusCode);
                }
            }
        };

        // send request
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        try {
            builder.sendRequest(body, callback);
        } catch (RequestException e) {
            LOG.warning("POST registration request threw exception: " + e.getMessage());
            onRegisterFailure(0);
        }
    }

    // private void xhrRegister(String username, String password, String name, String surname,
    // String email, String mobile) {
    //
    // String hashPass = Md5Hasher.hash(password);
    //
    // // prepare request properties
    // final String method = "POST";
    // final String url = Urls.USERS + ".json";
    // final AppEvent onSuccess = new AppEvent(RegisterEvents.AjaxRegisterSuccess);
    // onSuccess.setData("username", username);
    // onSuccess.setData("password", password);
    // final AppEvent onFailure = new AppEvent(RegisterEvents.AjaxRegisterFailure);
    //
    // // create request body
    // String userJson = "\"user\":{";
    // userJson += "\"username\":\"" + username + "\"";
    // userJson += "," + "\"password\":\"" + hashPass + "\"";
    // userJson += "," + "\"name\":\"" + name + "\"";
    // userJson += "," + "\"surname\":\"" + surname + "\"";
    // userJson += "," + "\"email\":\"" + email + "\"";
    // userJson += "," + "\"mobile\":\"" + mobile + "\"";
    // userJson += "}";
    // final String body = "{" + userJson + "}";
    //
    // // send request to AjaxController
    // final AppEvent ajaxRequest = new AppEvent(AjaxEvents.Request);
    // ajaxRequest.setData("method", method);
    // ajaxRequest.setData("url", url);
    // ajaxRequest.setData("body", body);
    // ajaxRequest.setData("onSuccess", onSuccess);
    // ajaxRequest.setData("onFailure", onFailure);
    // Dispatcher.forwardEvent(ajaxRequest);
    // }
}
