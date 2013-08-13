/*
 *  Copyright 2012 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

import org.codehaus.groovy.grails.commons.GrailsClassUtils

import org.springframework.web.multipart.commons.CommonsMultipartFile

class CrmBlogGrailsPlugin {
    // Dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    def loadAfter = ['crmContent']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/domain/grails/plugins/crm/blog/TestContentEntity.groovy",
            "src/groovy/grails/plugins/crm/blog/TestSecurityDelegate.groovy",
            "src/templates/text/**",
            "grails-app/views/error.gsp"
    ]
    def title = "Blog Support for Grails CRM"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
This plugin provide storage and services for managing blogs in Grails CRM.
'''
    def documentation = "http://grails.org/plugin/crm-blog"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-blog/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-blog"]

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
    }

}
