package grails.plugins.crm.blog

import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.TenantEntity

@TenantEntity
@AuditEntity
class CrmBlogPost {

    public static final List BIND_WHITELIST = ['title', 'description', 'username', 'status', 'date', 'visibleFrom', 'visibleTo', 'parser']

    String title
    String description
    String username
    CrmBlogStatus status
    Date date
    Date visibleFrom
    Date visibleTo
    String parser = 'raw'

    static constraints = {
        title(maxSize: 255, blank: false)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        username(maxSize: 80, nullable: true)
        visibleFrom(nullable: true)
        visibleTo(nullable: true)
        parser(maxSize: 20, blank: false)
    }

    static mapping = {
        sort 'date': 'desc'
    }

    static transients = ['active']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true
    static auditable = true

    def beforeValidate() {
        if (date == null) {
            date = new Date()
        }
    }

    transient boolean isActive() {
        status?.isActive()
    }

    @Override
    String toString() {
        title.toString()
    }

    Map<String, Object> getDao() {
        [id: id, title: title, name: title, description: description, username: username, parser: parser,
                date: date, status: [name: status.name, param: status.param], tags: { this.getTagValue() }]
    }
}
