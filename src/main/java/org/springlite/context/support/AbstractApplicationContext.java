/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springlite.context.support;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springlite.beans.exception.BeansException;
import org.springlite.beans.exception.NoSuchBeanDefinitionException;
import org.springlite.beans.factory.*;
import org.springlite.beans.factory.config.BeanPostProcessor;
import org.springlite.context.ApplicationContext;
import org.springlite.context.ConfigurableApplicationContext;
import org.springlite.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractApplicationContext implements ConfigurableApplicationContext, DisposableBean {

	/** Logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Unique id for this context, if any */
	private String id = ObjectUtils.identityToString(this);

	/** Parent context */
	private ApplicationContext parent;

	/** Display name */
	private String displayName = ObjectUtils.identityToString(this);

	/** System time in milliseconds when this context started */
	private long startupDate;

	/** Flag that indicates whether this context is currently active */
	private boolean active = false;

	/** Flag that indicates whether this context has been closed already */
	private boolean closed = false;

	/** Synchronization monitor for the "active" flag */
	private final Object activeMonitor = new Object();

	/** Synchronization monitor for the "refresh" and "destroy" */
	private final Object startupShutdownMonitor = new Object();

	/** Reference to the JVM shutdown hook, if registered */
	private Thread shutdownHook;

	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
		//this.resourcePatternResolver = getResourcePatternResolver();
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getStartupDate() {
		return startupDate;
	}

	public String getApplicationName() {
		return "";
	}

	public ApplicationContext getParent() {
		return this.parent ;
	}

	public void setParent(ApplicationContext parent) {
		this.parent = parent;
	}

	/**
	 * ConfigurableApplicationContext
	 * @see ConfigurableApplicationContext
	 */
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				// 执行 BeanFactoryPostProcessor
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				initMessageSource();

				// Initialize event multicaster for this context.
				// 初始化事件广播器 ApplicationEventMulticaster
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				onRefresh();

				// Check for listener beans and register them.
				// 注册事件监听器 ApplicationListener
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				// 初始化剩余的非延迟初始化单例
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}
		}
	}

	/**
	 * Prepare this context for refreshing, setting its startup date and
	 * active flag as well as performing any initialization of property sources.
	 */
	protected void prepareRefresh() {
		this.startupDate = System.currentTimeMillis();

		synchronized (this.activeMonitor) {
			this.active = true;
		}

		if (logger.isInfoEnabled()) {
			logger.info("Refreshing " + this);
		}

		// Initialize any placeholder property sources in the context environment
		initPropertySources();

		// Validate that all properties marked as required are resolvable
		// see ConfigurablePropertyResolver#setRequiredProperties
		//getEnvironment().validateRequiredProperties();
	}

	/**
	 * <p>Replace any stub property sources with actual instances.
	 */
	protected void initPropertySources() {
		// For subclasses: do nothing by default.
	}

	/**
	 * Tell the subclass to refresh the internal bean factory.
	 * @return the fresh BeanFactory instance
	 * @see #refreshBeanFactory()
	 * @see #getBeanFactory()
	 */
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
		}
		return beanFactory;
	}

	/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 * @param beanFactory the BeanFactory to configure
	 */
	protected void prepareBeanFactory(BeanFactory beanFactory) {
//		// Tell the internal bean factory to use the context's class loader etc.
//		beanFactory.setBeanClassLoader(getClassLoader());
//		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
//		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));
//
//		// Configure the bean factory with context callbacks.
//		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
//		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
//		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
//		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
//		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
//		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
//
//		// BeanFactory interface not registered as resolvable type in a plain factory.
//		// MessageSource registered (and found for autowiring) as a bean.
//		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
//		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
//		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
//		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

//		// Detect a LoadTimeWeaver and prepare for weaving, if found.
//		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
//			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
//			// Set a temporary ClassLoader for type matching.
//			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
//		}
//
//		// Register default environment beans.
//		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
//			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
//		}
//		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
//			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
//		}
//		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
//			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
//		}
	}

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for registering special
	 * BeanPostProcessors etc in certain ApplicationContext implementations.
	 * @param beanFactory the bean factory used by the application context
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before singleton instantiation.
	 *
	 * 执行顺序
	 * 1、getBeanFactoryPostProcessors().BeanDefinitionRegistryPostProcessor
	 * 2、beanFactory.getBeansOfType(BeanDefinitionRegistryPostProcessor.class, true, false)
	 * 3、getBeanFactoryPostProcessors().BeanFactoryPostProcessor
	 * 4、beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false)
	 * 	分类后在按顺序执行：PriorityOrdered -> Ordered -> the rest
	 *
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
//		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
//		Set<String> processedBeans = new HashSet<String>();
//		if (beanFactory instanceof BeanDefinitionRegistry) {
//			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
//			List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<BeanFactoryPostProcessor>();
//			List<BeanDefinitionRegistryPostProcessor> registryPostProcessors =
//					new LinkedList<BeanDefinitionRegistryPostProcessor>();
//			for (BeanFactoryPostProcessor postProcessor : getBeanFactoryPostProcessors()) {
//				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
//					BeanDefinitionRegistryPostProcessor registryPostProcessor =
//							(BeanDefinitionRegistryPostProcessor) postProcessor;
//					registryPostProcessor.postProcessBeanDefinitionRegistry(registry);
//					registryPostProcessors.add(registryPostProcessor);
//				}
//				else {
//					regularPostProcessors.add(postProcessor);
//				}
//			}
//			Map<String, BeanDefinitionRegistryPostProcessor> beanMap =
//					beanFactory.getBeansOfType(BeanDefinitionRegistryPostProcessor.class, true, false);
//			List<BeanDefinitionRegistryPostProcessor> registryPostProcessorBeans =
//					new ArrayList<BeanDefinitionRegistryPostProcessor>(beanMap.values());
//			OrderComparator.sort(registryPostProcessorBeans);
//			for (BeanDefinitionRegistryPostProcessor postProcessor : registryPostProcessorBeans) {
//				postProcessor.postProcessBeanDefinitionRegistry(registry);
//			}
//			invokeBeanFactoryPostProcessors(registryPostProcessors, beanFactory);
//			invokeBeanFactoryPostProcessors(registryPostProcessorBeans, beanFactory);
//			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
//			processedBeans.addAll(beanMap.keySet());
//		}
//		else {
//			// Invoke factory processors registered with the context instance.
//			invokeBeanFactoryPostProcessors(getBeanFactoryPostProcessors(), beanFactory);
//		}
//
//		// Do not initialize FactoryBeans here: We need to leave all regular beans
//		// uninitialized to let the bean factory post-processors apply to them!
//		String[] postProcessorNames =
//				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
//
//		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
//		// Ordered, and the rest.
//		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
//		List<String> orderedPostProcessorNames = new ArrayList<String>();
//		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
//		for (String ppName : postProcessorNames) {
//			if (processedBeans.contains(ppName)) {
//				// skip - already processed in first phase above
//			}
//			else if (isTypeMatch(ppName, PriorityOrdered.class)) {
//				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
//			}
//			else if (isTypeMatch(ppName, Ordered.class)) {
//				orderedPostProcessorNames.add(ppName);
//			}
//			else {
//				nonOrderedPostProcessorNames.add(ppName);
//			}
//		}
//
//		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
//		OrderComparator.sort(priorityOrderedPostProcessors);
//		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);
//
//		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
//		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
//		for (String postProcessorName : orderedPostProcessorNames) {
//			orderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
//		}
//		OrderComparator.sort(orderedPostProcessors);
//		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);
//
//		// Finally, invoke all other BeanFactoryPostProcessors.
//		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
//		for (String postProcessorName : nonOrderedPostProcessorNames) {
//			nonOrderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
//		}
//		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
	}

