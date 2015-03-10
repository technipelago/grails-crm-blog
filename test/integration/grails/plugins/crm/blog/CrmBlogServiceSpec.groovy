package grails.plugins.crm.blog

import grails.test.spock.IntegrationSpec
import grails.plugins.crm.core.TenantUtils

/**
 * Tests for CrmBlogService.
 */
class CrmBlogServiceSpec extends IntegrationSpec {

    def grailsApplication
    def crmBlogService

    void "test archive status"() {
        when:
        CrmBlogPost post = TenantUtils.withTenant(1L) {
            def published = crmBlogService.createBlogStatus(name: "Published", param: "published").save(failOnError: true)
            crmBlogService.createBlogStatus(name: "Archived", param: "archived").save(failOnError: true)
            crmBlogService.createBlogPost(status: published, title: "Test", true)
        }

        then:
        post.status.param == 'published'

        when:
        crmBlogService.archiveBlogPost(post)

        then:
        post.status.param == 'archived'
    }

    void "test archiving quartz job"() {
        given:
        def job = new CrmBlogStatusJob()
        job.grailsApplication = grailsApplication
        job.crmBlogService = crmBlogService

        when:
        TenantUtils.withTenant(1L) {
            def published = crmBlogService.createBlogStatus(name: "Published", param: "published").save(failOnError: true)
            crmBlogService.createBlogStatus(name: "Archived", param: "archived").save(failOnError: true)
            crmBlogService.createBlogPost(status: published, title: "Old", visibleTo: new Date() - 2, true)
            crmBlogService.createBlogPost(status: published, title: "Current", visibleFrom: new Date() - 1, visibleTo: new Date() + 1, true)
            crmBlogService.createBlogPost(status: published, title: "Future", visibleFrom: new Date() + 1, true)
        }

        then:
        CrmBlogPost.createCriteria().list { status { eq('param', 'published') } }.size() == 3

        when: "execute quartz job disabled by configuration"
        grailsApplication.config.crm.blog.job.status.enabled = false
        job.execute()

        then: "all posts are still published"
        CrmBlogPost.createCriteria().list { status { eq('param', 'published') } }.size() == 3

        when:
        grailsApplication.config.crm.blog.job.status.enabled = true
        job.execute()

        then: "old posts were archived"
        CrmBlogPost.createCriteria().list { status { eq('param', 'published') } }.size() == 1
    }
}
