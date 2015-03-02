package fnui.adminlte.navigation

import grails.util.Environment
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsControllerClass

class NavigationService {
    GrailsApplication grailsApplication

    private Navigation navigation = null

    Navigation getNavigation() {
        navigation ?: collectNavigationDescription()
    }

    private Navigation collectNavigationDescription() {
        def navigation = new Navigation('main')

        grailsApplication.controllerClasses.each { GrailsControllerClass grailsClass ->
            def navParent = navigation
            def navigationProperty = GrailsClassUtils.getStaticPropertyValue(grailsClass.clazz, 'navigation')

            if (navigationProperty && navigationProperty instanceof Map) {
                def categoryValue = navigationProperty['category']

                if (categoryValue) {
                    def category = navParent.categories[categoryValue]
                    if (!category) {
                        category = new Navigation(categoryValue)
                        navParent.categories[categoryValue] = category
                    }
                    navParent = category
                }

                def navItem = new NavigationItem()
                navItem.name = navigationProperty['name'] ?: grailsClass.shortName

                if (grailsClass.namespace) {
                    navItem.params['namespace'] = grailsClass.namespace
                }

                navItem.params['controller'] = GrailsNameUtils.getLogicalName(grailsClass.clazz, 'Controller')

                navParent.items << navItem
            }
        }

        // cache if not dev environment
        if (!Environment.isDevelopmentMode()) {
            this.navigation = navigation
        }

        return navigation
    }
}
