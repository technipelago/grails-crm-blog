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

import grails.events.Listener
import grails.plugins.crm.content.CrmResourceRef
import grails.plugins.crm.core.DateUtils
import grails.plugins.crm.core.SearchUtils
import grails.plugins.crm.core.TenantUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod

class CrmBlogService {

    def grailsApplication
    def crmSecurityService
    def crmContentService
    def crmTagService
    def messageSource

    @Listener(namespace = "crmBlog", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = crmSecurityService.getTenantInfo(event.tenant)
        if (!tenant) {
            throw new IllegalArgumentException("Cannot find tenant info for tenant [${event.tenant}], event=$event")
        }
        def config = grailsApplication.config.crm.blog
        def locale = tenant.locale
        TenantUtils.withTenant(tenant.id) {
            crmTagService.createTag(name: CrmBlogPost.name, multiple: true)

            createDefaultBlogStatus(config.status.draft ?: 'draft', 'crmBlogStatus.name.draft', 'Draft', locale)
            createDefaultBlogStatus(config.status.published ?: 'published', 'crmBlogStatus.name.published', 'Published', locale)
            createDefaultBlogStatus(config.status.archived ?: 'archived', 'crmBlogStatus.name.archived', 'Archived', locale)
        }
    }

    private CrmBlogStatus createDefaultBlogStatus(String param, String code, String defaultText, Locale locale) {
        def s = messageSource.getMessage(code, null, defaultText, locale)
        createBlogStatus(name: s, param: param).save(failOnError: true)
    }

    @Listener(namespace = "crmTenant", topic = "requestDelete")
    def requestDeleteTenant(event) {
        def tenant = event.id
        def count = 0
        count += CrmBlogPost.countByTenantId(tenant)
        count += CrmBlogStatus.countByTenantId(tenant)
        count ? [namespace: 'crmBlog', topic: 'deleteTenant'] : null
    }

    @Listener(namespace = "crmBlog", topic = "deleteTenant")
    def deleteTenant(event) {
        def tenant = event.id
        def result = CrmBlogPost.findAllByTenantId(tenant)
        result*.delete()
        CrmBlogStatus.findAllByTenantId(tenant)*.delete()
        log.warn("Deleted ${result.size()} blog posts in tenant $tenant")
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of CrmBlogPost domain instances
     */
    def list(Map params = [:]) {
        listBlogPosts([:], params)
    }

    /**
     * Find CrmBlogPost instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmBlogPost domain instances
     */
    def list(Map query, Map params) {
        listBlogPosts(query, params)
    }

    /**
     * Find CrmBlogPost instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmBlogPost domain instances
     */
    def listBlogPosts(Map query, Map params) {
        def tagged
        if (query.tags) {
            tagged = crmTagService.findAllIdByTag(CrmBlogPost, query.tags) ?: [0L]
        }

        CrmBlogPost.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (tagged) {
                inList('id', tagged)
            }
            if (query.exclude) {
                not {
                    inList('id', query.exclude)
                }
            }
            if (query.title) {
                ilike('title', SearchUtils.wildcard(query.title))
            }
            if (query.username) {
                eq('username', query.username)
            }
            if (query.status) {
                status {
                    if (query.status instanceof Collection) {
                        inList('param', query.status)
                    } else {
                        or {
                            ilike('name', SearchUtils.wildcard(query.status))
                            eq('param', query.status)
                        }
                    }
                }
            }
            if (query.fromDate && query.toDate) {
                def timezone = query.timezone ?: TimeZone.getDefault()
                def d1 = query.fromDate instanceof Date ? query.fromDate : DateUtils.parseDate(query.fromDate, timezone)
                def d2 = query.toDate instanceof Date ? query.toDate : DateUtils.parseDate(query.toDate, timezone)
                d1.clearTime()
                d2 = DateUtils.getDateSpan(d2)[1]
                between('date', d1, d2)
            } else if (query.fromDate) {
                def timezone = query.timezone ?: TimeZone.getDefault()
                def d1 = DateUtils.parseDate(query.fromDate, timezone)
                d1.clearTime()
                ge('date', d1)
            } else if (query.toDate) {
                def timezone = query.timezone ?: TimeZone.getDefault()
                def d2 = DateUtils.parseDate(query.toDate, timezone)
                d2 = DateUtils.getDateSpan(d2)[1]
                le('date', d2)
            }
        }
    }

    CrmBlogPost createBlogPost(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def m = new CrmBlogPost()
        def args = [m, params, [include: CrmBlogPost.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(m, 'bind', args.toArray())
        m.tenantId = tenant
        if (save) {
            m.save()
        } else {
            m.validate()
            m.clearErrors()
        }
        return m
    }

    CrmBlogPost getBlogPost(Long id) {
        CrmBlogPost.findByIdAndTenantId(id, TenantUtils.tenant, [cache: true])
    }

    CrmBlogPost findByName(String name) {
        CrmBlogPost.findByNameAndTenantId(name, TenantUtils.tenant, [cache: true])
    }

    CrmResourceRef getBlogContent(final CrmBlogPost post, String contentName = 'content.html') {
        crmContentService.findResourcesByReference(post, [name: contentName]).find { it }
    }

    def listBlogStatus(Map params = [:]) {
        CrmBlogStatus.findAllByTenantId(TenantUtils.tenant, params)
    }

    CrmBlogStatus getBlogStatus(String param) {
        CrmBlogStatus.findByParamAndTenantId(param, TenantUtils.tenant, [cache: true])
    }

    CrmBlogStatus createBlogStatus(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        if (!params.param && params.name) {
            params.param = paramify(params.name, new CrmBlogStatus().constraints.param.maxSize)
        }
        def m = CrmBlogStatus.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmBlogStatus(params)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updateBlogStatus(CrmBlogStatus status, Map params) {
        def args = [status, params]
        new BindDynamicMethod().invoke(status, 'bind', args.toArray())
        return status.validate()
    }

    def deleteBlogStatus(CrmBlogStatus status) {
        status.delete()
    }

    @CompileStatic
    private String paramify(final String name, Integer maxSize = 20) {
        def param = name.toLowerCase().replace(' ', '-')
        if (param.length() > maxSize) {
            param = param[0..(maxSize - 1)]
            if (param[-1] == '-') {
                param = param[0..-2]
            }
        }
        return param
    }

    boolean archiveBlogPost(CrmBlogPost post) {
        def param = grailsApplication.config.crm.blog.status.archived ?: 'archived'
        def archived = CrmBlogStatus.findByParamAndTenantId(param, post.tenantId, [cache: true])
        if (archived) {
            post.status = archived
            return true
        }
        log.warn "Cannot archive crmBlogPost@${post.id} because status [archived] is not available"
        return false
    }
}
