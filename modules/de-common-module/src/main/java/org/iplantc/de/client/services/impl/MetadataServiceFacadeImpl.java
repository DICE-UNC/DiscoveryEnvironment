package org.iplantc.de.client.services.impl;

import static org.iplantc.de.shared.services.BaseServiceCallWrapper.Type.POST;

import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.client.models.tags.IpalntTagAutoBeanFactory;
import org.iplantc.de.client.models.tags.IplantTag;
import org.iplantc.de.client.services.DEServiceFacade;
import org.iplantc.de.client.services.MetadataServiceFacade;
import org.iplantc.de.shared.services.BaseServiceCallWrapper.Type;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;

import java.util.List;

public class MetadataServiceFacadeImpl implements MetadataServiceFacade {
    
    private final DEProperties deProps;
    private final DEServiceFacade deServiceFacade;
    IpalntTagAutoBeanFactory factory = GWT.create(IpalntTagAutoBeanFactory.class);

    @Inject
    public MetadataServiceFacadeImpl(final DEServiceFacade deServiceFacade, final DEProperties deProps) {
        this.deServiceFacade = deServiceFacade;
        this.deProps = deProps;
    }

    @Override
    public void createTag(IplantTag tag, AsyncCallback<String> callback) {
       String address = deProps.getMuleServiceBaseUrl() + "tags/user";
        Splittable json = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(tag));
        ServiceCallWrapper wrapper = new ServiceCallWrapper(POST, address, json.getPayload());
        callService(wrapper, callback);
    }

    @Override
    public void suggestTag(String text, int limit, AsyncCallback<String> callback) {
        String address = deProps.getMuleServiceBaseUrl() + "tags/suggestions?contains=" + URL.encode(text) + "&limit=" + limit;
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        callService(wrapper, callback);
    }

    /**
     * Performs the actual service call.
     * 
     * @param wrapper the wrapper used to get to the actual service via the service proxy.
     * @param callback executed when RPC call completes.
     */
    private void callService(ServiceCallWrapper wrapper, AsyncCallback<String> callback) {
        deServiceFacade.getServiceData(wrapper, callback);
    }

    @Override
    public void updateTagDescription(String tagId, String description, AsyncCallback<String> callback) {
        String address = deProps.getMuleServiceBaseUrl() + "tags/user/" + tagId;
        JSONObject obj = new JSONObject();
        if (description != null) {
            obj.put("description", new JSONString(description));
            ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PATCH, address, obj.toString());
            callService(wrapper, callback);
        }

    }

    @Override
    public void attachTags(List<String> tagIds, String objectId, AsyncCallback<String> callback) {
        String address = deProps.getMuleServiceBaseUrl() + "filesystem/entry/" + objectId + "/tags?type=attach";
        ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PATCH, address, arrayToJsonString(tagIds));
        callService(wrapper, callback);

    }

    @Override
    public void detachTags(List<String> tagIds, String objectId, AsyncCallback<String> callback) {
        String address = deProps.getMuleServiceBaseUrl() + "filesystem/entry/" + objectId + "/tags?type=detach";
        ServiceCallWrapper wrapper = new ServiceCallWrapper(Type.PATCH, address, arrayToJsonString(tagIds));
        callService(wrapper, callback);

    }

    private String arrayToJsonString(List<String> ids) {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        int index = 0;
        for (String id:ids) {
            arr.set(index++, new JSONString(id));
        }
        obj.put("tags", arr);
        return obj.toString();

    }
}
