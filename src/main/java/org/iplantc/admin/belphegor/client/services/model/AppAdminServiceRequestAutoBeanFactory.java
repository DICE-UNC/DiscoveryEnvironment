package org.iplantc.admin.belphegor.client.services.model;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface AppAdminServiceRequestAutoBeanFactory extends AutoBeanFactory {

    AutoBean<AppCategorizeRequest> appCategorizeRequest();

    AutoBean<AppCategorizeRequest.CategoryRequest> categoryRequest();

    AutoBean<AppCategorizeRequest.CategoryPath> categoryPath();
}
