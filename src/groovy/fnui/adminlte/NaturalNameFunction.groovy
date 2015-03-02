package fnui.adminlte

import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.extension.TemplateFunction
import fnui.generator.util.WritableString
import grails.util.GrailsNameUtils

class NaturalNameFunction extends TemplateFunction {
    NaturalNameFunction() {
        super('naturalName')
    }

    @Override
    Writable executeFunction(UiGeneratorTemplateContext context, List arguments) {
        validateParameters(arguments, [String])

        new WritableString(GrailsNameUtils.getNaturalName(arguments[0]))
    }
}
