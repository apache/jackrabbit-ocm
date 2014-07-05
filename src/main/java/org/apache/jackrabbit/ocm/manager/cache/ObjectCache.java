/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.ocm.manager.cache;

/**
 *
 *
 * Interface use for an object cache.
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Lombart Christophe </a>
 *
 */
public interface ObjectCache {

    /**
     * Put an object to current session's object cache
     *
     * @param path Path to object
     * @param object Cached object
     */
    void cache(String path, Object object);

    /**
     * Clear current session's object cache
     */
    void clear();

    /**
     * Check for object presence in the current session's object cache If the object is not in the current session's
     * object cache, it is copied to current session's object cache from the global object cache.
     *
     * @param path Path to object
     * @return true if the object is present in the current session's object cache
     */
    boolean isCached(String path);

    /**
     * Get the object from the current session's object cache. If the object is not in the current session's object
     * cache, it is copied to current session's object cache from the global object cache.
     *
     * @param path Path to object
     * @return Object (perhaps partially loaded), or null otherwise
     */
    Object getObject(String path);

    /**
     * Remove an object from the current session's object cache. Also remove the object and all its subobjects from the
     * global object cache.
     *
     * @param path Path to object
     */
    void evict(String path);

    /**
     * Transfer a fully loaded object into the global object cache.
     *
     * @param path Path to object
     * @param object A fully loaded object
     */
    void ready(String path, Object object);

}
