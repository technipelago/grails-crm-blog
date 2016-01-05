# GR8 CRM - Blog Support

CRM = [Customer Relationship Management](http://en.wikipedia.org/wiki/Customer_relationship_management)

GR8 CRM is a set of [Grails Web Application Framework](http://www.grails.org/)
plugins that makes it easy to develop web application with CRM functionality.
With CRM we mean features like:

- Contact Management
- Task/Todo Lists
- Project Management


## Blog Support

This plugin provides domain classes and services for blog/newsletter authoring in GR8 CRM applications.
For a user interface see the [crm-blog-ui](https://github.com/technipelago/grails-crm-blog-ui) plugin.

## Examples

    def draft = crmBlogService.createBlogStatus(name: 'Draft', param: 'draft', true)
    def published = crmBlogService.createBlogStatus(name: 'Published', param: 'published', true)
    def archived = crmBlogService.createBlogStatus(name: 'Archived', param: 'archived', true)

    def post = crmBlogService.createBlogPost(status: published, title: 'GR8 CRM', description: "I'm very proud to present my latest project", true)

    post.setTagValue('groovy').setTagValue('grails').setTagValue('crm')

    crmContentService.createResource('<h1>GR8 CRM</h1><p>Lorem ipsum... This is the actual content posted to the blog.</p>', 'content.html', post)