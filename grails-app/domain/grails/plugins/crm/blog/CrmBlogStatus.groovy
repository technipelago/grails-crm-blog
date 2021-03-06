/*
 * Copyright (c) 2013 Goran Ehrsson.
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

package grails.plugins.crm.blog

import grails.plugins.crm.core.CrmLookupEntity

/**
 * Blog post status (draft, published, archived, ...)
 */
class CrmBlogStatus extends CrmLookupEntity {

    public static final String DRAFT = "draft"
    public static final String PUBLISHED = "published"
    public static final String ARCHIVED = "archived"

    public static final List INACTIVE_STATUSES = [9, 19, 29, 39, 49, 59, 69, 79, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 199, 299, 399, 499, 599, 699, 799, 899, 999]

    static transients = ['active']

    boolean isActive() {
        !INACTIVE_STATUSES.contains(orderIndex)
    }
}
