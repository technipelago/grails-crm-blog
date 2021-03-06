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

import grails.converters.JSON
import grails.plugins.crm.core.TenantUtils

import javax.servlet.http.HttpServletResponse

/**
 * REST API for CrmBlogPost access.
 */
class CrmBlogApiController {

    def crmBlogService
    def grailsLinkGenerator

    static final int ERROR_NOT_FOUND = 1001

    /**
     * Blog post REST API.
     * /posts collection of resources
     * /posts/name-of-post a single blog post
     *
     * @param id name of blog post
     * @return render JSON to the response
     */
    def posts(String id) {
        TenantUtils.withTenant(1L) {
            if (id) {
                def post = crmBlogService.findByName(id)
                if (post?.active) {
                    def result = post.dao
                    result.tags = result.tags()
                    result.href = getAbsoluteLink(post)
                    result.url = grailsLinkGenerator.link(uri: '/blog/show', absolute: true) + '/' + post.name.encodeAsURL()
                    def content = crmBlogService.getBlogContent(post)
                    if (content) {
                        def metadata = content.metadata
                        def etag = "${(post.lastUpdated ?: post.dateCreated).time}/${metadata.hash}".toString()
                        def requestETag = request.getHeader("If-None-Match")
                        if (requestETag == etag) {
                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
                            response.outputStream.flush()
                            return
                        }
                        def ms = request.getDateHeader("If-Modified-Since")
                        def modified = metadata.modified.time
                        modified = modified - (modified % 1000) // Remove milliseconds.
                        if (modified <= ms) {
                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
                            response.outputStream.flush()
                            return
                        }
                        result.text = content.text
                        response.setHeader("ETag", etag)
                    }
                    render contentType: getContentType('blog'), text: new JSON([result]).toString(true)
                } else {
                    def result = [
                            status: HttpServletResponse.SC_NOT_FOUND,
                            code: ERROR_NOT_FOUND,
                            property: 'name',
                            message: "No blog post with name '$id' found",
                            developerMessage: "No blog post with name '$id' found",
                            moreInfo: "${grailsLinkGenerator.link(uri: '', absolute: true)}/docs/api/blog.html"
                    ]
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND)
                    render contentType: getContentType('error'), text: new JSON(result).toString(true)
                }
            } else {
                def result = crmBlogService.list([status: ['published', 'archived']],
                        [offset: params.offset ?: 0, limit: params.max ?: 10, sort: 'date', order: 'desc']).collect { post ->
                    def dao = post.dao
                    dao.href = getAbsoluteLink(post)
                    dao.url = grailsLinkGenerator.link(uri: '/blog/show', absolute: true) + '/' + post.name.encodeAsURL()
                    dao.tags = dao.tags()
                    if (params.list('expand').contains('text')) {
                        dao.text = crmBlogService.getBlogContent(post)?.text
                    }
                    dao
                }
                render contentType: getContentType('blog'), text: new JSON(result).toString(true)
            }
        }
    }

    private String getContentType(String category) {
        "application/vnd.org.gr8crm.${category}+json;v=1".toString()
    }

    private String getAbsoluteLink(post) {
        def uri = grailsApplication.config.crm.api.blog.uri
        if(uri) {
            return "$uri/${post.name.encodeAsURL()}"
        } else {
            return grailsLinkGenerator.link(uri: "/api/blog/posts", absolute: true) + '/' + post.name.encodeAsURL()
        }
    }
}
