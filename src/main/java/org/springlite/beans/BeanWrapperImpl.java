package org.springlite.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springlite.util.Assert;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class BeanWrapperImpl implements BeanWrapper {
    /**
     * We'll create a lot of these objects, so we don't want a new logger every time.
     */
    private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);


    /** The wrapped object */
    private Object object;

    public BeanWrapperImpl(Object object){
        setWrappedInstance(object);
    }

    //---------------------------------------------------------------------
    // Implementation of BeanWrapper interface
    //---------------------------------------------------------------------

    /**
     * Switch the target object, replacing the cached introspection results only
     * if the class of the new object is different to that of the replaced object.
     * @param object the new target object
     */
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "", null);
    }

    /**
     * Switch the target object, replacing the cached introspection results only
     * if the class of the new object is different to that of the replaced object.
     * @param object the new target object
     * @param nestedPath the nested path of the object
     * @param rootObject the root object at the top of the path
     */
    public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
        Assert.notNull(object, "Bean object must not be null");
        this.object = object;
//        this.nestedPath = (nestedPath != null ? nestedPath : "");
//        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : object);
//        this.nestedBeanWrappers = null;
//        this.typeConverterDelegate = new TypeConverterDelegate(this, object);
//        setIntrospectionClass(object.getClass());
    }

    public final Object getWrappedInstance() {
        return this.object;
    }

    public final Class<?> getWrappedClass() {
        return (this.object != null ? this.object.getClass() : null);
    }
}
