/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.config.impl;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to track multiple services in cascade.
 * As it's exclusively using the ServiceTracker helper class,
 * the whole tracking mechanism is thread safe.
 */
public class ServiceTrackerHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceTrackerHelper.class);

    private final BundleContext context;

    private ServiceTrackerHelper(BundleContext context) {
        this.context = context;
    }

    /**
     * Build a ServiceTrackerHelper using the given BundleContext.
     */
    public static ServiceTrackerHelper helper(BundleContext bundleContext) {
        return new ServiceTrackerHelper(bundleContext);
    }

    /**
     * Start tracking a service for class S, and chain with another service tracking call.
     *
     * @param clazz the service class
     * @param consumer a function receiving the tracked service and chaining with another service tracking call
     * @param <S> the tracked service class
     * @param <T> the chained service tracker class
     * @return an opened service tracker
     */
    public <S, T extends ServiceTracker<?, ?>> ServiceTracker<S, T> track(
            Class<S> clazz,
            Function<S, T> consumer
    ) {
        return track(clazz, defaultFilter(clazz), consumer, ServiceTracker::close);
    }

    /**
     * Start tracking a service for class S with a given filter, and chain with another service tracking call.
     * If a null filter is given, the service tracking is completely bypassed and a null value will be
     * immediately given to the consumer.
     *
     * @param clazz the service class
     * @param filter the filter to use
     * @param consumer a function receiving the tracked service and chaining with another service tracking call
     * @param <S> the tracked service class
     * @param <T> the chained service tracker class
     * @return an opened service tracker
     */
    public <S, T extends ServiceTracker<?, ?>> ServiceTracker<S, T> track(
            Class<S> clazz,
            String filter,
            Function<S, T> consumer
    ) {
        return track(clazz, filter, consumer, ServiceTracker::close);
    }

    /**
     * Start tracking a service for class S with a given filter, and create the final object.
     *
     * @param clazz the service class
     * @param creator a function receiving the tracked service and creating the final object
     * @param destroyer a callback to destroy the final object when the tracked service is lost
     * @param <S> the tracked service class
     * @param <T> the final object type
     * @return an opened service tracker
     */
    public <S, T> ServiceTracker<S, T> track(
            Class<S> clazz,
            Function<S, T> creator,
            Consumer<T> destroyer
    ) {
        return track(clazz, defaultFilter(clazz), creator, destroyer);
    }

    /**
     * Start tracking a service for class S with a given filter, and create the final object.
     * If a null filter is given, the service tracking is completely bypassed and a null value will be
     * immediately given to the consumer.
     *
     * @param clazz the service class
     * @param filter the filter to use
     * @param creator a function receiving the tracked service and creating the final object
     * @param destroyer a callback to destroy the final object when the tracked service is lost
     * @param <S> the tracked service class
     * @param <T> the final object type
     * @return an opened service tracker
     */
    public <S, T> ServiceTracker<S, T> track(
            Class<S> clazz,
            String filter,
            Function<S, T> creator,
            Consumer<T> destroyer
    ) {
        if (filter != null) {
            ServiceTracker<S, T> tracker = new ServiceTracker<S, T>(context, getOrCreateFilter(filter), null) {
                @Override
                public T addingService(ServiceReference<S> reference) {
                    LOGGER.debug("Obtained service dependency: " + filter);
                    S s = context.getService(reference);
                    return creator.apply(s);
                }
                @Override
                public void removedService(ServiceReference<S> reference, T service) {
                    LOGGER.debug("Lost service dependency: " + filter);
                    destroyer.accept(service);
                    context.ungetService(reference);
                }
            };
            tracker.open();
            if (tracker.isEmpty()) {
                LOGGER.debug("Waiting for service dependency: " + filter);
            }
            return tracker;
        } else {
            T t = creator.apply(null);
            return new ServiceTracker<S, T>(context, clazz, null) {
                @Override
                public void close() {
                    destroyer.accept(t);
                }
            };
        }
    }

    /**
     * Start tracking a service for class S with a given filter, and create the final object.
     * If a null filter is given, the service tracking is completely bypassed and a null value will be
     * immediately given to the consumer.
     *
     * @param clazz the service class
     * @param filter the filter to use
     * @param creator a function receiving the tracked service plus its {@link ServiceReference} and creating the final object
     * @param destroyer a callback to destroy the final object when the tracked service is lost
     * @param <S> the tracked service class
     * @param <T> the final object type
     * @return an opened service tracker
     */
    public <S, T> ServiceTracker<S, T> track(
            Class<S> clazz,
            String filter,
            BiFunction<S, ServiceReference<S>, T> creator,
            Consumer<T> destroyer
    ) {
        if (filter != null) {
            ServiceTracker<S, T> tracker = new ServiceTracker<S, T>(context, getOrCreateFilter(filter), null) {
                @Override
                public T addingService(ServiceReference<S> reference) {
                    LOGGER.debug("Obtained service dependency: " + filter);
                    S s = context.getService(reference);
                    return creator.apply(s, reference);
                }
                @Override
                public void removedService(ServiceReference<S> reference, T service) {
                    LOGGER.debug("Lost service dependency: " + filter);
                    destroyer.accept(service);
                    context.ungetService(reference);
                }
            };
            tracker.open();
            if (tracker.isEmpty()) {
                LOGGER.debug("Waiting for service dependency: " + filter);
            }
            return tracker;
        } else {
            T t = creator.apply(null, null);
            return new ServiceTracker<S, T>(context, clazz, null) {
                @Override
                public void close() {
                    destroyer.accept(t);
                }
            };
        }
    }

    private String defaultFilter(Class<?> clazz) {
        return "(" + Constants.OBJECTCLASS + "=" + clazz.getName() + ")";
    }

    private Filter getOrCreateFilter(String filter) {
        try {
            return context.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unable to create filter", e);
        }
    }

}
