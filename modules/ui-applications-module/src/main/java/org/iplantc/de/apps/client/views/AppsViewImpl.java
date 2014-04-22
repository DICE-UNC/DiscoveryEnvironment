package org.iplantc.de.apps.client.views;

import org.iplantc.de.apps.client.events.AppFavoritedEvent;
import org.iplantc.de.apps.client.events.AppGroupSelectionChangedEvent;
import org.iplantc.de.apps.client.events.AppSelectionChangedEvent;
import org.iplantc.de.apps.client.views.cells.AppInfoCell;
import org.iplantc.de.apps.client.views.dialogs.SubmitAppForPublicDialog;
import org.iplantc.de.apps.client.views.widgets.events.AppSearchResultLoadEvent;
import org.iplantc.de.client.models.DEProperties;
import org.iplantc.de.client.models.IsMaskable;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppGroup;
import org.iplantc.de.client.services.AppUserServiceFacade;
import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.resources.client.IplantResources;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static com.sencha.gxt.core.client.Style.SelectionMode.SINGLE;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.theme.gray.client.panel.GrayContentPanelAppearance;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
import com.sencha.gxt.widget.core.client.tree.TreeStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author jstroot
 *
 */
public class AppsViewImpl implements AppsView, IsMaskable, AppGroupSelectionChangedEvent.HasAppGroupSelectionChangedEventHandlers, AppSelectionChangedEvent.HasAppSelectionChangedEventHandlers, AppInfoCell.AppInfoClickedEventHandler, AppFavoritedEvent.AppFavoritedEventHandler {
    /**
     * FIXME CORE-2992: Add an ID to the Categories panel collapse tool to assist QA.
     */
    private static String WEST_COLLAPSE_BTN_ID = "idCategoryCollapseBtn"; //$NON-NLS-1$
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("AppsView.ui.xml")
    interface MyUiBinder extends UiBinder<Widget, AppsViewImpl> {
    }

    private String FAVORITES;
    private String USER_APPS_GROUP;
    private String WORKSPACE;

    private Presenter presenter;

    @UiField(provided = true)
    protected Tree<AppGroup, String> tree;

    @UiField(provided = true)
    TreeStore<AppGroup> treeStore;

    @UiField
    protected Grid<App> grid;

    @UiField
    GridView<App> gridView;

    @UiField
    ListStore<App> listStore;

    @UiField(provided = true)
    protected ColumnModel<App> cm;

    @UiField(provided = true)
    BorderLayoutContainer con;

    @UiField
    ContentPanel westPanel;
    @UiField
    ContentPanel centerPanel;
    @UiField
    ContentPanel eastPanel;

    @UiField
    BorderLayoutData northData;
    @UiField
    BorderLayoutData eastData;

    private final Widget widget;

    final DEProperties properties;
    private final AppsView.ViewMenu toolbar;
    private final IplantResources resources;
    private final IplantDisplayStrings displayStrings;
    private final AppUserServiceFacade appUserService;

    Logger logger = Logger.getLogger("App View");

