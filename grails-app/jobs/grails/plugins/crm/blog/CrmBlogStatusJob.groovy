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

class CrmBlogStatusJob {
    static triggers = {
        simple name: 'crmBlogStatus', startDelay: 1000 * 60 * 2, repeatInterval: 1000 * 60 * 3 // every 3 minutes
        cron name: 'crmBlogStatus', cronExpression: "0 15 0 * * ?" // every day at 00:15
    }

    def group = 'crmBlog'
    def concurrent = false

    def crmBlogService

    def execute() {
        final Date now = new Date()
        final List<CrmBlogPost> needsUpdate = CrmBlogPost.createCriteria().list() {
            status {
                eq('param', 'published')
            }
            or {
                gt('visibleFrom', now)
                lt('visibleTo', now)
            }
        }
        for (post in needsUpdate) {
            try {
                crmBlogService.archiveBlogPost(post)
            } catch (Exception e) {
                log.error("Failed to update status on crmBlogPost@${post.id}", e)
            }
        }
    }
}