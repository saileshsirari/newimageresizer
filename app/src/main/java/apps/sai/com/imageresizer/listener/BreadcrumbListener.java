package apps.sai.com.imageresizer.listener;


import apps.sai.com.imageresizer.select.BreadcrumbItem;

/**
 * Interface with events from a breadcrumb
 */
public interface BreadcrumbListener {
    /**
     * This method is called when a breadcrumb item is clicked
     *
     * @param item The breadcrumb item click
     */
    void onBreadcrumbItemClick(BreadcrumbItem item);
}