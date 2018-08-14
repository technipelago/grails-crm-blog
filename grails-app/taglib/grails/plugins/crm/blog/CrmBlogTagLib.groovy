package grails.plugins.crm.blog

import grails.plugins.crm.core.TenantUtils

/**
 * Grails CRM Blog tags.
 */
class CrmBlogTagLib {

    static namespace = "crm"

    def grailsApplication
    def crmCoreService
    def crmBlogService
    def crmContentService

    def blogPosts = { attrs, body ->
        def tenant = attrs.tenant ? Long.valueOf(attrs.tenant) : (grailsApplication.config.crm.blog.defaultTenant ?: TenantUtils.tenant)
        TenantUtils.withTenant(tenant) {
            def query = attrs.query ?: [:]
            def params = attrs.params ?: [:]
            if (!params.sort) {
                params.sort = 'date'
            }
            if (!params.order) {
                params.order = 'desc'
            }
            if (query.status == null) {
                query.status = (grailsApplication.config.crm.blog.defaultStatus ?: 'published')
            }
            def result = crmBlogService.list(query, params)
            int i = 0
            for (post in result) {
                def map = post.dao
                map.reference = crmCoreService.getReferenceIdentifier(post)
                map.template = { crmContentService.findResourcesByReference(post, [name: (attrs.template ?: 'content.html')]).find { it } }
                def model = [(attrs.var ?: 'it'): map]
                if (attrs.status) {
                    model[attrs.status] = i++
                }
                out << body(model)
            }
        }
    }

    def blogPost = { attrs, body ->
        def post = attrs.post
        if (!post) {
            throwTagError("Tag [blogPost] is missing required attribute [post]")
        }
        def map = post.dao
        map.reference = crmCoreService.getReferenceIdentifier(post)
        map.template = { crmContentService.findResourcesByReference(post, [name: (attrs.template ?: 'content.html')]).find { it } }
        def model = [(attrs.var ?: 'it'): map]
        out << body(model)
    }
}
