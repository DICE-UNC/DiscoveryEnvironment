package org.iplantc.de.apps.client.presenter;

import org.iplantc.de.apps.client.AppCategoriesView;
import org.iplantc.de.apps.client.AppsGridView;
import org.iplantc.de.apps.client.AppsToolbarView;
import org.iplantc.de.apps.client.AppsView;
import org.iplantc.de.apps.client.gin.factory.AppsViewFactory;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppCategory;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * The presenter for the AppsView.
 *
 * @author jstroot
 */
public class AppsViewPresenterImpl implements AppsView.Presenter {

    protected final AppsView view;
    private final AppCategoriesView.Presenter categoriesPresenter;
    private final AppsGridView.Presenter appsGridPresenter;

    @Inject
    protected AppsViewPresenterImpl(final AppsViewFactory viewFactory,
                                    final AppCategoriesView.Presenter categoriesPresenter,
                                    final AppsGridView.Presenter appsGridPresenter,
                                    final AppsToolbarView.Presenter toolbarPresenter) {
        this.categoriesPresenter = categoriesPresenter;
        this.appsGridPresenter = appsGridPresenter;
        this.view = viewFactory.create(categoriesPresenter,
                                       appsGridPresenter,
                                       toolbarPresenter);

        categoriesPresenter.getView().addAppCategorySelectedEventHandler(appsGridPresenter);
        categoriesPresenter.getView().addAppCategorySelectedEventHandler(appsGridPresenter.getView());
        categoriesPresenter.getView().addAppCategorySelectedEventHandler(toolbarPresenter.getView());

        // Wire up list store handlers
        appsGridPresenter.addStoreAddHandler(categoriesPresenter);
        appsGridPresenter.addStoreRemoveHandler(categoriesPresenter);
        appsGridPresenter.addStoreClearHandler(categoriesPresenter);
        appsGridPresenter.addAppFavoritedEventHandler(categoriesPresenter);
        appsGridPresenter.getView().addAppSelectionChangedEventHandler(toolbarPresenter.getView());

        appsGridPresenter.getView().addAppInfoSelectedEventHandler(categoriesPresenter);

        toolbarPresenter.getView().addDeleteAppsSelectedHandler(appsGridPresenter);
        toolbarPresenter.getView().addCopyAppSelectedHandler(categoriesPresenter);
        toolbarPresenter.getView().addCopyWorkflowSelectedHandler(categoriesPresenter);
        toolbarPresenter.getView().addRunAppSelectedHandler(appsGridPresenter);
        toolbarPresenter.getView().addBeforeAppSearchEventHandler(appsGridPresenter.getView());
        toolbarPresenter.getView().addAppSearchResultLoadEventHandler(categoriesPresenter);
        toolbarPresenter.getView().addAppSearchResultLoadEventHandler(appsGridPresenter);
        toolbarPresenter.getView().addAppSearchResultLoadEventHandler(appsGridPresenter.getView());

    }

    @Override
    public Grid<App> getAppsGrid() {
        // FIXME Too many levels of misdirection
        return appsGridPresenter.getView().getGrid();
    }

    @Override
    public App getSelectedApp() {
        return appsGridPresenter.getSelectedApp();
    }

    @Override
    public AppCategory getSelectedAppCategory() {
        return categoriesPresenter.getSelectedAppCategory();
    }

    @Override
    public void go(final HasOneWidget container,
                   final HasId selectedAppCategory,
                   final HasId selectedApp) {
        categoriesPresenter.go(selectedAppCategory);
        container.setWidget(view);
    }

    @Override
    public AppsView.Presenter hideAppMenu() {
        view.hideAppMenu();
        return this;
    }

    @Override
    public AppsView.Presenter hideWorkflowMenu() {
        view.hideWorkflowMenu();
        return this;
    }

    @Override
    public void setViewDebugId(String baseId) {
        view.asWidget().ensureDebugId(baseId);
    }

}
