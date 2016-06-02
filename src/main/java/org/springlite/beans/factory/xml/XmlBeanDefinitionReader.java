package org.springlite.beans.factory.xml;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springlite.beans.BeanReference;
import org.springlite.beans.BeanSchema;
import org.springlite.beans.BeanUtils;
import org.springlite.beans.ConstructorArgumentValues;
import org.springlite.beans.ConstructorArgumentValues.ValueHolder;
import org.springlite.beans.exception.BeanDefinitionStoreException;
import org.springlite.core.NamedThreadLocal;
import org.springlite.core.io.ResourceLoader;
import org.springlite.beans.factory.support.AbstractBeanDefinitionReader;
import org.springlite.beans.factory.support.BeanDefinitionBuilder;
import org.springlite.beans.factory.support.BeanDefinitionRegistry;
import org.springlite.core.io.Resource;
import org.springlite.util.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    /**
     * JAXP attribute used to configure the schema language for validation.
     */
    private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * JAXP attribute value indicating the XSD schema language.
     */
    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private final ThreadLocal<Set<Resource>> resourcesCurrentlyBeingLoaded =
            new NamedThreadLocal<Set<Resource>>("XML bean definition resources currently being loaded");

    private boolean validating = true;

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
        super(registry, resourceLoader);
    }

    public boolean isValidating() {
        return validating;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * Load bean definitions from the specified XML file.
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        Assert.notNull(resource, "Resource must not be null");
        if (logger.isInfoEnabled()) {
            logger.info("Loading XML bean definitions from " + resource);
        }

        Set<Resource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
        if (currentResources == null) {
            currentResources = new HashSet<Resource>(4);
            this.resourcesCurrentlyBeingLoaded.set(currentResources);
        }
        if (!currentResources.add(resource)) {
            throw new BeanDefinitionStoreException(
                    "Detected cyclic loading of " + resource + " - check your import definitions!");
        }
        try {
            InputStream inputStream = resource.getInputStream();
            try {
                InputSource inputSource = new InputSource(inputStream);
                return doLoadBeanDefinitions(inputSource, resource);
            }
            finally {
                inputStream.close();
            }
        }
        catch (IOException ex) {
            throw new BeanDefinitionStoreException(
                    "IOException parsing XML document from " + resource, ex);
        }
        finally {
            currentResources.remove(resource);
            if (currentResources.isEmpty()) {
                this.resourcesCurrentlyBeingLoaded.remove();
            }
        }
    }

    protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
        try {
            DocumentBuilderFactory factory = createDocumentBuilderFactory(validating);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputSource);
            // 解析beans
            return registerBeanDefinitions(doc, resource);
        } catch (ParserConfigurationException e) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "Parser configuration exception parsing XML document from " + resource ,e);
        } catch (SAXParseException ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
        } catch (SAXException e) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "SAXException parsing XML document from " + resource ,e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "IOException parsing XML document from " + resource ,e);
        } catch (BeanDefinitionStoreException ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "BeanDefinition registering exception parsing XML document from " + resource, ex);
        } catch (Throwable ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "Unexpected exception parsing XML document from " + resource, ex);
        }
    }

    protected DocumentBuilderFactory createDocumentBuilderFactory(boolean validating)
            throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        if(validating){
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            try {
                factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
            }
            catch (IllegalArgumentException ex) {
                ParserConfigurationException pcex = new ParserConfigurationException(
                        "Unable to validate using XSD: Your JAXP provider [" + factory +
                                "] does not support XML Schema. Are you running on Java 1.4 with Apache Crimson? " +
                                "Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
                pcex.initCause(ex);
                throw pcex;
            }
        }
        return factory;
    }

    protected int registerBeanDefinitions(Document doc, Resource resource) {
        Element root = doc.getDocumentElement();
        int beanDefinitionsCount = 0;
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if(BeanSchema.ELEMENT.BEAN.equals(node.getNodeName())){
                    try {
                        processBeanDefinition(ele);
                    } catch (IllegalArgumentException e){
                        throw new BeanDefinitionStoreException("IllegalArgumentException: ", e);
                    }
                    beanDefinitionsCount++;
                }else if(BeanSchema.ELEMENT.IMPORT.equals(node.getNodeName())){
                    importBeanDefinitionResource(ele ,resource);
                }else{
                    logger.error("Unknown element '"+ele.getNodeName()+"'"+":"+ele.getTextContent());
                }
            }
        }
        return beanDefinitionsCount;
    }

    protected void processBeanDefinition(Element ele) throws BeanDefinitionStoreException {

        String className = ele.getAttribute(BeanSchema.ATTRIBUTE.CLASS);
        String beanName = ele.getAttribute(BeanSchema.ATTRIBUTE.ID);
        Validate.notEmpty(className, "Bean element attribute［id］can not be null, detail: " + ele.getNodeName() + "\n\r");
        Validate.notEmpty(className, "Bean element attribute［class］can not be null, detail: " + ele.getNodeName() + "\n\r");

        //构造beanDefinition对象
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(className);
        definitionBuilder.setBeanName(beanName);

        //处理<bean>其他属性
        processBeanAttributes(ele, definitionBuilder);

        //构造方法参数
        processConstructorArgs(ele, definitionBuilder);

        //处理bean属性
        processProperty(ele, definitionBuilder);

        //注册beanDefinition
        getRegistry().registerBeanDefinition(beanName, definitionBuilder.getBeanDefinition());

    }

    /**
     * Parse an "import" element and load the bean definitions
     * from the given resource into the bean factory.
     */
    protected void importBeanDefinitionResource(Element ele, Resource parentResource) {
        String location = ele.getAttribute(BeanSchema.ATTRIBUTE.RESOURCE);
        if (!StringUtils.isNotBlank(location)) {
            logger.error("Resource location must not be empty, from xml " + getElementText(ele));
            return;
        }

        String actualLocation = null;
        if(location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)){
            actualLocation = location;

        } else {
            try {
                String fileDirectory = ResourceUtils.getFileDirectory(parentResource.getURL());
                actualLocation = ResourceUtils.FILE_URL_PREFIX + fileDirectory + File.separator + location;
            } catch (IOException e) {
                throw new BeanDefinitionStoreException("Cannot parse import resource in location '"+location
                        +"',  please check element '"+ getElementText(ele) +"'", e);
            }
        }
        Set<Resource> actualResources = new LinkedHashSet<Resource>(4);
        loadBeanDefinitions(actualLocation, actualResources);
    }

    protected void processBeanAttributes(Element ele, BeanDefinitionBuilder definitionBuilder){
        //init-method
        String initMethodName = ele.getAttribute(BeanSchema.ATTRIBUTE.INIT_METHOD);
        if (StringUtils.isNotBlank(initMethodName)) {
            definitionBuilder.setInitMethodName(initMethodName);
        }

        //destroy-method
        String destroyMethodName = ele.getAttribute(BeanSchema.ATTRIBUTE.DESTROY_METHOD);
        if(StringUtils.isNotBlank(destroyMethodName)){
            definitionBuilder.setDestroyMethodName(destroyMethodName);
        }

        //depends-on
        String dependsOn = ele.getAttribute(BeanSchema.ATTRIBUTE.DEPENDS_ON);
        if (StringUtils.isNotBlank(dependsOn)) {
            String[] depends  = dependsOn.split(BeanSchema.COMMA_VALUE);
            for(String depend : depends){
                definitionBuilder.addDependsOn(depend);
            }
        }

        //lazy-init
        String lazyInit = ele.getAttribute(BeanSchema.ATTRIBUTE.LAZY_INIT);
        if (BeanSchema.TRUE_VALUE.equals(lazyInit)){
            definitionBuilder.setLazyInit(true);
        } else if(BeanSchema.DEFAULT_VALUE.equals(lazyInit)) {
            //default not set
        } else {
            definitionBuilder.setLazyInit(false);
        }

        //scope
        String scope = ele.getAttribute(BeanSchema.ATTRIBUTE.SCOPE);
        if (StringUtils.isNotBlank(scope)){
            definitionBuilder.setScope(scope);
        }
    }

    protected void processConstructorArgs(Element ele, BeanDefinitionBuilder definitionBuilder) {
        NodeList constructorArgsNode = ele.getElementsByTagName(BeanSchema.ELEMENT.CONSTRUCTOR_ARG);
        for(int i = 0 ;i < constructorArgsNode.getLength(); i++){
            Node node = constructorArgsNode.item(i);
            if (node instanceof Element) {
                Element propertyEle = (Element) node;
                String index = StringUtils.trimToNull(propertyEle.getAttribute(BeanSchema.ATTRIBUTE.INDEX));
                String name = StringUtils.trimToNull(propertyEle.getAttribute(BeanSchema.ATTRIBUTE.NAME));
                String type = StringUtils.trimToNull(propertyEle.getAttribute(BeanSchema.ATTRIBUTE.TYPE));

                String ref = StringUtils.trimToNull(propertyEle.getAttribute(BeanSchema.ATTRIBUTE.REF));
                String value = StringUtils.trimToNull(propertyEle.getAttribute(BeanSchema.ATTRIBUTE.VALUE));

                //must specify a ref or value
                if( (ref == null && value == null) ||
                        (ref != null && value != null) ){
                    throw new BeanDefinitionStoreException("Element '<" + BeanSchema.ELEMENT.CONSTRUCTOR_ARG + ">' must specify a ref or value, from xml: "+getElementText(propertyEle));
                }
                //index
                if(index != null){
                    int indexInt = Integer.valueOf(index);
                    if(value != null){
                        definitionBuilder.addIndexConstructorArgValue(indexInt, value, type);
                    } else {
                        BeanReference reference = new BeanReference(ref);
                        definitionBuilder.addIndexConstructorArgValue(indexInt, reference, type);
                    }
                //name or type
                } else if(name != null || type != null) {
                    if(value != null){
                        definitionBuilder.addGenericConstructorArgValue(value, name, type);
                    } else {
                        BeanReference reference = new BeanReference(ref);
                        definitionBuilder.addGenericConstructorArgValue(reference, name, type);
                    }
                //ref or value
                } else {
                    if(value != null){
                        definitionBuilder.addConstructorArgValue(value);
                    } else {
                        definitionBuilder.addConstructorArgReference(ref);
                    }
                }
            }
        }
        ConstructorArgumentValues constructorArgs = definitionBuilder.getBeanDefinition().getConstructorArgumentValues();
        try {
            validateConstructorArgs(ele, constructorArgs, constructorArgsNode.getLength());
        } catch (IllegalArgumentException e) {
            throw new BeanDefinitionStoreException("Constructor-args validate fail, from xml: " + getElementText(ele, true), e);
        }
    }

    private void validateConstructorArgs(Element ele, ConstructorArgumentValues constructorArgs, int argsCount){
        if(constructorArgs.getArgumentCount() == 0){
            return;
        }
        if( !constructorArgs.getIndexedArgumentValues().isEmpty() && !constructorArgs.getGenericArgumentValues().isEmpty()){
            Validate.isTrue(false, "Bean named '" + ele.getAttribute(BeanSchema.ATTRIBUTE.ID)
                    + "', only support one constructor-args autowire type: 'index' or 'name' or 'type'");
        }
        Validate.isTrue(constructorArgs.getArgumentCount()== argsCount, "Constructor args count should equal element '<" + BeanSchema.ELEMENT.CONSTRUCTOR_ARG + ">' count.");
        if(!constructorArgs.getIndexedArgumentValues().isEmpty()){
            constructorArgs.setAutowrieBy(ConstructorArgumentValues.BY_INDEX);
            Map<Integer, ValueHolder> indexedArgumentValues = constructorArgs.getIndexedArgumentValues();
            //判断index是从0-argsCount, 以1递增的数字
            for(int i=0; i< argsCount; i++){
                Validate.isTrue(indexedArgumentValues.containsKey(new Integer(i)), "constructor-args index should be from '0' to 'args_count-1'.");
            }
        } else {
            List<ValueHolder> genericArgumentValues = constructorArgs.getGenericArgumentValues();
            ValueHolder first = genericArgumentValues.get(0);
            if(StringUtils.isNotBlank(first.getName())){
                constructorArgs.setAutowrieBy(ConstructorArgumentValues.BY_NAME);
                for( ValueHolder valueHolder : genericArgumentValues ){
                    Validate.notEmpty(valueHolder.getName(), "Bean named '" + ele.getAttribute(BeanSchema.ATTRIBUTE.ID)
                            + "', only support one constructor-args autowire type: 'index' or 'name' or 'type'");
                }
            } else {
                constructorArgs.setAutowrieBy(ConstructorArgumentValues.BY_TYPE);
                for( ValueHolder valueHolder : genericArgumentValues ){
                    Validate.notEmpty(valueHolder.getType(), "Bean named '" + ele.getAttribute(BeanSchema.ATTRIBUTE.ID)
                            + "', only support one constructor-args autowire type: 'index' or 'name' or 'type'");
                    Validate.isTrue(BeanUtils.isSimpleType(valueHolder.getType()),
                            "Class type '" + valueHolder.getType() + "' must be a simple property, on property 'type' from xml: " + getElementText(ele));
                }
            }
        }
    }

    private String getElementText(Element element){
        return XmlUtils.getElementText(element, false);
    }

    private String getElementText(Element element, boolean includeChilds){
        return XmlUtils.getElementText(element, includeChilds);
    }

    /**
     * 处理bean属性
     * @param ele
     * @param definitionBuilder
     */
    protected void processProperty(Element ele, BeanDefinitionBuilder definitionBuilder) {
        NodeList propertyNode = ele.getElementsByTagName(BeanSchema.ELEMENT.PROPERTY);
        for (int i = 0; i < propertyNode.getLength(); i++) {
            Node node = propertyNode.item(i);

            if (node instanceof Element) {
                Element propertyEle = (Element) node;

                String name = propertyEle.getAttribute(BeanSchema.ATTRIBUTE.NAME);
                Validate.notEmpty(name, "Property 'name' can not be null, from xml: " + getElementText(ele));

                if(!propertyEle.hasChildNodes()){
                    String value = propertyEle.getAttribute(BeanSchema.ATTRIBUTE.VALUE);
                    String ref = propertyEle.getAttribute(BeanSchema.ATTRIBUTE.REF);
                    if( (StringUtils.isBlank(ref) && StringUtils.isBlank(value)) ||
                            (StringUtils.isNotBlank(ref) && StringUtils.isNotBlank(value)) ){
                        throw new BeanDefinitionStoreException("Element '<" + BeanSchema.ELEMENT.PROPERTY + ">' must specify a ref or value.");
                    }
                    if (StringUtils.isNotBlank(value)) {
                        definitionBuilder.addPropertyValue(name, value);
                    } else {
                        BeanReference beanReference = new BeanReference(ref);
                        definitionBuilder.addPropertyValue(name, beanReference);
                    }
                }
                else {
                    processPropertyChilds(name, propertyEle.getChildNodes(), definitionBuilder);
                }
            }

        }

    }

    protected void processPropertyChilds(String propertyName, NodeList nodeList, BeanDefinitionBuilder definitionBuilder) {
        //TODO 属性有子节点的情况处理
    }

}
