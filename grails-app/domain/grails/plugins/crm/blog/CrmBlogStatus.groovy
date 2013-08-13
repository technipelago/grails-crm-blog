package grails.plugins.crm.blog

import grails.plugins.crm.core.CrmLookupEntity

/**
 * Blog post status (draft, published, archived, ...)
 */
class CrmBlogStatus extends CrmLookupEntity {

    static transients = ['active']

    boolean isActive() {
        orderIndex.toString()[0] != '9'
    }
}