//	/**
//	 * Invoke the given BeanFactoryPostProcessor beans.
//	 */
//	private void invokeBeanFactoryPostProcessors(
//			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {
//
//		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
//			postProcessor.postProcessBeanFactory(beanFactory);
//		}
//	}

	/**
	 * Instantiate and invoke all registered processor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before any instantiation of application beans.
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : postProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
		//TODO 优先级和顺序BeanPostProcessors 暂不支持
	}

	/**
	 * Register the given processor beans.
	 */
	private void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 */
	protected void initMessageSource() {
//		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
//		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
//			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
//			// Make MessageSource aware of parent MessageSource.
//			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
//				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
//				if (hms.getParentMessageSource() == null) {
//					// Only set parent context as parent MessageSource if no parent MessageSource
//					// registered already.
//					hms.setParentMessageSource(getInternalParentMessageSource());
//				}
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("Using MessageSource [" + this.messageSource + "]");
//			}
//		}
//		else {
//			// Use empty MessageSource to be able to accept getMessage calls.
//			DelegatingMessageSource dms = new DelegatingMessageSource();
//			dms.setParentMessageSource(getInternalParentMessageSource());
//			this.messageSource = dms;
//			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_NAME +
//						"': using default [" + this.messageSource + "]");
//			}
//		}
	}

	/**
	 * Initialize the ApplicationEventMulticaster.
	 * Uses SimpleApplicationEventMulticaster if none defined in the context.
	 */
	protected void initApplicationEventMulticaster() {
//		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
//		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
//			this.applicationEventMulticaster =
//					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
//			}
//		}
//		else {
//			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
//			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
//						APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
//						"': using default [" + this.applicationEventMulticaster + "]");
//			}
//		}
	}

	/**
	 * Initialize the LifecycleProcessor.
	 * Uses DefaultLifecycleProcessor if none defined in the context.
	 */
	protected void initLifecycleProcessor() {
//		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
//		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
//			this.lifecycleProcessor =
//					beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
//			}
//		}
//		else {
//			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
//			defaultProcessor.setBeanFactory(beanFactory);
//			this.lifecycleProcessor = defaultProcessor;
//			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Unable to locate LifecycleProcessor with name '" +
//						LIFECYCLE_PROCESSOR_BEAN_NAME +
//						"': using default [" + this.lifecycleProcessor + "]");
//			}
//		}
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * Called on initialization of special beans, before instantiation of singletons.
	 * <p>This implementation is empty.
	 * @throws BeansException in case of errors
	 * @see #refresh()
	 */
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	protected void registerListeners() {
//		// Register statically specified listeners first.
//		for (ApplicationListener<?> listener : getApplicationListeners()) {
//			getApplicationEventMulticaster().addApplicationListener(listener);
//		}
//
//		// Do not initialize FactoryBeans here: We need to leave all regular beans
//		// uninitialized to let post-processors apply to them!
//		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
//		for (String listenerBeanName : listenerBeanNames) {
//			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
//		}
	}

	/**
	 * Finish the initialization of this context's bean factory,
	 * initializing all remaining singleton beans.
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
//		// Initialize conversion service for this context.
//		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
//				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
//			beanFactory.setConversionService(
//					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
//		}
//
//		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
//		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
//		for (String weaverAwareName : weaverAwareNames) {
//			getBean(weaverAwareName);
//		}
//
//		// Stop using the temporary ClassLoader for type matching.
//		beanFactory.setTempClassLoader(null);
//
//		// Allow for caching all bean definition metadata, not expecting further changes.
//		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * Finish the refresh of this context, invoking the LifecycleProcessor's
	 * onRefresh() method and publishing the
	 */
	protected void finishRefresh() {
		// Initialize lifecycle processor for this context.
		initLifecycleProcessor();

//		// Propagate refresh to lifecycle processor first.
//		getLifecycleProcessor().onRefresh();
//
//		// Publish the final event.
//		publishEvent(new ContextRefreshedEvent(this));
//
//		// Participate in LiveBeansView MBean, if active.
//		LiveBeansView.registerApplicationContext(this);
	}

	/**
	 * Cancel this context's refresh attempt, resetting the {@code active} flag
	 * after an exception got thrown.
	 * @param ex the exception that led to the cancellation
	 */
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.activeMonitor) {
			this.active = false;
		}
	}


	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * @see Runtime#addShutdownHook
	 * @see #close()
	 * @see #doClose()
	 */
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					doClose();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	/**
	 * DisposableBean callback for destruction of this instance.
	 * Only called when the ApplicationContext itself is running
	 * as a bean in another BeanFactory or ApplicationContext,
	 * which is rather unusual.
	 * <p>The {@code close} method is the native way to
	 * shut down an ApplicationContext.
	 * @see #close()
	 */
	public void destroy() {
		close();
	}

	/**
	 * Close this application context, destroying all beans in its bean factory.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
	 * @see #doClose()
	 * @see #registerShutdownHook()
	 */
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
				}
			}
		}
	}

	/**
	 * Actually performs context closing: publishes a ContextClosedEvent and
	 * destroys the singletons in the bean factory of this application context.
	 * <p>Called by both {@code close()} and a JVM shutdown hook, if any.
	 * @see #destroyBeans()
	 * @see #close()
	 * @see #registerShutdownHook()
	 */
	protected void doClose() {
		boolean actuallyClose;
		synchronized (this.activeMonitor) {
			actuallyClose = this.active && !this.closed;
			this.closed = true;
		}

		if (actuallyClose) {
			if (logger.isInfoEnabled()) {
				logger.info("Closing " + this);
			}

//			LiveBeansView.unregisterApplicationContext(this);
//
//			try {
//				// Publish shutdown event.
//				publishEvent(new ContextClosedEvent(this));
//			}
//			catch (Throwable ex) {
//				logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
//			}
//
//			// Stop all Lifecycle beans, to avoid delays during individual destruction.
//			try {
//				getLifecycleProcessor().onClose();
//			}
//			catch (Throwable ex) {
//				logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
//			}

			// Destroy all cached singletons in the context's BeanFactory.
			destroyBeans();

			// Close the state of this context itself.
			closeBeanFactory();

			// Let subclasses do some final clean-up if they wish...
			onClose();

			synchronized (this.activeMonitor) {
				this.active = false;
			}
		}
	}

	/**
	 * Template method for destroying all beans that this context manages.
	 * The default implementation destroy all cached singletons in this context,
	 * invoking {@code DisposableBean.destroy()} and/or the specified
	 * "destroy-method".
	 * <p>Can be overridden to add context-specific bean destruction steps
	 * right before or right after standard singleton destruction,
	 * while the context's BeanFactory is still active.
	 * @see #getBeanFactory()
	 */
	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}

	/**
	 * Template method which can be overridden to add context-specific shutdown work.
	 * The default implementation is empty.
	 * <p>Called at the end of {@link #doClose}'s shutdown procedure, after
	 * this context's BeanFactory has been closed. If custom shutdown logic
	 * needs to execute while the BeanFactory is still active, override
	 * the {@link #destroyBeans()} method instead.
	 */
	protected void onClose() {
		// For subclasses: do nothing by default.
	}

	public boolean isActive() {
		synchronized (this.activeMonitor) {
			return this.active;
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(requiredType);
	}

	public Object getBean(String name, Object... args) throws BeansException {
		return getBeanFactory().getBean(name, args);
	}

	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isPrototype(name);
	}

	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isTypeMatch(name, targetType);
	}

	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	//---------------------------------------------------------------------

	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanFactory().getBeanNamesForType(type);
	}

	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

