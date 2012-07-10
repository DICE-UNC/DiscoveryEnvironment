package org.iplantc.de.client.services;

import org.iplantc.core.uicommons.client.models.DEProperties;
import org.iplantc.core.uicommons.client.models.UserInfo;
import org.iplantc.de.client.Constants;
import org.iplantc.de.shared.SharedDataApiServiceFacade;
import org.iplantc.de.shared.SharedUnsecuredServiceFacade;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Facade for file editors.
 */
public class FileEditorServiceFacade {
    /**
     * Call service to retrieve the manifest for a requested file
     * 
     * @param idFile desired manifest's file ID (path).
     * @param callback executes when RPC call is complete.
     */
    public void getManifest(String idFile, DiskResourceServiceCallback callback) {
        String address = "org.iplantc.services.de-data-mgmt.file-manifest?path=" //$NON-NLS-1$
                + URL.encodeQueryString(idFile);

        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        SharedDataApiServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * Construct a servlet download URL for the given file ID.
     * 
     * @param idFile the desired file ID to be used in the return URL
     * @return a URL for the given file ID.
     */
    public String getServletDownloadUrl(final String idFile) {
        return GWT.getModuleBaseURL() + Constants.CLIENT.fileDownloadServlet()
                + "?url=display-download&user="
                + UserInfo.getInstance().getUsername() + "&path=" + idFile;
    }

    /**
     * Call service to retrieve data for a requested file
     * 
     * @param idFile file to retrieve raw data from.
     * @param callback executes when RPC call is complete.
     */
    public void getData(String url, DiskResourceServiceCallback callback) {
        String address = DEProperties.getInstance().getDataMgmtBaseUrl() + url;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        SharedUnsecuredServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * Get Tree URLs for the given tree's file ID.
     * 
     * @param idFile file ID (path) of the tree.
     * @param callback executes when RPC call is complete.
     */
    public void getTreeUrl(String idFile, AsyncCallback<String> callback) {
        String address = "org.iplantc.services.buggalo.baseUrl?path=" + URL.encodeQueryString(idFile); //$NON-NLS-1$

        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        SharedDataApiServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void uploadTextAsFile(String destination, String fileContents, AsyncCallback<String> callback) {
        String fullAddress = "org.iplantc.services.de-data-mgmt.saveas";
        JSONObject obj = new JSONObject();
        obj.put("dest", new JSONString(destination)); //$NON-NLS-1$
        obj.put("content", new JSONString(fileContents));
        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                obj.toString());
        SharedDataApiServiceFacade.getInstance().getServiceData(wrapper, callback);

    }

}
