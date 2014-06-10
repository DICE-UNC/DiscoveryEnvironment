package org.iplantc.de.diskResource.client.presenters;

import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.events.diskResources.FolderRefreshEvent;
import org.iplantc.de.client.events.diskResources.OpenFolderEvent;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.HasPath;
import org.iplantc.de.client.models.HasPaths;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.comments.Comment;
import org.iplantc.de.client.models.comments.CommentsAutoBeanFactory;
import org.iplantc.de.client.models.dataLink.DataLink;
import org.iplantc.de.client.models.dataLink.DataLinkFactory;
import org.iplantc.de.client.models.dataLink.DataLinkList;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.diskResources.DiskResourceAutoBeanFactory;
import org.iplantc.de.client.models.diskResources.DiskResourceInfo;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.diskResources.PermissionValue;
import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
import org.iplantc.de.client.models.viewer.InfoType;
import org.iplantc.de.client.services.DiskResourceServiceFacade;
import org.iplantc.de.client.util.CommonModelUtils;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.comments.presenter.CommentsPresenter;
import org.iplantc.de.commons.client.comments.view.CommentsView;
import org.iplantc.de.commons.client.comments.view.CommentsViewImpl;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.views.gxt3.dialogs.IPlantDialog;
import org.iplantc.de.commons.client.views.window.configs.TabularFileViewerWindowConfig;
import org.iplantc.de.diskResource.client.dataLink.presenter.DataLinkPresenter;
import org.iplantc.de.diskResource.client.dataLink.view.DataLinkPanel;
import org.iplantc.de.diskResource.client.events.CreateNewFileEvent;
import org.iplantc.de.diskResource.client.events.DiskResourceRenamedEvent;
import org.iplantc.de.diskResource.client.events.DiskResourceSelectionChangedEvent;
import org.iplantc.de.diskResource.client.events.DiskResourcesDeletedEvent;
import org.iplantc.de.diskResource.client.events.DiskResourcesMovedEvent;
import org.iplantc.de.diskResource.client.events.FolderCreatedEvent;
import org.iplantc.de.diskResource.client.events.FolderSelectionEvent;
import org.iplantc.de.diskResource.client.events.RequestBulkDownloadEvent;
import org.iplantc.de.diskResource.client.events.RequestBulkUploadEvent;
import org.iplantc.de.diskResource.client.events.RequestImportFromUrlEvent;
import org.iplantc.de.diskResource.client.events.RequestSendToCoGeEvent;
import org.iplantc.de.diskResource.client.events.RequestSendToEnsemblEvent;
import org.iplantc.de.diskResource.client.events.RequestSendToTreeViewerEvent;
import org.iplantc.de.diskResource.client.events.RequestSimpleDownloadEvent;
import org.iplantc.de.diskResource.client.events.RequestSimpleUploadEvent;
import org.iplantc.de.diskResource.client.events.ShowFilePreviewEvent;
import org.iplantc.de.diskResource.client.metadata.presenter.DiskResourceMetadataUpdateCallback;
import org.iplantc.de.diskResource.client.metadata.presenter.MetadataPresenter;
import org.iplantc.de.diskResource.client.metadata.view.DiskResourceMetadataView;
import org.iplantc.de.diskResource.client.presenters.callbacks.CreateFolderCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceDeleteCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceMoveCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.DiskResourceRestoreCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.GetDiskResourceDetailsCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.RenameDiskResourceCallback;
import org.iplantc.de.diskResource.client.presenters.handlers.CachedFolderTreeStoreBinding;
import org.iplantc.de.diskResource.client.presenters.handlers.DiskResourcesEventHandler;
import org.iplantc.de.diskResource.client.presenters.proxy.FolderContentsLoadConfig;
import org.iplantc.de.diskResource.client.presenters.proxy.FolderContentsRpcProxy;
import org.iplantc.de.diskResource.client.presenters.proxy.SelectDiskResourceByIdStoreAddHandler;
import org.iplantc.de.diskResource.client.presenters.proxy.SelectFolderByPathLoadHandler;
import org.iplantc.de.diskResource.client.search.events.SaveDiskResourceQueryEvent;
import org.iplantc.de.diskResource.client.search.events.SubmitDiskResourceQueryEvent;
import org.iplantc.de.diskResource.client.search.events.UpdateSavedSearchesEvent;
import org.iplantc.de.diskResource.client.search.presenter.DataSearchPresenter;
import org.iplantc.de.diskResource.client.sharing.views.DataSharingDialog;
import org.iplantc.de.diskResource.client.views.DiskResourceView;
import org.iplantc.de.diskResource.client.views.cells.events.DiskResourceNameSelectedEvent;
import org.iplantc.de.diskResource.client.views.cells.events.ManageCommentsEvent;
import org.iplantc.de.diskResource.client.views.cells.events.ManageMetadataEvent;
import org.iplantc.de.diskResource.client.views.cells.events.ManageSharingEvent;
import org.iplantc.de.diskResource.client.views.cells.events.RequestDiskResourceFavoriteEvent;
import org.iplantc.de.diskResource.client.views.cells.events.ShareByDataLinkEvent;
import org.iplantc.de.diskResource.client.views.dialogs.CreateFolderDialog;
import org.iplantc.de.diskResource.client.views.dialogs.FolderSelectDialog;
import org.iplantc.de.diskResource.client.views.dialogs.InfoTypeEditorDialog;
import org.iplantc.de.diskResource.client.views.dialogs.RenameFileDialog;
import org.iplantc.de.diskResource.client.views.dialogs.RenameFolderDialog;
import org.iplantc.de.diskResource.share.DiskResourceModule;
import org.iplantc.de.resources.client.messages.I18N;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author jstroot
 * 
 */
