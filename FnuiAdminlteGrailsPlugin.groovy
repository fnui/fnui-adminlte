import fnui.adminlte.LinkTemplateFunction
import fnui.adminlte.NaturalNameFunction

class FnuiAdminlteGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.4 > *"
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def loadAfter = ['fnui-generator']

    // TODO Fill in these fields
    def title = "Fnui Adminlte Plugin" // Headline display name of the plugin
    def author = "Florian Freudenberg"
    def authorEmail = "flo@freudenberg.berlin"
    def description = '''\
This plugins provides templates for the fnui-generator to generate a UI based on the AdminLTE template.
'''

    def documentation = "http://grails.org/plugin/fnui-adminlte"

    def license = "APACHE"


    def scm = [ url: "https://github.com/fnui/fnui-adminlte" ]

    def doWithSpring = {
        application.config.fnui.ui.template.provider << 'fnui-adminlte'
    }

    def configureFnuiGenerator = {
        uiGenerator.addExtensionPointTemplate('ControllerBody', 'controllers/extensions/ControllerScopeProperty.groovy')
        uiGenerator.addExtensionPointTemplate('ControllerBody', 'controllers/extensions/ControllerNavigationProperty.groovy')

        uiGenerator.addExtensionWrapperTemplate('PropertyDecoration', 'views/extensions/PropertyLinkWrapper.gsp')

        uiGenerator.addTemplateFunction(new LinkTemplateFunction())
        uiGenerator.addTemplateFunction(new NaturalNameFunction())
    }
}
