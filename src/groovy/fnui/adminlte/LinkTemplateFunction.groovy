package fnui.adminlte

import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.extension.TemplateFunction
import fnui.model.AuiLink

class LinkTemplateFunction extends TemplateFunction {
    LinkTemplateFunction() {
        super('generateLink')
    }

    @Override
    Writable executeFunction(UiGeneratorTemplateContext context, List arguments) {
        validateParameters(arguments, [AuiLink, String, Map, Writable])

        translateTemplate('function/LinkFunction.gsp', context, [link: arguments[0], contextVar: arguments[1], attributes:arguments[2], body: arguments[3]])
    }
}
