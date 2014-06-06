package org.iplantc.de.client.viewer.presenter;

import org.iplantc.de.client.gin.ServicesInjector;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.viewer.MimeType;
import org.iplantc.de.client.models.viewer.VizUrl;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.client.viewer.callbacks.TreeUrlCallback;
import org.iplantc.de.client.viewer.commands.ViewCommand;
import org.iplantc.de.client.viewer.factory.MimeTypeViewerResolverFactory;
import org.iplantc.de.client.viewer.views.FileViewer;
import org.iplantc.de.client.views.windows.FileViewerWindow;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Widget;

import com.sencha.gxt.widget.core.client.TabItemConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sriram
 * 
 */
public class FileViewerPresenter implements FileViewer.Presenter {

    // A presenter can handle more than one view of the same data at a time
    private List<FileViewer> viewers;

    private FileViewerWindow container;

    /**
     * The file shown in the window.
     */
    private File file;

    /**
     * The manifest of file contents
     */
    private final JSONObject manifest;

    private final boolean editing;

    private boolean isDirty;

    private final boolean isVizTabFirst;

    private String title;

    public FileViewerPresenter(File file, JSONObject manifest, boolean editing, boolean isVizTabFirst) {
        this.manifest = manifest;
        viewers = new ArrayList<FileViewer>();
        this.file = file;
        this.editing = editing;
        this.isVizTabFirst = isVizTabFirst;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.commons.client.presenter.Presenter#go(com.google.gwt
     * .user.client.ui.HasOneWidget )
     */
    @Override
    public void go(HasOneWidget container, Folder parentFolder) {
        this.container = (FileViewerWindow)container;
        title = this.container.getTitle();
        composeView(parentFolder);
    }

    @Override
    public void composeView(Folder parentFolder) {
        container.mask(org.iplantc.de.resources.client.messages.I18N.DISPLAY.loadingMask());
        String mimeType = JsonUtil.getString(manifest, "content-type");
        ViewCommand cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType.fromTypeString(mimeType));
        String infoType = JsonUtil.getString(manifest, "info-type");
        List<? extends FileViewer> viewers_list = cmd.execute(file, infoType, editing, parentFolder);

        if (viewers_list != null && viewers_list.size() > 0) {
            viewers.addAll(viewers_list);
            for (FileViewer view : viewers) {
                view.setPresenter(this);
                container.getWidget().add(view.asWidget(), view.getViewName());
            }
            container.unmask();
        }

        boolean treeViewer = DiskResourceUtil.isTreeTab(manifest);
        boolean cogeViewer = DiskResourceUtil.isGenomeVizTab(manifest);
        boolean ensembleViewer = DiskResourceUtil.isEnsemblVizTab(manifest);

        if (treeViewer || cogeViewer || ensembleViewer) {
            cmd = MimeTypeViewerResolverFactory.getViewerCommand(MimeType.fromTypeString("viz"));
            List<? extends FileViewer> vizViewers = cmd.execute(file, infoType, editing, parentFolder);
            List<VizUrl> urls = getManifestVizUrls();
            if (urls != null && urls.size() > 0) {
                vizViewers.get(0).setData(urls);
            } else {
                if (treeViewer) {
                    callTreeCreateService(vizViewers.get(0));
                }
            }
            viewers.add(vizViewers.get(0));
            if (isVizTabFirst) {
                Widget asWidget = vizViewers.get(0).asWidget();
                container.getWidget().insert(asWidget, 0, new TabItemConfig(vizViewers.get(0).getViewName()));
                container.getWidget().setActiveWidget(asWidget);
            } else {
                container.getWidget().add(vizViewers.get(0).asWidget(), vizViewers.get(0).getViewName());
            }
        }

        if (viewers.size() == 0) {
            container.unmask();
            container.add(new HTML(org.iplantc.de.resources.client.messages.I18N.DISPLAY.fileOpenMsg()));
        }

    }

    /**
     * Gets the tree-urls json array from the manifest.
     * 
     * @return A json array of at least one tree URL, or null otherwise.
     */
    private List<VizUrl> getManifestVizUrls() {
        return TreeUrlCallback.getTreeUrls(manifest.toString());

    }

    /**
     * Calls the tree URL service to fetch the URLs to display in the grid.
     */
    public void callTreeCreateService(final FileViewer viewer) {
        container.mask(org.iplantc.de.resources.client.messages.I18N.DISPLAY.loadingMask());
        ServicesInjector.INSTANCE.getFileEditorServiceFacade().getTreeUrl(file.getId(), false, new TreeUrlCallback(file, container, viewer));
    }

    @Override
    public void setVeiwDirtyState(boolean dirty) {
        this.isDirty = dirty;
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        if (isDirty) {
            container.setTitle(container.getTitle() + "<span style='color:red; vertical-align: super'> * </span>");
        } else {
            container.setTitle(title);
        }
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void cleanUp() {
        if (viewers != null && viewers.size() > 0) {
            for (FileViewer view : viewers) {
                view.cleanUp();
            }
        }

        viewers = null;
        file = null;

    }

    @Override
    public void refreshViews() {
        if (viewers != null) {
        for (FileViewer fv : viewers) {
            fv.refresh();
            }
        }
    }

}
