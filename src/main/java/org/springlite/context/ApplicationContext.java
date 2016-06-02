package org.springlite.context;

import org.springlite.beans.factory.BeanFactory;
import org.springlite.beans.factory.ListableBeanFactory;

/**
 * <p/>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 2016/5/27
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface ApplicationContext extends ListableBeanFactory{

    /**
     * Return the unique id of this application context.
     * @return the unique id of the context, or {@code null} if none
     */
    String getId();

    /**
     * Return a name for the deployed application that this context belongs to.
     * @return a name for the deployed application, or the empty String by default
     */
    String getApplicationName();

    /**
     * Return a friendly name for this context.
     * @return a display name for this context (never {@code null})
     */
    String getDisplayName();

    /**
     * Return the timestamp when this context was first loaded.
     * @return the timestamp (ms) when this context was first loaded
     */
    long getStartupDate();

    ApplicationContext getParent();

    //Environment getEnvironment();
}