    @Inject
    public AppsViewImpl(final Tree<AppGroup, String> tree,
                        final DEProperties properties,
                        final AppsView.ViewMenu toolbar,
                        final IplantResources resources,
                        final IplantDisplayStrings displayStrings,
                        final AppUserServiceFacade appUserService) {
        this.tree = tree;
        this.properties = properties;
        this.toolbar = toolbar;
        final AppColumnModel appColumnModel = new AppColumnModel(this, displayStrings);
        this.cm = appColumnModel;
        this.resources = resources;
        this.displayStrings = displayStrings;
        this.appUserService = appUserService;
        this.treeStore = tree.getStore();
        this.widget = uiBinder.createAndBindUi(this);
        initConstants();

        this.tree.getSelectionModel().setSelectionMode(SINGLE);
        initTreeStoreSorter();


        grid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<App>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<App> event) {
                asWidget().fireEvent(new AppSelectionChangedEvent(event.getSelection()));
            }
        });

        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedHandler<AppGroup>() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent<AppGroup> event) {
                updateCenterPanelHeading(event.getSelection());
                asWidget().fireEvent(new AppGroupSelectionChangedEvent(event.getSelection()));
            }
        });
        setTreeIcons();
        new QuickTip(grid).getToolTipConfig().setTrackMouse(true);
        westPanel.getHeader().getTool(0).getElement().setId(WEST_COLLAPSE_BTN_ID);

    }
    private void initConstants() {
        WORKSPACE = properties.getPrivateWorkspace();

        if (properties.getPrivateWorkspaceItems() != null) {
            JSONArray items = JSONParser.parseStrict(properties.getPrivateWorkspaceItems()).isArray();
            USER_APPS_GROUP = JsonUtil.getRawValueAsString(items.get(0));
            FAVORITES = JsonUtil.getRawValueAsString(items.get(1));
        }

    }

    @Override
    public void onAppFavorited(AppFavoritedEvent event) {
        final AppGroup appGroupByName = findAppGroupByName(FAVORITES);
        if(appGroupByName != null){
            int tmp = event.isFavorite() ? 1 : -1;

            updateAppGroupAppCount(appGroupByName, appGroupByName.getAppCount() + tmp);
        }
        final String name = getSelectedAppGroup().getName();
        if(name.equalsIgnoreCase(WORKSPACE) || name.equalsIgnoreCase(FAVORITES)){
            removeApp(findApp(event.getAppId()));
        }
    }

    @Override
    public void onAppInfoClicked(AppInfoCell.AppInfoClickedEvent event) {
        final App selectedApp = grid.getSelectionModel().getSelectedItem();
        Dialog appInfoWin = new Dialog();
        appInfoWin.setModal(true);
        appInfoWin.setResizable(false);
        appInfoWin.setHeadingText(selectedApp.getName());
        appInfoWin.setPixelSize(450, 300);
        appInfoWin.add(new AppInfoView(selectedApp, this, appUserService));
        appInfoWin.getButtonBar().clear();
        appInfoWin.show();
    }

    @Override
    public void onAppSearchResultLoad(AppSearchResultLoadEvent event) {
        String searchText = event.getSearchText();

        List<App> results = event.getResults();
        int total = results == null ?0 : results.size();

        selectAppGroup(null);
        centerPanel.setHeadingText(displayStrings.searchAppResultsHeader(searchText, total));
        setApps(results);
        unMaskCenterPanel();


    }

    void updateCenterPanelHeading(List<AppGroup> selection) {
        checkArgument(selection.size() == 1, "Only one app group should be selected");
        centerPanel.setHeadingText(Joiner.on(" >> ").join(computeGroupHierarchy(selection.get(0))));
    }

    @Override
    public HandlerRegistration addAppGroupSelectedEventHandler(AppGroupSelectionChangedEvent.AppGroupSelectionChangedEventHandler handler) {
        return asWidget().addHandler(handler, AppGroupSelectionChangedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addAppSelectionChangedEventHandler(AppSelectionChangedEvent.AppSelectionChangedEventHandler handler) {
        return asWidget().addHandler(handler, AppSelectionChangedEvent.TYPE);
    }

    @UiFactory
    ContentPanel createContentPanel() {
        // FIXME JDS This violates goal of theming. Implement proper theming/appearance.
        return new ContentPanel(new GrayContentPanelAppearance());
    }

    @UiFactory
    ListStore<App> createListStore() {
        return new ListStore<App>(new ModelKeyProvider<App>() {
            @Override
            public String getKey(App item) {
                return item.getId();
            }

        });
    }

    /**
     * FIXME JDS This needs to be implemented in an {@link TreeAppearance}
     */
    private void setTreeIcons() {
        TreeStyle style = tree.getStyle();
        style.setNodeCloseIcon(resources.category());
        style.setNodeOpenIcon(resources.category_open());
        style.setLeafIcon(resources.subCategory());
    }

    private void initTreeStoreSorter() {

        Comparator<AppGroup> comparator = new Comparator<AppGroup>() {

            @Override
            public int compare(AppGroup group1, AppGroup group2) {
                if (treeStore.getRootItems().contains(group1)
                            || treeStore.getRootItems().contains(group2)) {
                    // Do not sort Root groups, since we want to keep the service's root order.
                    return 0;
                }

                return group1.getName().compareToIgnoreCase(group2.getName());
            }
        };

        treeStore.addSortInfo(new StoreSortInfo<AppGroup>(comparator, SortDir.ASC));
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void hideAppMenu() {
        toolbar.hideAppMenu();
    }

    @Override
    public void hideWorkflowMenu() {
        toolbar.hideWorkflowMenu();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        ((AppColumnModel)cm).addAppInfoClickedEventHandler(this);
        ((AppColumnModel)cm).addAppNameSelectedEventHandler(presenter);
        ((AppColumnModel)cm).addAppFavoritedEventHandler(this);
        addAppGroupSelectedEventHandler(presenter);
        this.toolbar.init(presenter, this, this, this);
    }

    @Override
    public void setCenterPanelHeading(final String name) {
        centerPanel.setHeadingText(name);
    }

    @Override
    public void maskCenterPanel(final String loadingMask) {
        centerPanel.mask(loadingMask);
    }

    @Override
    public void submitSelectedApp() {
        App selectedApp = grid.getSelectionModel().getSelectedItem();
        new SubmitAppForPublicDialog(selectedApp).show();
    }

    @Override
    public void unMaskCenterPanel() {
        centerPanel.unmask();
    }

    @Override
    public void maskWestPanel(String loadingMask) {
        westPanel.mask(loadingMask);
    }

    @Override
    public void unMaskWestPanel() {
        westPanel.unmask();
    }

    @Override
    public void selectApp(String appId) {
        App app = listStore.findModelWithKey(appId);
        if (app != null) {
            grid.getSelectionModel().select(app, false);
        }
    }

    @Override
    public void selectAppGroup(String appGroupId) {
        if (Strings.isNullOrEmpty(appGroupId)) {
            tree.getSelectionModel().deselectAll();
        } else {
            AppGroup ag = treeStore.findModelWithKey(appGroupId);
            if (ag != null) {
                tree.getSelectionModel().select(ag, false);
                tree.scrollIntoView(ag);
            }
        }
    }

    @Override
    public List<String> computeGroupHierarchy(final AppGroup ag) {
        List<String> groupNames = Lists.newArrayList();

        for (AppGroup group : getGroupHierarchy(ag, null)) {
            groupNames.add(group.getName());
        }
        Collections.reverse(groupNames);
        return groupNames;
    }

    @Override
    public App getSelectedApp() {
        return grid.getSelectionModel().getSelectedItem();
    }

    @Override

    public List<App> getAllSelectedApps() {
        return grid.getSelectionModel().getSelectedItems();
    }

    @Override
    public AppGroup getSelectedAppGroup() {
        return tree.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setApps(final List<App> apps) {
        listStore.clear();

        for (App app : apps) {
            if (listStore.findModel(app) == null) {
                listStore.add(app);
            }
        }
    }


    protected void setNorthWidget(IsWidget widget) {
        northData.setHidden(false);
        con.setNorthWidget(widget, northData);
    }

    @Override
    public void selectFirstApp() {
        grid.getSelectionModel().select(0, false);
    }

    @Override
    public void selectFirstAppGroup() {
        AppGroup ag = treeStore.getRootItems().get(0);
        tree.getSelectionModel().select(ag, false);
        tree.scrollIntoView(ag);
    }

    @Override
    public void addAppGroup(AppGroup parent, AppGroup child) {
        if (child == null) {
            return;
        }

        if (parent == null) {
            treeStore.add(child);
        } else {
            treeStore.add(parent, child);
        }
    }

    @Override
    public void addAppGroups(AppGroup parent, List<AppGroup> children) {
        if ((children == null) || children.isEmpty()) {
            return;
        }
        if (parent == null) {
            treeStore.add(children);
        } else {
            treeStore.add(parent, children);
        }

        for (AppGroup ag : children) {
            addAppGroups(ag, ag.getGroups());
        }
    }

    @Override
    public void removeApp(App app) {
        grid.getSelectionModel().deselectAll();
        listStore.remove(app);
    }

    protected void deSelectAllAppGroups() {
        tree.getSelectionModel().deselectAll();
    }

    @Override
    public void updateAppGroup(AppGroup appGroup) {
        treeStore.update(appGroup);
    }

    @Override
    public AppGroup findAppGroupByName(String name) {
        for (AppGroup appGroup : treeStore.getAll()) {
            if (appGroup.getName().equalsIgnoreCase(name)) {
                return appGroup;
            }
        }

        return null;
    }

    @Override
    public void updateAppGroupAppCount(AppGroup appGroup, int newCount) {
        int difference = appGroup.getAppCount() - newCount;

        while (appGroup != null) {
            appGroup.setAppCount(appGroup.getAppCount() - difference);
            updateAppGroup(appGroup);
            appGroup = treeStore.getParent(appGroup);
        }

    }

    protected App findApp(String appId) {
        return listStore.findModelWithKey(appId);
    }

    @Override
    public Grid<App> getAppsGrid() {
        return grid;
    }

    @Override
    public void expandAppGroups() {
        tree.expandAll();
    }

    @Override
    public boolean isTreeStoreEmpty() {
        return treeStore.getAll().isEmpty();
    }

    @Override
    public void clearAppGroups() {
        treeStore.clear();
    }

    @Override
    public AppGroup getAppGroupFromElement(Element el) {
        TreeNode<AppGroup> node = tree.findNode(el);
        if (node != null && tree.getView().isSelectableTarget(node.getModel(), el)) {
            return node.getModel();
        }

        return null;
    }

    @Override
    public App getAppFromElement(Element el) {
        Element row = gridView.findRow(el);
        int index = gridView.findRowIndex(row);
        return listStore.get(index);
    }

    @Override
    public String highlightSearchText(String text) {
        // Sanitize
        String ret = SafeHtmlUtils.fromString(Strings.nullToEmpty(text)).asString();

        return presenter.highlightSearchText(text);
    }

    @Override
    public List<AppGroup> getAppGroupRoots() {
        return treeStore.getRootItems();
    }

    @Override
    public AppGroup getParent(AppGroup child) {
        return treeStore.getParent(child);
    }

    List<AppGroup> getGroupHierarchy(AppGroup grp, List<AppGroup> groups) {
        if (groups == null) {
            groups = new ArrayList<AppGroup>();
        }
        groups.add(grp);
        if (treeStore.getRootItems().contains(grp)) {
            return groups;
        } else {
            return getGroupHierarchy(treeStore.getParent(grp), groups);
        }
    }

    @Override
    public void mask(String loadingMask) {
        con.mask(displayStrings.loadingMask());

    }

    @Override
    public void unmask() {
        con.unmask();

    }
}
