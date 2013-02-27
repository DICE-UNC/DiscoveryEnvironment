package org.iplantc.admin.belphegor.client.views.panels;

import org.iplantc.admin.belphegor.client.Constants;
import org.iplantc.admin.belphegor.client.I18N;
import org.iplantc.admin.belphegor.client.Services;
import org.iplantc.admin.belphegor.client.models.ToolIntegrationAdminProperties;
import org.iplantc.admin.belphegor.client.util.FormFieldBuilderUtil;
import org.iplantc.core.jsonutil.JsonUtil;
import org.iplantc.core.uiapps.client.models.autobeans.App;
import org.iplantc.core.uiapps.client.util.AnalysisUtil;
import org.iplantc.core.uicommons.client.ErrorHandler;
import org.iplantc.core.uicommons.client.validators.BasicEmailValidator;
import org.iplantc.de.shared.services.ConfluenceServiceFacade;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

@Deprecated
public class EditAppDetailsPanel extends LayoutContainer {
    private static final String NAME = "name";
    private static final String INTEGRATOR_NAME = "integName"; //$NON-NLS-1$
    private static final String EMAIL = "email"; //$NON-NLS-1$
    private static final String WIKI_URL = "wiki_url"; //$NON-NLS-1$
    private static final String DESC = "desc"; //$NON-NLS-1$

    private TextField<String> integratorNameField;
    private TextField<String> emailField;

    private TextField<String> appNameField;
    private TextArea descField;
    private CheckBox isDisabledField;
    private CheckBoxGroup chkGroup;

    private TextField<String> urlField;
    private Button btnSubmit;
    private Button btnCancel;
    private FormPanel form;
    private final App app;
    private final AsyncCallback<String> closeCallback;
    private String oldAppName;

    /**
     * Creates a new instance of EditAppDetailsPanel
     * 
     * @param app the analysis for editing
     * @param closeCallback a command to execute when one of the buttons is clicked. onSuccess is called
     *            when the publish form is successfully submitted, and onFailure is called when the
     *            publish fails, or the user cancels the form.
     */
    public EditAppDetailsPanel(App app, AsyncCallback<String> closeCallback) {
        this.app = app;
        this.closeCallback = closeCallback;
        setLayout(new FormLayout());
        initForm();
    }

    private void initForm() {
        form = new FormPanel() {
            @Override
            public boolean isValid(boolean preventMark) {
                return super.isValid(preventMark);
            }
        };

        form.setLayout(new FormLayout(LabelAlign.TOP));
        form.setSize(595, 400);
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setButtonAlign(HorizontalAlignment.CENTER);
        form.setScrollMode(Scroll.AUTOY);

        buildFields();
        addFields();
        initFields();
        buildButtons();

        add(form);
    }

    private void initFields() {
        appNameField.setValue(app.getName());
        descField.setValue(app.getDescription());
        integratorNameField.setValue(app.getIntegratorName());
        if (app.getWikiUrl() != null && !app.getWikiUrl().isEmpty()) {
            urlField.setValue(app.getWikiUrl());
        }

        emailField.setValue(app.getIntegratorEmail());
        isDisabledField.setValue(app.isDisabled());

        urlField.setEmptyText(Constants.CLIENT.appWikiUrl()
                + ToolIntegrationAdminProperties.getInstance().getValidAppWikiUrlPath()
                + app.getName());
        addNameFieldListener();

    }

    private JSONObject toJson() {
        app.setName(getNonNullString(appNameField.getValue()));
        app.setIntegratorEmail(getNonNullString(emailField.getValue()));
        app.setIntegratorName(getNonNullString(integratorNameField.getValue()));
        app.setWikiUrl(getNonNullString(urlField.getValue()));
        app.setDescription(getNonNullString(descField.getValue()));
        app.setDisabled(isDisabledField.getValue());
        String jsonString = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(app)).getPayload();

