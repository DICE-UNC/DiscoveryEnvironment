package org.iplantc.de.client.services;

import org.iplantc.core.uiapplications.client.services.AppTemplateUserServiceFacade;
import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.shared.services.ServiceCallWrapper;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides access to remote services for operations related to job submission templates.
 * 
 * @author Dennis Roberts
 */
public class TemplateServiceFacade implements AppTemplateUserServiceFacade {
    /**
     * Retrieves a template from the database.
     * 
     * @param templateId unique identifier for the template.
     * @param callback called when the RPC call is complete.
     */
    public void getTemplate(String templateId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "template/" + templateId; //$NON-NLS-1$
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void getAnalysisCategories(String workspaceId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl()
                + "get-only-analysis-groups/" + workspaceId; //$NON-NLS-1$
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAnalysis(String analysisGroupId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "get-analyses-in-group/" //$NON-NLS-1$
                + analysisGroupId;
        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getDataObjectsForAnalysis(String analysisId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl()
                + "analysis-data-objects/" + analysisId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * Adds an app to the given public categories.
     * 
     * @param application
     * @param callback
     */
    public void publishToWorld(JSONObject application, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "make-analysis-public"; //$NON-NLS-1$

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                application.toString());

        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    @Override
    public void rateAnalysis(String analysisId, int rating, String appName, String comment,
            AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "rate-analysis";

        JSONObject body = new JSONObject();
        body.put("analysis_id", new JSONString(analysisId));
        body.put("rating", new JSONNumber(rating));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                body.toString());
        addComment(appName, comment, wrapper, callback);
    }

    private void addComment(String appName, String comment, final ServiceCallWrapper wrapper,
            final AsyncCallback<String> callback) {
        ConfluenceServiceFacade.getInstance().addComment(appName, comment, new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                DEServiceFacade.getInstance().getServiceData(wrapper, callback);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    @Override
    public void updateRating(String analysisId, int rating, String appName, String comment,
            String commentId,
            AsyncCallback<String> callback) {
        rateAnalysis(analysisId, rating, appName, comment, callback);
        // TODO call update-rating
        // String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "update-rating";
        //
        // JSONObject body = new JSONObject();
        // body.put("analysis_id", new JSONString(analysisId));
        // body.put("rating", new JSONNumber(rating));
        // body.put("comment", new JSONString(comment));
        // body.put("comment_id", new JSONString(commentId));
        //
        // ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
        // body.toString());
        // DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    @Override
    public void deleteRating(String analysisId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "delete-rating";

        JSONObject body = new JSONObject();
        body.put("analysis_id", new JSONString(analysisId));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                body.toString());
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    @Override
    public void favoriteAnalysis(String workspaceId, String analysisId, boolean fav,
            AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "update-favorites";

        JSONObject body = new JSONObject();
        body.put("workspace_id", new JSONString(workspaceId));
        body.put("analysis_id", new JSONString(analysisId));
        body.put("user_favorite", JSONBoolean.getInstance(fav));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                body.toString());
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * Checks if the given analysisId is able to be exported to TITo via a copy or edit. The service will
     * respond with JSON that contains a boolean "can-export" key, and a "cause" key if "can-export" is
     * false:
     * 
     * <code>
     * { "can-export": false, "cause": "Analysis has multiple templates." }
     * </code>
     * 
     * @param analysisId
     * @param callback
     */
    public void analysisExportable(String analysisId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl()
                + "can-export-analysis";

        JSONObject body = new JSONObject();
        body.put("analysis_id", new JSONString(analysisId));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                body.toString());
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void editAnalysis(String analysisId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "edit-template/"
                + analysisId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void copyAnalysis(String analysisId, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "copy-template/"
                + analysisId;

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    public void deleteAnalysisFromWorkspace(String email, String analysisId,
            AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "delete-workflow";
        JSONObject body = new JSONObject();
        body.put("analysis_id", new JSONString(analysisId));
        body.put("email", new JSONString(email));

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                body.toString());
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchAnalysis(String search, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "search-analyses/" //$NON-NLS-1$
                + URL.encode(search);

        ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishWorkflow(String body, AsyncCallback<String> callback) {
        String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl() + "update-workflow";

        ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
    }
}