//	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
//			throws BeansException {
//
//		return getBeanFactory().getBeansWithAnnotation(annotationType);
//	}
//
//	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
//		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
//	}


//	//---------------------------------------------------------------------
//	// Implementation of HierarchicalBeanFactory interface
//	//---------------------------------------------------------------------
//
//	public BeanFactory getParentBeanFactory() {
//		return getParent();
//	}
//
//	public boolean containsLocalBean(String name) {
//		return getBeanFactory().containsLocalBean(name);
//	}
//
//	/**
//	 * Return the internal bean factory of the parent context if it implements
//	 * ConfigurableApplicationContext; else, return the parent context itself.
//	 * @see org.springframework.context.ConfigurableApplicationContext#getBeanFactory
//	 */
//	protected BeanFactory getInternalParentBeanFactory() {
//		return (getParent() instanceof ConfigurableApplicationContext) ?
//				((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent();
//	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource interface
	//---------------------------------------------------------------------

//	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
//		return getMessageSource().getMessage(code, args, defaultMessage, locale);
//	}
//
//	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
//		return getMessageSource().getMessage(code, args, locale);
//	}
//
//	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
//		return getMessageSource().getMessage(resolvable, locale);
//	}
//
//	/**
//	 * Return the internal MessageSource used by the context.
//	 * @return the internal MessageSource (never {@code null})
//	 * @throws IllegalStateException if the context has not been initialized yet
//	 */
//	private MessageSource getMessageSource() throws IllegalStateException {
//		if (this.messageSource == null) {
//			throw new IllegalStateException("MessageSource not initialized - " +
//					"call 'refresh' before accessing messages via the context: " + this);
//		}
//		return this.messageSource;
//	}
//
//	/**
//	 * Return the internal message source of the parent context if it is an
//	 * AbstractApplicationContext too; else, return the parent context itself.
//	 */
//	protected MessageSource getInternalParentMessageSource() {
//		return (getParent() instanceof AbstractApplicationContext) ?
//			((AbstractApplicationContext) getParent()).messageSource : getParent();
//	}
//
//
//	//---------------------------------------------------------------------
//	// Implementation of ResourcePatternResolver interface
//	//---------------------------------------------------------------------
//
//	public Resource[] getResources(String locationPattern) throws IOException {
//		return this.resourcePatternResolver.getResources(locationPattern);
//	}
//
//
//	//---------------------------------------------------------------------
//	// Implementation of Lifecycle interface
//	//---------------------------------------------------------------------
//
//	public void start() {
//		getLifecycleProcessor().start();
//		publishEvent(new ContextStartedEvent(this));
//	}
//
//	public void stop() {
//		getLifecycleProcessor().stop();
//		publishEvent(new ContextStoppedEvent(this));
//	}
//
//	public boolean isRunning() {
//		return (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning());
//	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by {@link #refresh()} before any other initialization work.
	 * <p>A subclass will either create a new bean factory and hold a reference to it,
	 * or return a single BeanFactory instance that it holds. In the latter case, it will
	 * usually throw an IllegalStateException if refreshing the context more than once.
	 * @throws BeansException if initialization of the bean factory failed
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 */
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	/**
	 * Subclasses must implement this method to release their internal bean factory.
	 * This method gets invoked by {@link #close()} after all other shutdown work.
	 * <p>Should never throw an exception but rather log shutdown failures.
	 */
	protected abstract void closeBeanFactory();

	/**
	 * Subclasses must return their internal bean factory here. They should implement the
	 * lookup efficiently, so that it can be called repeatedly without a performance penalty.
	 * <p>Note: Subclasses should check whether the context is still active before
	 * returning the internal bean factory. The internal factory should generally be
	 * considered unavailable once the context has been closed.
	 * @return this application context's internal bean factory (never {@code null})
	 * @throws IllegalStateException if the context does not hold an internal bean factory yet
	 * (usually if {@link #refresh()} has never been called) or if the context has been
	 * closed already
	 * @see #refreshBeanFactory()
	 * @see #closeBeanFactory()
	 */
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;



	/**
	 * processor that logs an info message when a bean is created during
	 * processor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private class BeanPostProcessorChecker implements BeanPostProcessor {

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
//			if (bean != null && !(bean instanceof processor) &&
//					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
//				if (logger.isInfoEnabled()) {
//					logger.info("Bean '" + beanName + "' of type [" + bean.getClass() +
//							"] is not eligible for getting processed by all BeanPostProcessors " +
//							"(for example: not eligible for auto-proxying)");
//				}
//			}
			return bean;
		}
	}


//	/**
//	 * processor that detects beans which implement the ApplicationListener interface.
//	 * This catches beans that can't reliably be detected by getBeanNamesForType.
//	 */
//	private class ApplicationListenerDetector implements MergedBeanDefinitionPostProcessor {
//
//		private final Map<String, Boolean> singletonNames = new ConcurrentHashMap<String, Boolean>(64);
//
//		public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
//			if (beanDefinition.isSingleton()) {
//				this.singletonNames.put(beanName, Boolean.TRUE);
//			}
//		}
//
//		public Object postProcessBeforeInitialization(Object bean, String beanName) {
//			return bean;
//		}
//
//		public Object postProcessAfterInitialization(Object bean, String beanName) {
//			if (bean instanceof ApplicationListener) {
//				// potentially not detected as a listener by getBeanNamesForType retrieval
//				Boolean flag = this.singletonNames.get(beanName);
//				if (Boolean.TRUE.equals(flag)) {
//					// singleton bean (top-level or inner): register on the fly
//					addApplicationListener((ApplicationListener<?>) bean);
//				}
//				else if (flag == null) {
//					if (logger.isWarnEnabled() && !containsBean(beanName)) {
//						// inner bean with other scope - can't reliably process events
//						logger.warn("Inner bean '" + beanName + "' implements ApplicationListener interface " +
//								"but is not reachable for event multicasting by its containing ApplicationContext " +
//								"because it does not have singleton scope. Only top-level listener beans are allowed " +
//								"to be of non-singleton scope.");
//					}
//					this.singletonNames.put(beanName, Boolean.FALSE);
//				}
//			}
//			return bean;
//		}
//	}

}
