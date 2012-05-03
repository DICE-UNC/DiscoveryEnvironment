package org.iplantc.de.client.services;

import org.iplantc.core.uicommons.client.models.DEProperties;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.iplantc.core.uicommons.client.DEServiceFacade;

/**
 * A service facade to save and retrieve user session
 * 
 * @author sriram
 * 
 */
public class UserSessionServiceFacade {

    public void getUserSession(AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "sessions"; //$NON-NLS-1$
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void saveUserSession(JSONObject json, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "sessions"; //$NON-NLS-1$

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                json.toString());

        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void searchCollaborators(String term, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "user-search/" + term; //$NON-NLS-1$

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);

        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

}
