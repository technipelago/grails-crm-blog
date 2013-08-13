package grails.plugins.crm.blog

import grails.validation.Validateable

/**
 * Blog query by example helper command.
 */
@Validateable
class CrmBlogQueryCommand implements Serializable {

    String title
    String status
    String username
    String fromDate
    String toDate
}