public class DiskResourcePresenterImpl implements DiskResourceView.Presenter, DiskResourceSelectionChangedEvent.DiskResourceSelectionChangedEventHandler {

    final DiskResourceView view;
    private final DiskResourceView.Proxy proxy;
    private final TreeLoader<Folder> treeLoader;
    private final HashMap<EventHandler, HandlerRegistration> registeredHandlers = new HashMap<EventHandler, HandlerRegistration>();
    private final List<HandlerRegistration> dreventHandlers = new ArrayList<HandlerRegistration>();
    private final DiskResourceServiceFacade diskResourceService;
    private final IplantDisplayStrings displayStrings;
    private final DiskResourceAutoBeanFactory drFactory;
    private final Builder builder;
    protected boolean isFilePreviewEnabled = true;
    private FolderContentsRpcProxy rpc_proxy;
    private PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>> gridLoader;
    private final DataLinkFactory dlFactory;
    private final UserInfo userInfo;
    private final DataSearchPresenter dataSearchPresenter;
    private final EventBus eventBus;
    private final IplantAnnouncer announcer;

    @Inject
    public DiskResourcePresenterImpl(final DiskResourceView view,
                                     final DiskResourceView.Proxy proxy,
                                     final FolderContentsRpcProxy folderRpcProxy,
                                     final DiskResourceServiceFacade diskResourceService,
                                     final IplantDisplayStrings display,
                                     final DiskResourceAutoBeanFactory factory,
                                     final DataLinkFactory dlFactory,
                                     final UserInfo userInfo,
                                     final DataSearchPresenter dataSearchPresenter,
                                     final EventBus eventBus,
                                     final IplantAnnouncer announcer) {
        this.view = view;
        this.proxy = proxy;
        this.rpc_proxy = folderRpcProxy;
        this.diskResourceService = diskResourceService;
        this.displayStrings = display;
        this.drFactory = factory;
        this.dlFactory = dlFactory;
        this.userInfo = userInfo;
        this.dataSearchPresenter = dataSearchPresenter;
        this.eventBus = eventBus;
        this.announcer = announcer;

        builder = new MyBuilder(this);

        treeLoader = new TreeLoader<Folder>(this.proxy) {
            @Override
            public boolean hasChildren(Folder parent) {
                return parent.hasSubDirs();
            }
        };

        this.proxy.init(dataSearchPresenter, this);
        this.dataSearchPresenter.searchInit(view, view, this, view.getToolbar().getSearchField());
        this.rpc_proxy.init(view.getCenterPanelHeader());

        this.view.setTreeLoader(treeLoader);
        this.view.setPresenter(this);

        gridLoader = new PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>>(rpc_proxy);
        gridLoader.useLoadConfig(new FolderContentsLoadConfig());
        gridLoader.setReuseLoadConfig(true);
        view.setViewLoader(gridLoader);

        initHandlers();
    }

    @Override
    public HandlerRegistration addDiskResourceSelectionChangedEventHandler(DiskResourceSelectionChangedEvent.DiskResourceSelectionChangedEventHandler handler) {
        return view.addDiskResourceSelectionChangedEventHandler(handler);
    }

    @Override
    public HandlerRegistration addFolderSelectedEventHandler(FolderSelectionEvent.FolderSelectionEventHandler handler) {
        return view.addFolderSelectedEventHandler(handler);
    }

    @Override
    public void onDiskResourceNameSelected(DiskResourceNameSelectedEvent event) {
        checkNotNull(event.getSelectedItem());

        if (event.getSelectedItem() instanceof Folder) {
            setSelectedFolderByPath(event.getSelectedItem());
        } else if ((event.getSelectedItem() instanceof File) && isFilePreviewEnabled) {
            eventBus.fireEvent(new ShowFilePreviewEvent((File)event.getSelectedItem(), this));
        }
    }

    @Override
    public void onDiskResourceSelectionChanged(DiskResourceSelectionChangedEvent event) {
        final List<DiskResource> selection = event.getSelection();
        if (selection != null && selection.size() == 1) {
            Iterator<DiskResource> it = selection.iterator();
            DiskResource next = it.next();
            String path = next.getPath();
            DiskResourceInfo diskResourceInfo = next.getDiskResourceInfo();
            if (diskResourceInfo == null) {
                getDetails(path);
            } else {
                view.updateDetails(path, diskResourceInfo);
            }
        } else {
            view.resetDetailsPanel();
        }
    }

    @Override
    public void onRequestShareByDataLink(ShareByDataLinkEvent event) {
        checkNotNull(event.getDiskResourceToShare());
        doShareByDataLink(event.getDiskResourceToShare());
    }

    @Override
    public void onFavoriteRequest(RequestDiskResourceFavoriteEvent event) {
        checkNotNull(event.getDiskResource());
        if(event.getDiskResource().isFavorite()) {
            //call unfavorite
        } else {
            // call favorite
        }
    }

