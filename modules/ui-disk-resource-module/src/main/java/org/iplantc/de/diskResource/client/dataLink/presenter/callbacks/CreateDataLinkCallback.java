package org.iplantc.de.diskResource.client.dataLink.presenter.callbacks;

import org.iplantc.de.client.models.dataLink.DataLink;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.diskResource.client.dataLink.view.DataLinkPanel;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;

import java.util.List;

/**
 * @author jstroot
 */
public class CreateDataLinkCallback implements AsyncCallback<List<DataLink>> {

    private final Tree<DiskResource, DiskResource> tree;
    private final DataLinkPanel view;

    public CreateDataLinkCallback(final DataLinkPanel view) {
        this.view = view;
        this.tree = this.view.getTree();
    }

    @Override
    public void onFailure(Throwable caught) {
        ErrorHandler.post(I18N.ERROR.createDataLinksError(), caught);
        view.unmask();
    }

    @Override
    public void onSuccess(List<DataLink> dlList) {

        TreeStore<DiskResource> treeStore = tree.getStore();
        DiskResource parent = null;
        for (DataLink dl : dlList) {
            // manually find the item since id's wont work
            for (DiskResource item : tree.getStore().getAll()) {
                if (item.getPath().equals(dl.getPath())) {
                    parent = item;
                    break;
                }
            }
            if (parent != null) {
                treeStore.add(parent, dl);
                tree.setExpanded(parent, true);
                tree.setChecked(dl, CheckState.CHECKED);
            }
        }
        view.unmask();
    }

}