        // return analysis.toJson();
        return JsonUtil.getObject(jsonString);

    }

    private String getNonNullString(String value) {
        return (value == null ? "" : value); //$NON-NLS-1$
    }

    private void addFields() {
        FormData formData = new FormData("93%"); //$NON-NLS-1$

        LayoutContainer top = buildColumnLayoutContainer();
        LayoutContainer left = buildLeftLayoutContainer(buildFormLayout());

        left.add(appNameField, formData);
        left.add(integratorNameField, formData);
        left.add(emailField, formData);
        left.add(chkGroup, formData);

        top.add(left, new ColumnData(.5));
        form.add(top);

        form.add(createSpacer());

        form.add(descField, formData);

        form.add(new Label(
                "<span class='required_marker'>*</span> " + I18N.DISPLAY.wikiUrlLabel(Constants.CLIENT.publishDocumentationUrl()) + ":")); //$NON-NLS-1$
        form.add(buildUrlField(), formData);
    }

    private void buildButtons() {
        buildSubmitButton();
        buildCancelButton();
        form.addButton(btnSubmit);
        form.addButton(btnCancel);
        FormButtonBinding binding = new FormButtonBinding(form);
        binding.addButton(btnSubmit);
    }

    private LayoutContainer buildLeftLayoutContainer(FormLayout left2Layout) {
        LayoutContainer left2 = new LayoutContainer(left2Layout);
        left2.setStyleAttribute("paddingRight", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        return left2;
    }

    private FormLayout buildFormLayout() {
        FormLayout left1Layout = new FormLayout();
        left1Layout.setLabelAlign(LabelAlign.TOP);
        return left1Layout;
    }

    private LayoutContainer buildColumnLayoutContainer() {
        ColumnLayout colLayout1 = new ColumnLayout();
        colLayout1.setAdjustForScroll(true);
        LayoutContainer pnl1 = new LayoutContainer(colLayout1);
        return pnl1;
    }

    /**
     * Spacer for non-Field elements
     * 
     * @return
     */
    private Component createSpacer() {
        return new Html("<div style=\"margin-top:5px;\"/>"); //$NON-NLS-1$
    }

    private void buildFields() {
        appNameField = buildAppNameField();

        integratorNameField = FormFieldBuilderUtil.buildTextField(I18N.DISPLAY.integratorName(), false,
                null, INTEGRATOR_NAME, null, 32);

        emailField = FormFieldBuilderUtil.buildTextField(I18N.DISPLAY.integratorEmail(), false, null,
                EMAIL, new BasicEmailValidator(), 256);

        descField = FormFieldBuilderUtil.buildTextArea(I18N.DISPLAY.analysisDesc(), true,
                app.getDescription(), DESC, 255);
        isDisabledField = new CheckBox();
        isDisabledField.setBoxLabel(I18N.DISPLAY.appDisabled());
        chkGroup = new CheckBoxGroup();
        chkGroup.setFieldLabel(I18N.DISPLAY.tempDisable());
        chkGroup.add(isDisabledField);
    }

    private TextField<String> buildAppNameField() {
        TextField<String> ret = FormFieldBuilderUtil.buildTextField(I18N.DISPLAY.name(), false, null,
                NAME, null, 255);

        // Set restricted characters in this field's regex validation.
        AnalysisUtil.setAppNameRegexValidation(ret);

        return ret;
    }

    private void addNameFieldListener() {
        appNameField.addListener(Events.Change, new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                oldAppName = be.getOldValue().toString();
            }
        });
    }

    private VerticalPanel buildUrlField() {
        final String[] validUrl = ToolIntegrationAdminProperties.getInstance().getValidAppWikiUrlPath();
        urlField = FormFieldBuilderUtil.buildTextField(null, false, null, WIKI_URL, new Validator() {
            @Override
            public String validate(Field<?> field, String value) {
                // make sure the URL protocol is http or https, has a valid iPlant host name, and has at
                // least one character under the validAppWikiUrlPath.
                if (validUrl.length > 1) {
                    if (!value.matches("https?://[^/]*iplantc(ollaborative)?\\.org" //$NON-NLS-1$
                            + validUrl[0] + ".+") && !value.matches("https?://[^/]*iplantc(ollaborative)?\\.org" //$NON-NLS-1$
                                            + validUrl[1] + ".+")) { //$NON-NLS-1$
                        return I18N.DISPLAY.notValidAppWikiUrl();
                    }
                }

                return null;
            }
        }, 1024);

        urlField.setWidth(520);

        VerticalPanel panel = new VerticalPanel();
        panel.setBorders(true);
        panel.setSpacing(5);
        panel.add(urlField);

        return panel;
    }

    private void buildSubmitButton() {
        btnSubmit = new Button();

        btnSubmit.setText(I18N.DISPLAY.save());
        btnSubmit.setId("idBtnSubmit"); //$NON-NLS-1$

        btnSubmit.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                submit();
            }
        });
    }

    private void submit() {
        if (oldAppName != null) {
            ConfluenceServiceFacade.getInstance().movePage(oldAppName, appNameField.getValue(),
                    new AsyncCallback<String>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            ErrorHandler.post(caught.getMessage());
                            Services.ADMIN_APP_SERVICE.updateApplication(toJson(), closeCallback);
                        }

                        @Override
                        public void onSuccess(String result) {
                            urlField.setValue(result);
                            Services.ADMIN_APP_SERVICE.updateApplication(toJson(), closeCallback);
                        }
                    });
        } else {
            Services.ADMIN_APP_SERVICE.updateApplication(toJson(), closeCallback);
        }

    }

    private void buildCancelButton() {
        btnCancel = new Button();

        btnCancel.setText(I18N.DISPLAY.cancel());
        btnCancel.setId("idBtnCancel"); //$NON-NLS-1$
    }

    /**
     * Adds a SelectionListener to the Cancel button (e.g. for hiding parent components).
     * 
     * @param listener
     */
    public void addCancelButtonSelectionListener(SelectionListener<ButtonEvent> listener) {
        if (listener != null) {
            btnCancel.addSelectionListener(listener);
        }
    }
}