    @Override
    public void onManageComments(ManageCommentsEvent event) {
        checkNotNull(event.getDiskResource());
        // call to retrieve comments...and show dialog
        Dialog d = new Dialog();
        d.setHeadingText(I18N.DISPLAY.comments());
        d.remove(d.getButtonBar());
        d.setSize("500px", "400px");
        CommentsView cv = new CommentsViewImpl();
        CommentsPresenter cp = new CommentsPresenter(cv);
        cv.setPresenter(cp);
        cp.loadComments(loadTestComments());
        cp.go(d);
        d.show();
    }

    private List<Comment> loadTestComments() {
        List<Comment> commentsList = new ArrayList<Comment>();
        CommentsAutoBeanFactory factory = GWT.create(CommentsAutoBeanFactory.class);
        UserInfo userInfo = UserInfo.getInstance();

        for (int i = 0; i < 10; i++) {
            Comment c = AutoBeanCodex.decode(factory, Comment.class, "{}").as();
            long time = new Date().getTime();
            c.setId(time + "" + i);
            c.setCommentText("foo bar foo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo bar" + "foo barfoo barfoo barfoo barfoo barfoo barfoo barfoo barfoo bar"
                    + "foo barfoo barfoo barfoo barfoo barfoo bar" + "foo barfoo barfoo barfoo bar");
            c.setCommentedBy(userInfo.getUsername());
            c.setTimestamp(time);
            commentsList.add(c);

        }

        return commentsList;
    }

    void doShareByDataLink(final DiskResource toBeShared) {
        if (toBeShared instanceof Folder) {
            showShareLink(GWT.getHostPageBaseURL() + "?type=data&folder=" + toBeShared.getId());
        } else {
            diskResourceService.createDataLinks(Arrays.asList(toBeShared.getPath()), new AsyncCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    AutoBean<DataLinkList> tickets = AutoBeanCodex.decode(dlFactory, DataLinkList.class, result);
                    List<DataLink> dlList = tickets.as().getTickets();
                    showShareLink(dlList.get(0).getDownloadUrl());
                }

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(I18N.ERROR.createDataLinksError(), caught);
                }
            });
        }
    }

    /**
     * Method called by the view when a folder is selected.
     * Whenever this method is called with a non-null and non-empty list, the presenter will have the
     * view de-select all disk resources
     * in the center panel.
     * 
     * @param folder the selected folder
     */
    void onFolderSelected(final Folder folder) {
        view.showDataListingWidget();
        view.deSelectDiskResources();
        FolderContentsLoadConfig config = gridLoader.getLastLoadConfig();
        config.setFolder(folder);
        gridLoader.load(0, 200);
    }

    @Override
    public void onFolderSelected(FolderSelectionEvent event) {
        Folder selectedFolder = event.getSelectedFolder();
        if (selectedFolder instanceof DiskResourceQueryTemplate) {
            // If the given query has not been saved, we need to deselect everything
            DiskResourceQueryTemplate searchQuery = (DiskResourceQueryTemplate)selectedFolder;
            if (!searchQuery.isSaved()) {
                deSelectDiskResources();
                getView().deSelectNavigationFolder();
            }
            view.setAllowSelectAll(false);
        } else {
            view.setAllowSelectAll(true);
        }
        onFolderSelected(selectedFolder);
    }

    @Override
    public void onRequestManageMetadata(ManageMetadataEvent event) {
        DiskResource selected = event.getDiskResource();
        final DiskResourceMetadataView mview = new DiskResourceMetadataView(selected);
        final DiskResourceMetadataView.Presenter p = new MetadataPresenter(selected, mview);
        final IPlantDialog ipd = new IPlantDialog(true);

        ipd.setSize("600", "400"); //$NON-NLS-1$ //$NON-NLS-2$
        ipd.setHeadingText(displayStrings.metadata() + ":" + selected.getId()); //$NON-NLS-1$
        ipd.setResizable(true);
        ipd.addHelp(new HTML(I18N.HELP.metadataHelp()));
        p.go(ipd);
        if (DiskResourceUtil.isWritable(selected)) {
            ipd.setHideOnButtonClick(false);
            ipd.addOkButtonSelectHandler(new SelectHandler() {
                @Override
                public void onSelect(SelectEvent event) {
                    if (mview.shouldValidate()) {
                        if (mview.isValid()) {
                            p.setDiskResourceMetaData(mview.getMetadataToAdd(), mview.getMetadataToDelete(), new DiskResourceMetadataUpdateCallback());
                            ipd.hide();
                        } else {
                            IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(I18N.ERROR.metadataFormInvalid()));
                        }
                    } else {
                        p.setDiskResourceMetaData(mview.getMetadataToAdd(), mview.getMetadataToDelete(), new DiskResourceMetadataUpdateCallback());
                        ipd.hide();
                    }
                }
            });

            ipd.addCancelButtonSelectHandler(new SelectHandler() {

                @Override
                public void onSelect(SelectEvent event) {
                    ipd.hide();
                }
            });
        }

        ipd.show();

    }

    @Override
    public void onRequestManageSharing(ManageSharingEvent event) {
        checkNotNull(event.getDiskResourceToShare());
        doShareWithCollaborators(Lists.newArrayList(event.getDiskResourceToShare()));
    }

    private void showShareLink(String linkId) {
        // Open dialog window with text selected.
        IPlantDialog dlg = new IPlantDialog();
        dlg.setHeadingText(displayStrings.copy());
        dlg.setHideOnButtonClick(true);
        dlg.setResizable(false);
        dlg.setSize("535", "130");
        TextField textBox = new TextField();
        textBox.setWidth(500);
        textBox.setReadOnly(true);
        textBox.setValue(linkId);
        VerticalLayoutContainer container = new VerticalLayoutContainer();
        dlg.setWidget(container);
        container.add(textBox);
        container.add(new Label(displayStrings.copyPasteInstructions()));
        dlg.setFocusWidget(textBox);
        dlg.show();
        textBox.selectAll();
    }

    private void initHandlers() {
        treeLoader.addLoadHandler(new CachedFolderTreeStoreBinding(view.getTreeStore()));

        DiskResourcesEventHandler diskResourcesEventHandler = new DiskResourcesEventHandler(this);
        dreventHandlers.add(eventBus.addHandler(FolderRefreshEvent.TYPE, diskResourcesEventHandler));
        dreventHandlers.add(eventBus.addHandler(DiskResourcesDeletedEvent.TYPE, diskResourcesEventHandler));
        dreventHandlers.add(eventBus.addHandler(FolderCreatedEvent.TYPE, diskResourcesEventHandler));
        dreventHandlers.add(eventBus.addHandler(DiskResourceRenamedEvent.TYPE, diskResourcesEventHandler));
        dreventHandlers.add(eventBus.addHandler(DiskResourcesMovedEvent.TYPE, diskResourcesEventHandler));
        dreventHandlers.add(eventBus.addHandler(UpdateSavedSearchesEvent.TYPE, diskResourcesEventHandler));
    }

    @Override
    public void cleanUp() {
        for (HandlerRegistration hr : dreventHandlers) {
            eventBus.removeHandler(hr);
        }
    }

    @Override
    public DiskResourceView getView() {
        return view;
    }

    @Override
    public void go(HasOneWidget container) {
        container.setWidget(view);
        // JDS Re-select currently selected folder in order to load center
        // panel.
        setSelectedFolderByPath(getSelectedFolder());
    }

    @Override
    public void disableFilePreview() {
        isFilePreviewEnabled = false;
    }

    @Override
    public void go(HasOneWidget container, HasPath folderToSelect, final List<? extends HasId> diskResourcesToSelect) {

        if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getPath())) {
            go(container);
        } else {
            container.setWidget(view);
            setSelectedFolderByPath(folderToSelect);
            setSelectedDiskResourcesById(diskResourcesToSelect);
        }
    }

    @Override
    public void setSelectedDiskResourcesById(final List<? extends HasId> diskResourcesToSelect) {
        SelectDiskResourceByIdStoreAddHandler diskResourceStoreAddHandler = new SelectDiskResourceByIdStoreAddHandler(diskResourcesToSelect, this);
        HandlerRegistration diskResHandlerReg = view.getListStore().addStoreAddHandler(diskResourceStoreAddHandler);
        addEventHandlerRegistration(diskResourceStoreAddHandler, diskResHandlerReg);
    }

    @Override
    public void setSelectedFolderByPath(final HasPath folderToSelect) {
        if ((folderToSelect == null) || Strings.isNullOrEmpty(folderToSelect.getPath())) {
            return;
        }

        Folder folder = view.getFolderByPath(folderToSelect.getPath());
        if (folder != null) {
            final Folder selectedFolder = view.getSelectedFolder();
            if (folder == selectedFolder) {
                // If the folder IS the currently selected folder, then trigger
                // reload of center panel.
                onFolderSelected(folder);
            } else {
                /*
                 * If it is NOT the currently selected folder, then deselect the
                 * current folder and re-select it.
                 */
                view.deSelectNavigationFolder();
                view.setSelectedFolder(folder);
            }
        } else {
            // Create and add the SelectFolderByIdLoadHandler to the treeLoader.
            final SelectFolderByPathLoadHandler handler = new SelectFolderByPathLoadHandler(folderToSelect, this, announcer);
            /*
             * Only add handler if no root items have been loaded, or the folderToSelect has a common
             * root with the treestore.
             */
            if (view.getTreeStore().getRootCount() < 1 || handler.isRootFolderDetected()) {
                addEventHandlerRegistration(handler, treeLoader.addLoadHandler(handler));
            }
        }
    }

    @Override
    public Folder getSelectedFolder() {
        return view.getSelectedFolder();
    }

    @Override
    public Set<DiskResource> getSelectedDiskResources() {
        return view.getSelectedDiskResources();
    }

    private void getDetails(String path) {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        arr.set(0, new JSONString(path));
        obj.put("paths", arr); //$NON-NLS-1$
        diskResourceService.getStat(obj.toString(), new GetDiskResourceDetailsCallback(this, path, drFactory));
        view.maskSendToCoGe();
        view.maskSendToEnsembl();
        view.maskSendToTreeViewer();
    }

    @Override
    public void doBulkUpload() {
        eventBus.fireEvent(new RequestBulkUploadEvent(this, getSelectedUploadFolder()));
    }

    @Override
    public void doSimpleUpload() {
        eventBus.fireEvent(new RequestSimpleUploadEvent(this, getSelectedUploadFolder()));
    }

    @Override
    public void editSelectedFile() {


        checkState(getSelectedDiskResources().size() == 1, "Only one file should be selected, but there are %i", getSelectedDiskResources().size());
        final DiskResource next = getSelectedDiskResources().iterator().next();
        checkState(next instanceof File, "Selected item should be a file, but is not.");
        checkState(PermissionValue.own.equals(next.getPermission())
                          || PermissionValue.write.equals(next), "User should have either own or write permissions for the selected item");

        eventBus.fireEvent(new ShowFilePreviewEvent((File) next, this));
    }

    @Override
    public void editSelectedResourceInfoType() {
        checkState(getSelectedDiskResources().size() == 1, "Only one Disk Resource should be selected, but there are %i", getSelectedDiskResources().size());
        onInfoTypeClick(getSelectedDiskResources().iterator().next(), "");
    }

    @Override
    public void doImportFromUrl() {
        eventBus.fireEvent(new RequestImportFromUrlEvent(this, getSelectedUploadFolder()));
    }

    private Folder getSelectedUploadFolder() {
        Folder selectedFolder = getSelectedFolder();

        if (selectedFolder == null) {
            return view.getFolderByPath(UserInfo.getInstance().getHomePath());
        }

        return selectedFolder;
    }

    @Override
    public void openNewWindow(boolean atThisLocation) {
        // If current folder is null, or window SHOULD NOT be opened at current location, folderPath is null
        String folderPath = (getSelectedFolder() == null) || !atThisLocation ? null : getSelectedFolder().getPath();
        OpenFolderEvent openEvent = new OpenFolderEvent(folderPath);
        openEvent.requestNewView(true);
        eventBus.fireEvent(openEvent);
    }

    @Override
    public void refreshSelectedFolder() {
        checkState(getSelectedFolder() != null, "Selected folder should no be null");
        doRefreshFolder(getSelectedFolder());
    }

    void onFolderRefresh(Folder folder) {
        if (folder == null || Strings.isNullOrEmpty(folder.getId())) {
            dataSearchPresenter.refreshQuery();
            return;
        }

        eventBus.fireEvent(new FolderRefreshEvent(folder));
    }

    @Override
    public void renameSelectedResource() {
        if (!getSelectedDiskResources().isEmpty() && (getSelectedDiskResources().size() == 1)) {
            DiskResource dr = getSelectedDiskResources().iterator().next();
            if (dr instanceof File) {
                RenameFileDialog dlg = new RenameFileDialog((File)dr, this);
                dlg.show();

            } else {
                RenameFolderDialog dlg = new RenameFolderDialog((Folder)dr, this);
                dlg.show();

            }
        } else if (getSelectedFolder() != null) {
            RenameFolderDialog dlg = new RenameFolderDialog(getSelectedFolder(), this);
            dlg.show();
        }
    }

    @Override
    public void restoreSelectedResources() {
        final Set<DiskResource> selectedResources = getSelectedDiskResources();

        if (selectedResources == null || selectedResources.isEmpty()) {
            return;
        }

        mask(""); //$NON-NLS-1$

        DiskResourceRestoreCallback callback = new DiskResourceRestoreCallback(this, drFactory, selectedResources);
        if (view.isSelectAll()) {
            diskResourceService.restoreAll(callback);
        } else {
            HasPaths request = drFactory.pathsList().as();
            request.setPaths(DiskResourceUtil.asStringIdList(selectedResources));
            diskResourceService.restoreDiskResource(request, callback);
        }
    }

    @Override
    public void selectTrashFolder() {
        final HasPath hasPath = CommonModelUtils.createHasPathFromString(userInfo.getTrashPath());
        setSelectedFolderByPath(hasPath);
    }

    @Override
    public void sendSelectedResourceToEnsembl() {
        final Set<DiskResource> selection = view.getSelectedDiskResources();
        Iterator<DiskResource> it = selection.iterator();
        DiskResource next = it.next();
        String infoType = getInfoType(next);
        if(Strings.isNullOrEmpty(infoType)) {
            showInfoTypeError(I18N.ERROR.unsupportedEnsemblInfoType());
            return;
        }
        JSONObject obj = new JSONObject();
        obj.put("info-type", new JSONString(infoType));
        if (DiskResourceUtil.isEnsemblVizTab(obj)) {
            eventBus.fireEvent(new RequestSendToEnsemblEvent((File)next, InfoType.fromTypeString(infoType)));
        } else {
            showInfoTypeError(I18N.ERROR.unsupportedEnsemblInfoType());
        }
    }

    @Override
    public void sendSelectedResourcesToCoge() {
        final Set<DiskResource> selection = view.getSelectedDiskResources();
        Iterator<DiskResource> it = selection.iterator();
        DiskResource next = it.next();
        String infoType = getInfoType(next);
        if (Strings.isNullOrEmpty(infoType)) {
            showInfoTypeError(I18N.ERROR.unsupportedCogeInfoType());
            return;
        }
        JSONObject obj = new JSONObject();
        obj.put("info-type", new JSONString(infoType));
        if (DiskResourceUtil.isGenomeVizTab(obj)) {
            eventBus.fireEvent(new RequestSendToCoGeEvent((File)next));
        } else {
            showInfoTypeError(I18N.ERROR.unsupportedCogeInfoType());
        }
    }

    @Override
    public void sendSelectedResourcesToTreeViewer() {
        final Set<DiskResource> selection = view.getSelectedDiskResources();
        Iterator<DiskResource> it = selection.iterator();
        DiskResource next = it.next();
        String infoType = getInfoType(next);
        if (Strings.isNullOrEmpty(infoType)) {
            showInfoTypeError(I18N.ERROR.unsupportedTreeInfoType());
            return;
        }
        JSONObject obj = new JSONObject();
        obj.put("info-type", new JSONString(infoType));
        if (DiskResourceUtil.isTreeTab(obj)) {
            eventBus.fireEvent(new RequestSendToTreeViewerEvent((File)next));
        } else {
            showInfoTypeError(I18N.ERROR.unsupportedTreeInfoType());
        }
    }

    private void showInfoTypeError(String msg) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscaped(msg);
        announcer.schedule(new ErrorAnnouncementConfig(builder.toSafeHtml(), true, 4000));
    }


    private String getInfoType(DiskResource dr) {
        DiskResourceInfo diskResourceInfo = dr.getDiskResourceInfo();
        if (diskResourceInfo == null) {
            ErrorHandler.post("Unable to retrieve information type");
        }
        return diskResourceInfo.getInfoType();
    }

    @Override
    public void setViewDebugId(String baseID) {
        view.asWidget().ensureDebugId(baseID + DiskResourceModule.Ids.DISK_RESOURCE_VIEW);
    }

    @Override
    public void createNewFolder() {
        CreateFolderDialog dlg = new CreateFolderDialog(getSelectedUploadFolder(), this);
        dlg.show();
    }

    @Override
    public void createNewPlainTextFile() {
        CreateNewFileEvent event = new CreateNewFileEvent(getSelectedUploadFolder());
        eventBus.fireEvent(event);
    }

    @Override
    public void createNewTabFile(TabularFileViewerWindowConfig config) {
        CreateNewFileEvent event = new CreateNewFileEvent(getSelectedUploadFolder(), config);
        eventBus.fireEvent(event);
    }

    @Override
    public void doCreateNewFolder(Folder parentFolder, final String newFolderName) {
        view.mask(displayStrings.loadingMask());
        diskResourceService.createFolder(parentFolder, newFolderName, new CreateFolderCallback(parentFolder, view));
    }

    @Override
    public void doRefreshFolder(Folder folder) {
        folder = view.getFolderById(folder.getId());
        if (folder == null) {
            return;
        }

        Folder selectedFolder = getSelectedFolder();
        view.refreshFolder(folder);

        if (!(folder instanceof DiskResourceQueryTemplate) && selectedFolder == folder) {
            // If the folder is currently selected, then trigger reload of
            // center panel.
            onFolderSelected(folder);
        }

    }

    @Override
    public void doSimpleDownload() {
        eventBus.fireEvent(new RequestSimpleDownloadEvent(this, getSelectedDiskResources(), getSelectedFolder()));
    }

    @Override
    public void doBulkDownload() {
        eventBus.fireEvent(new RequestBulkDownloadEvent(this, view.isSelectAll(), getSelectedDiskResources(), getSelectedFolder()));
    }

    @Override
    public void doRenameDiskResource(final DiskResource dr, final String newName) {
        if (dr != null && !dr.getName().equals(newName)) {
            view.mask(displayStrings.loadingMask());
            diskResourceService.renameDiskResource(dr, newName, new RenameDiskResourceCallback(dr, view));
        }
    }

    @Override
    public void manageSelectedResourceCollaboratorSharing() {
        doShareWithCollaborators(getSelectedDiskResources());
    }

    private void doShareWithCollaborators(final Iterable<DiskResource> resourcesToBeShared){
        DataSharingDialog dlg = new DataSharingDialog(Sets.newHashSet(resourcesToBeShared));
        dlg.show();
        dlg.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent event) {
                final Set<DiskResource> selection = view.getSelectedDiskResources();
                if (selection != null && selection.size() == 1) {
                    Iterator<DiskResource> it = selection.iterator();
                    DiskResource next = it.next();
                    String path = next.getPath();
                    getDetails(path);
                }
            }
        });
   }

    @Override
    public void deleteSelectedResources() {
        Set<DiskResource> selectedResources = getSelectedDiskResources();
        if (!selectedResources.isEmpty() && DiskResourceUtil.isOwner(selectedResources)) {
            HashSet<DiskResource> drSet = Sets.newHashSet(selectedResources);

            if (DiskResourceUtil.containsTrashedResource(drSet)) {
                confirmDelete(drSet);
            } else {
                delete(drSet, displayStrings.deleteMsg());
            }
        }
    }

    private void confirmDelete(final Set<DiskResource> drSet) {
        final MessageBox confirm = new ConfirmMessageBox(displayStrings.warning(), displayStrings.emptyTrashWarning());

        confirm.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent event) {
                if (confirm.getHideButton() == confirm.getButtonById(PredefinedButton.YES.name())) {
                    delete(drSet, displayStrings.deleteTrash());
                }
            }
        });

        confirm.show();
    }

    private void delete(Set<DiskResource> drSet, String announce) {
        view.mask(displayStrings.loadingMask());
        Folder selectedFolder = getSelectedFolder();
        final AsyncCallback<HasPaths> callback = new DiskResourceDeleteCallback(drSet, selectedFolder, view, announce);

        if (view.isSelectAll() && selectedFolder != null) {
            diskResourceService.deleteContents(selectedFolder.getPath(), callback);

        } else {
            diskResourceService.deleteDiskResources(drSet, callback);
        }
    }

    @Override
    public void manageSelectedResourceMetadata() {
        DiskResource selected = getSelectedDiskResources().iterator().next();
        final DiskResourceMetadataView mview = new DiskResourceMetadataView(selected);
        final DiskResourceMetadataView.Presenter p = new MetadataPresenter(selected, mview);
        final IPlantDialog ipd = new IPlantDialog(true);

        ipd.setSize("600", "400"); //$NON-NLS-1$ //$NON-NLS-2$
        ipd.setHeadingText(I18N.DISPLAY.metadata() + ":" + selected.getId()); //$NON-NLS-1$
        ipd.setResizable(true);
        ipd.addHelp(new HTML(I18N.HELP.metadataHelp()));
        p.go(ipd);
        if (DiskResourceUtil.isWritable(selected)) {
            ipd.setHideOnButtonClick(false);
            ipd.addOkButtonSelectHandler(new SelectHandler() {
                @Override
                public void onSelect(SelectEvent event) {
                    if (mview.shouldValidate()) {
                        if (mview.isValid()) {
                            p.setDiskResourceMetaData(mview.getMetadataToAdd(), mview.getMetadataToDelete(), new DiskResourceMetadataUpdateCallback());
                            ipd.hide();
                        } else {
                            IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig(I18N.ERROR.metadataFormInvalid()));
                        }
                    } else {
                        p.setDiskResourceMetaData(mview.getMetadataToAdd(), mview.getMetadataToDelete(), new DiskResourceMetadataUpdateCallback());
                        ipd.hide();
                    }
                }
            });

            ipd.addCancelButtonSelectHandler(new SelectHandler() {

                @Override
                public void onSelect(SelectEvent event) {
                    ipd.hide();
                }
            });
        }

        ipd.show();

    }

    @Override
    public void unregisterHandler(EventHandler handler) {
        if (registeredHandlers.containsKey(handler)) {
            registeredHandlers.remove(handler).removeHandler();
        }
    }

    @Override
    public void addEventHandlerRegistration(EventHandler handler, HandlerRegistration reg) {
        registeredHandlers.put(handler, reg);
    }

    @Override
    public boolean canDragDataToTargetFolder(final Folder targetFolder, final Collection<DiskResource> dropData) {
        if (targetFolder instanceof DiskResourceQueryTemplate) {
            return false;
        }

        if (targetFolder.isFilter()) {
            return false;
        }

        // Assuming that ownership is of no concern.
        for (DiskResource dr : dropData) {
            // if the resource is a direct child of target folder
            if (DiskResourceUtil.isChildOfFolder(targetFolder, dr)) {
                return false;
            }

            if (dr instanceof Folder) {
                if (targetFolder.getPath().equals(dr.getPath())) {
                    return false;
                }

                // cannot drag an ancestor (parent, grandparent, etc) onto a
                // child and/or descendant
                if (DiskResourceUtil.isDescendantOfFolder((Folder)dr, targetFolder)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void doMoveDiskResources(Folder targetFolder, Set<DiskResource> resources) {
        Folder parent = getSelectedFolder();
        view.mask(I18N.DISPLAY.loadingMask());
        if (view.isSelectAll()) {
            diskResourceService.moveContents(parent.getPath(), targetFolder, new DiskResourceMoveCallback(view, true, parent, targetFolder, resources));
        } else {
            diskResourceService.moveDiskResources(resources, targetFolder, new DiskResourceMoveCallback(view, false, parent, targetFolder, resources));
        }
    }

    @Override
    public Set<? extends DiskResource> getDragSources(IsWidget source, Element dragStartEl) {
        // Verify the drag started from a valid item in the tree or grid, then
        // return the selected items.
        if (view.isViewGrid(source)) {
            Set<DiskResource> selectedResources = getSelectedDiskResources();

            if (!selectedResources.isEmpty()) {
                // Verify the dragStartEl is a row within the grid.
                Element targetRow = view.findGridRow(dragStartEl);

                if (targetRow != null) {
                    int dropIndex = view.findRowIndex(targetRow);

                    DiskResource selDiskResource = view.getListStore().get(dropIndex);
                    if (selDiskResource != null) {
                        return Sets.newHashSet(selectedResources);
                    }
                }
            }
        } else if (view.isViewTree(source) && (getSelectedFolder() != null)) {
            // Verify the dragStartEl is a folder within the tree.
            Folder srcFolder = getDropTargetFolder(source, dragStartEl);

            if (srcFolder != null) {
                return Sets.newHashSet(srcFolder);
            }
        }

        return null;
    }

    @Override
    public Folder getDropTargetFolder(IsWidget target, Element eventTargetElement) {
        Folder ret = null;
        if (view.isViewTree(target) && (view.findTreeNode(eventTargetElement) != null)) {
            TreeNode<Folder> targetTreeNode = view.findTreeNode(eventTargetElement);
            ret = targetTreeNode.getModel();
        } else if (view.isViewGrid(target)) {
            Element targetRow = view.findGridRow(eventTargetElement).cast();

            if (targetRow == null) {
                ret = getSelectedUploadFolder();
            } else {
                int dropIndex = view.findRowIndex(targetRow);

                DiskResource selDiskResource = view.getListStore().get(dropIndex);
                ret = (selDiskResource instanceof Folder) ? (Folder)selDiskResource : null;
            }
        }
        return ret;
    }

    @Override
    public Builder builder() {
        return builder;
    }

    @Override
    public void doSaveDiskResourceQueryTemplate(SaveDiskResourceQueryEvent event) {

    }

    @Override
    public void doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent event) {}

    private class MyBuilder implements Builder {

        private final DiskResourceView.Presenter presenter;

        public MyBuilder(DiskResourceView.Presenter presenter) {
            this.presenter = presenter;
        }

        @Override
        public void go(HasOneWidget container) {
            presenter.go(container);
        }

        @Override
        public Builder hideNorth() {
            presenter.getView().setNorthWidgetHidden(true);
            return this;
        }

        @Override
        public Builder hideWest() {
            presenter.getView().setWestWidgetHidden(true);
            return this;
        }

        @Override
        public Builder hideCenter() {
            presenter.getView().setCenterWidgetHidden(true);
            return this;
        }

        @Override
        public Builder hideEast() {
            presenter.getView().setEastWidgetHidden(true);
            return this;
        }

        @Override
        public Builder singleSelect() {
            presenter.getView().setSingleSelect();
            return this;
        }

        @Override
        public Builder disableFilePreview() {
            presenter.disableFilePreview();
            return this;
        }

    }

    @Override
    public void deSelectDiskResources() {
        view.deSelectDiskResources();
    }

    void doEmptyTrash() {
        view.mask(displayStrings.loadingMask());
        diskResourceService.emptyTrash(UserInfo.getInstance().getUsername(), new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                doRefreshFolder(view.getFolderByPath(UserInfo.getInstance().getTrashPath()));
                view.unmask();
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
                view.unmask();
            }
        });
    }

    @Override
    public void emptyTrash() {

        // TODO CORE-5300 Move confirmation box to view, which will call presenter
        final ConfirmMessageBox cmb = new ConfirmMessageBox(I18N.DISPLAY.emptyTrash(), I18N.DISPLAY.emptyTrashWarning());
        cmb.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent event) {
                if (cmb.getHideButton() == cmb.getButtonById(PredefinedButton.YES.name())) {
                    doEmptyTrash();
                }
            }
        });

        cmb.setWidth(300);
        cmb.show();
    }

    @Override
    public void manageSelectedResourceDataLinks() {
        IPlantDialog dlg = new IPlantDialog(true);
        dlg.setHeadingText(I18N.DISPLAY.manageDataLinks());
        dlg.setHideOnButtonClick(true);
        dlg.setWidth(550);
        dlg.setOkButtonText(I18N.DISPLAY.done());
        DataLinkPanel.Presenter<DiskResource> dlPresenter = new DataLinkPresenter<DiskResource>(new ArrayList<DiskResource>(getSelectedDiskResources()));
        dlPresenter.go(dlg);
        dlg.addHelp(new HTML(I18N.HELP.manageDataLinksHelp()));
        dlg.show();
    }

    @Override
    public void onInfoTypeClick(final DiskResource dr, final String type) {
        final InfoTypeEditorDialog dialog = new InfoTypeEditorDialog(type);
        dialog.show();
        dialog.addOkButtonSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                String newType = dialog.getSelectedValue();
                setInfoType(dr.getId(), newType);
            }
        });

    }

    @Override
    public void moveSelectedDiskResources() {
        final FolderSelectDialog fsd = new FolderSelectDialog();
        fsd.show();
        fsd.addOkButtonSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                Folder targetFolder = fsd.getValue();
                final Set<DiskResource> selectedResources = getSelectedDiskResources();
                if (DiskResourceUtil.isMovable(targetFolder, selectedResources)) {
                    if (canDragDataToTargetFolder(targetFolder, selectedResources)) {
                        doMoveDiskResources(targetFolder, selectedResources);
                    } else {
                        announcer.schedule(new ErrorAnnouncementConfig(I18N.ERROR.diskResourceIncompleteMove()));
                        view.unmask();
                    }
                } else {
                    announcer.schedule(new ErrorAnnouncementConfig(I18N.ERROR.permissionErrorMessage()));
                    view.unmask();
                }
            }
        });

    }

    @Override
    public void moveSelectedDiskResourcesToTrash() {

        checkState(!getSelectedDiskResources().isEmpty(), "Selected resources should not be empty");
        delete(getSelectedDiskResources(), displayStrings.deleteMsg());
    }

    @Override
    public void mask(String loadingMask) {
        view.mask((Strings.isNullOrEmpty(loadingMask)) ? displayStrings.loadingMask() : loadingMask);
    }

    @Override
    public void unmask() {
        boolean hasLoadHandlers = false;
        for (Entry<EventHandler, HandlerRegistration> entry : registeredHandlers.entrySet()) {
            if (entry.getKey() instanceof LoadHandler<?, ?>) {
                hasLoadHandlers = true;
            }

        }
        if (!hasLoadHandlers) {
            view.unmask();
        }

    }

    @Override
    public void resetInfoType() {
        if (getSelectedDiskResources().size() > 0) {
            Iterator<DiskResource> it = getSelectedDiskResources().iterator();
            if (it != null && it.hasNext()) {
                setInfoType(it.next().getPath(), "");
            }
        }
    }

    private void setInfoType(final String id, String newType) {
        diskResourceService.setFileType(id, newType, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable arg0) {
                ErrorHandler.post(arg0);
            }

            @Override
            public void onSuccess(String arg0) {
                getDetails(id);
            }
        });
    }

    @Override
    public void displayAndCacheDiskResourceInfo(String path, DiskResourceInfo info) {
        if (info == null) {
            return;
        }

        view.displayAndCacheDiskResourceInfo(path, info);
    }

    @Override
    public void unmaskVizMenuOptions() {
        view.unmaskSendToCoGe();
        view.unmaskSendToEnsembl();
        view.unmaskSendToTreeViewer();
    }

    @Override
    public void shareSelectedFolderByDataLink() {
        checkState(getSelectedDiskResources().size() == 1, "Selected resources should only contain 1 item, but contains %i", getSelectedDiskResources().size());

        doShareByDataLink(Iterables.getFirst(getSelectedDiskResources(), null));
    }

}
