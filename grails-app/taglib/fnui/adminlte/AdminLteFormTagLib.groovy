package fnui.adminlte

import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.support.encoding.CodecLookup
import org.codehaus.groovy.grails.support.encoding.Encoder
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder
import org.codehaus.groovy.grails.web.util.GrailsPrintWriter
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.MessageSourceResolvable
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.support.RequestContextUtils
import org.springframework.web.servlet.support.RequestDataValueProcessor

import java.text.DateFormat
import java.text.DateFormatSymbols

/**
 * Implementation based on org.codehaus.groovy.grails.plugins.web.taglib.FormTagLib and was modified for
 * the requirements of AdminLTE.
 */
class AdminLteFormTagLib implements ApplicationContextAware, InitializingBean {
    static namespace = 'adminlte'

    ApplicationContext applicationContext
    RequestDataValueProcessor requestDataValueProcessor
    ConversionService conversionService

    CodecLookup codecLookup

    /**
     * Creates a new text field.
     *
     * @emptyTag
     *
     * @attr name REQUIRED the field name
     * @attr value the field value
     */
    Closure textField = { attrs ->
        attrs.type = 'text'
        attrs.tagName = 'textField'
        fieldImpl(out, attrs)
    }

    /**
     * Creates a new password field.
     *
     * @emptyTag
     *
     * @attr name REQUIRED the field name
     * @attr value the field value
     */
    Closure passwordField = { attrs ->
        attrs.type = "password"
        attrs.tagName = "passwordField"
        fieldImpl(out, attrs)
    }

    /**
     * Creates a hidden field.
     *
     * @attr name REQUIRED the field name
     * @attr value the field value
     */
    Closure hiddenField = { attrs ->
        hiddenFieldImpl(out, attrs)
    }

    /**
     * A general tag for creating fields.
     *
     * @attr type REQUIRED the input type
     */
    Closure field = { attrs ->
        attrs.tagName = "field"
        fieldImpl(out, attrs)
    }

    /**
     * Creates a submit button.
     *
     * @emptyTag
     *
     * @attr name REQUIRED the field name
     * @attr value the button text
     * @attr type input type; defaults to 'submit'
     * @attr event the webflow event id
     */
    Closure submitButton = { attrs ->
        attrs.tagName = 'submitButton'
        if (request.flowExecutionKey) {
            attrs.name = attrs.event ? "_eventId_${attrs.event}" : "_eventId_${attrs.name}"
        }
        if (attrs.name && (attrs.value == null)) {
            attrs.value = attrs.name
        }

        attrs['class'] = attrs['class'] ?: 'btn btn-primary'

        resolveAttributes(attrs)

        attrs.value = processFormFieldValueIfNecessary(attrs.name, attrs.value, attrs.type)

        out << '<button type="submit" '
        outputAttributes(attrs, out, true)
        out << '>'
        out << attrs.value
        out << '</button>'
    }

    /**
     * A helper tag for creating checkboxes.
     *
     * @emptyTag
     *
     * @attr name REQUIRED the name of the checkbox
     * @attr value  the value of the checkbox
     * @attr checked if evaluates to true sets to checkbox to checked
     * @attr disabled if evaluates to true sets to checkbox to disabled
     * @attr readonly if evaluates to true, sets to checkbox to read only
     * @attr id DOM element id; defaults to name
     */
    Closure checkBox = { attrs ->
        def value = attrs.remove('value')
        def name = attrs.remove('name')
        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'readonly')

        // Deal with the "checked" attribute. If it doesn't exist, we
        // default to a value of "true", otherwise we use Groovy Truth
        // to determine whether the HTML attribute should be displayed or not.
        def checked = true
        def checkedAttributeWasSpecified = false
        if (attrs.containsKey('checked')) {
            checkedAttributeWasSpecified = true
            checked = attrs.remove('checked')
        }

        if (checked instanceof String) checked = Boolean.valueOf(checked)

        if (value == null) value = false
        def hiddenValue = ""

        value = processFormFieldValueIfNecessary(name, value, "checkbox")
        hiddenValue = processFormFieldValueIfNecessary("_${name}", hiddenValue, "hidden")

        def hiddenFieldName
        if (name.indexOf('.') == -1) {
            hiddenFieldName = "_${name}"
        } else {
            def lastDot = name.lastIndexOf('.')
            hiddenFieldName = name[0..lastDot] + '_' + name[(lastDot + 1)..-1]
        }
        out << "<input type=\"hidden\" name=\"${hiddenFieldName}\""
        if (hiddenValue != "") {
            out << " value=\"${hiddenValue}\""
        }
        out << " /><input type=\"checkbox\" name=\"${name}\" "
        if (checkedAttributeWasSpecified) {
            if (checked) {
                out << 'checked="checked" '
            }
        } else if (value) {
            out << 'checked="checked" '
        }

        def outputValue = !(value instanceof Boolean || value?.getClass() == boolean)
        if (outputValue) {
            out << "value=\"${value}\" "
        }
        // process remaining attributes
        outputAttributes(attrs, out)

        if (!attrs.containsKey('id')) {
            out << """id="${name}" """
        }

        // close the tag, with no body
        out << ' />'
    }

    /**
     * A general tag for creating textareas.
     *
     * @attr name REQUIRED the name of the textarea
     * @attr value  the text of the textarea; if not specified renders the body as the text
     * @attr escapeHtml if true escapes the text as HTML
     * @attr id DOM element id; defaults to name
     */
    Closure textArea = { attrs, body ->
        resolveAttributes(attrs)
        // Pull out the value to use as content not attrib
        def value = attrs.remove('value')
        if (!value) {
            value = body()
        }

        attrs['class'] = attrs['class'] ?: 'form-control'

        boolean escapeHtml = true
        if (attrs.containsKey('escapeHtml')) {
            escapeHtml = attrs.boolean('escapeHtml')
            attrs.remove 'escapeHtml'
        }

        // Add textarea field to requestDataValueProcessor
        def content = (escapeHtml ? value.encodeAsHTML() : value)
        if (attrs.name) {
            content = processFormFieldValueIfNecessary(attrs.name, content, "textarea")
        }
        out << "<textarea "
        outputAttributes(attrs, out, true)
        out << ">" << content << "</textarea>"
    }

    /**
     * Same as &lt;g:form&gt;, except sets the relevant enctype for a file upload form.
     *
     * @attr action the name of the action to use in the link, if not specified the default action will be linked
     * @attr controller the name of the controller to use in the link, if not specified the current controller will be linked
     * @attr id The id to use in the link
     * @attr url A map containing the action,controller,id etc.
     * @attr name A value to use for both the name and id attribute of the form tag
     * @attr useToken Set whether to send a token in the request to handle duplicate form submissions. See Handling Duplicate Form Submissions
     * @attr method the form method to use, either 'POST' or 'GET'; defaults to 'POST'
     */
    Closure uploadForm = { attrs, body ->
        attrs.enctype = "multipart/form-data"
        out << form(attrs, body)
    }

    /**
     * General linking to controllers, actions etc. Examples:<br/>
     *
     * &lt;g:form action="myaction"&gt;...&lt;/g:form&gt;<br/>
     * &lt;g:form controller="myctrl" action="myaction"&gt;...&lt;/g:form&gt;<br/>
     *
     * @attr action the name of the action to use in the link, if not specified the default action will be linked
     * @attr controller the name of the controller to use in the link, if not specified the current controller will be linked
     * @attr id The id to use in the link
     * @attr url A map containing the action,controller,id etc.
     * @attr name A value to use for both the name and id attribute of the form tag
     * @attr useToken Set whether to send a token in the request to handle duplicate form submissions. See Handling Duplicate Form Submissions
     * @attr method the form method to use, either 'POST' or 'GET'; defaults to 'POST'
     */
    Closure form = { attrs, body ->

        boolean useToken = false
        if (attrs.containsKey('useToken')) {
            useToken = attrs.boolean('useToken')
            attrs.remove('useToken')
        }

        def writer = getOut()

        def linkAttrs = attrs.subMap(LinkGenerator.LINK_ATTRIBUTES)

        writer << "<form action=\""

        // Call RequestDataValueProcessor to modify url if necessary
        def link = createLink(linkAttrs)
        if (requestDataValueProcessor != null) {
            link = requestDataValueProcessor.processAction(request, link, request.method)
        }

        writer << link
        writer << "\" "

        // if URL is not null remove attributes
        if (attrs.url == null) {
            attrs = attrs - linkAttrs
        } else {
            attrs.remove('url')
        }

        // default to post
        def method = linkAttrs[LinkGenerator.ATTRIBUTE_METHOD]?.toUpperCase() ?: 'POST'
        def httpMethod = HttpMethod.valueOf(method)
        boolean notGet = httpMethod != HttpMethod.GET

        if (notGet) {
            writer << 'method="post" '
        } else {
            writer << 'method="get" '
        }

        attrs.remove('method')
        // process remaining attributes
        if (attrs.id == null) attrs.remove('id')

        outputAttributes(attrs, writer, true)

        writer << ">"
        if (request['flowExecutionKey']) {
            writer.println()
            hiddenFieldImpl(writer, [name: "execution", value: request['flowExecutionKey']])
        }

        if (notGet && httpMethod != HttpMethod.POST) {
            hiddenFieldImpl(writer, [name: "_method", value: httpMethod.toString()])
        }

        if (useToken) {
            def tokensHolder = SynchronizerTokensHolder.store(session)
            writer.println()
            hiddenFieldImpl(writer, [name: SynchronizerTokensHolder.TOKEN_KEY, value: tokensHolder.generateToken(request.forwardURI)])
            writer.println()
            hiddenFieldImpl(writer, [name: SynchronizerTokensHolder.TOKEN_URI, value: request.forwardURI])
        }

        // output the body
        writer << body()

        //Write RequestDataValueProcessor hidden fields if necessary
        if (requestDataValueProcessor != null) {
            writeHiddenFields requestDataValueProcessor.getExtraHiddenFields(request)
        }
        // close tag
        writer << "</form>"
    }

    /**
     * Creates a submit button that submits to an action in the controller specified by the form action.<br/>
     * The name of the action attribute is translated into the action name, for example "Edit" becomes
     * "_action_edit" or "List People" becomes "_action_listPeople".<br/>
     * If the action attribute is not specified, the value attribute will be used as part of the action name.
     *
     * &lt;g:actionSubmit value="Edit" /&gt;<br/>
     * &lt;g:actionSubmit action="Edit" value="Some label for editing" /&gt;<br/>
     *
     * @emptyTag
     *
     * @attr value REQUIRED The title of the button and name of action when not explicitly defined.
     * @attr action The name of the action to be executed, otherwise it is derived from the value.
     * @attr disabled Makes the button to be disabled. Will be interpreted as a Groovy Truth
     */
    Closure actionSubmit = { attrs ->
        if (!attrs.value) {
            throwTagError("Tag [actionSubmit] is missing required attribute [value]")
        }

        attrs.tagName = "actionSubmit"

        // Strip out any 'name' attribute, since this tag overrides it.
        if (attrs.name) {
            log.warn "[actionSubmit] 'name' attribute will be ignored"
            attrs.remove('name')
        }

        attrs['class'] = attrs['class'] ?: 'btn btn-primary'

        // add action and value
        def value = attrs.remove('value')
        def action = attrs.remove('action') ?: value
        // Change value if necessary in requestDataValueProcessor
        value = processFormFieldValueIfNecessary("_action_${action}", value, "submit")
        booleanToAttribute(attrs, 'disabled')

        out << "<button type=\"submit\" name=\"_action_${action}\" value=\"${value}\" "

        // process remaining attributes
        outputAttributes(attrs, out)

        // close tag
        out << '/>'
    }

//    /**
//     * Creates a an image submit button that submits to an action in the controller specified by the form action.
//     * The name of the action attribute is translated into the action name, for example "Edit" becomes
//     * "_action_edit" or "List People" becomes "_action_listPeople".<br/>
//     * If the action attribute is not specified, the value attribute will be used as part of the action name.<br/>
//     *
//     * &lt;g:actionSubmitImage src="/images/submitButton.gif" action="Edit" /&gt;
//     *
//     * @emptyTag
//     *
//     * @attr value REQUIRED The title of the button and name of action when not explicitly defined.
//     * @attr action The name of the action to be executed, otherwise it is derived from the value.
//     * @attr src The source of the image to use
//     * @attr disabled Makes the button to be disabled. Will be interpreted as a Groovy Truth
//     */
//    Closure actionSubmitImage = { attrs ->
//        // TODO: Adept code for need of AdminLTE layout
//        attrs.tagName = "actionSubmitImage"
//
//        if (!attrs.value) {
//            throwTagError("Tag [$attrs.tagName] is missing required attribute [value]")
//        }
//
//        // add action and value
//        def value = attrs.remove('value')
//        def action = attrs.remove('action') ?: value
//        //Change this button to use requestDataValueProcessor
//        value = processFormFieldValueIfNecessary("_action_${action}", "${value}", "image")
//        booleanToAttribute(attrs, 'disabled')
//
//        out << "<input type=\"image\" name=\"_action_${action}\" value=\"${value}\" "
//
//        // add image src
//        def src = attrs.remove('src')
//        if (src) {
//            src = processedUrl(src, request)
//            out << "src=\"${src}\" "
//        }
//
//        // process remaining attributes
//        outputAttributes(attrs, out)
//
//        // close tag
//        out << '/>'
//    }

    /**
     * A simple date picker that renders a date as selects.<br/>
     * e.g. &lt;g:datePicker name="myDate" value="${new Date()}" /&gt;
     *
     * @emptyTag
     *
     * @attr name REQUIRED The name of the date picker field set
     * @attr value The current value of the date picker; defaults to either the value specified by the default attribute or now if no default is set
     * @attr default A Date or parsable date string that will be used if there is no value
     * @attr precision The desired granularity of the date to be rendered
     * @attr noSelection A single-entry map detailing the key and value to use for the "no selection made" choice in the select box. If there is no current selection this will be shown as it is first in the list, and if submitted with this selected, the key that you provide will be submitted. Typically this will be blank.
     * @attr years A list or range of years to display, in the order specified. i.e. specify 2007..1900 for a reverse order list going back to 1900. If this attribute is not specified, a range of years from the current year - 100 to current year + 100 will be shown.
     * @attr relativeYears A range of int representing values relative to value. For example, a relativeYears of -2..7 and a value of today will render a list of 10 years starting with 2 years ago through 7 years in the future. This can be useful for things like credit card expiration dates or birthdates which should be bound relative to today.
     * @attr id the DOM element id
     * @attr disabled Makes the resulting inputs and selects to be disabled. Is treated as a Groovy Truth.
     * @attr readonly Makes the resulting inputs and selects to be made read only. Is treated as a Groovy Truth.
     */
    Closure datePicker = { attrs ->
        def out = out // let x = x ?
        def xdefault = attrs['default']
        if (xdefault == null) {
            xdefault = new Date()
        }
        else if (xdefault.toString() != 'none') {
            if (xdefault instanceof String) {
                xdefault = DateFormat.getInstance().parse(xdefault)
            }
            else if (!(xdefault instanceof Date)) {
                throwTagError("Tag [datePicker] requires the default date to be a parseable String or a Date")
            }
        }
        else {
            xdefault = null
        }
        def years = attrs.years
        def relativeYears = attrs.relativeYears
        if (years != null && relativeYears != null) {
            throwTagError 'Tag [datePicker] does not allow both the years and relativeYears attributes to be used together.'
        }

        if (relativeYears != null) {
            if (!(relativeYears instanceof IntRange)) {
                // allow for a syntax like relativeYears="[-2..5]".  The value there is a List containing an IntRage.
                if ((!(relativeYears instanceof List)) || (relativeYears.size() != 1) || (!(relativeYears[0] instanceof IntRange))) {
                    throwTagError 'The [datePicker] relativeYears attribute must be a range of int.'
                }
                relativeYears = relativeYears[0]
            }
        }
        def value = attrs.value
        if (value.toString() == 'none') {
            value = null
        }
        else if (!value) {
            value = xdefault
        }
        def name = attrs.name
        def id = attrs.id ?: name

        def noSelection = attrs.noSelection
        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }

        final PRECISION_RANKINGS = ["year": 0, "month": 10, "day": 20, "hour": 30, "minute": 40]
        def precision = (attrs.precision ? PRECISION_RANKINGS[attrs.precision] :
                (grailsApplication.config.grails.tags.datePicker.default.precision ?
                        PRECISION_RANKINGS["${grailsApplication.config.grails.tags.datePicker.default.precision}"] :
                        PRECISION_RANKINGS["minute"]))

        def day
        def month
        def year
        def hour
        def minute
        def dfs = new DateFormatSymbols(RequestContextUtils.getLocale(request))

        def c = null
        if (value instanceof Calendar) {
            c = value
        }
        else if (value != null) {
            c = new GregorianCalendar()
            c.setTime(value)
        }

        if (c != null) {
            day = c.get(GregorianCalendar.DAY_OF_MONTH)
            month = c.get(GregorianCalendar.MONTH)
            year = c.get(GregorianCalendar.YEAR)
            hour = c.get(GregorianCalendar.HOUR_OF_DAY)
            minute = c.get(GregorianCalendar.MINUTE)
        }

        if (years == null) {
            def tempyear
            if (year == null) {
                // If no year, we need to get current year to setup a default range... ugly
                def tempc = new GregorianCalendar()
                tempc.setTime(new Date())
                tempyear = tempc.get(GregorianCalendar.YEAR)
            }
            else {
                tempyear = year
            }
            if (relativeYears) {
                if (relativeYears.reverse) {
                    years = (tempyear + relativeYears.toInt)..(tempyear + relativeYears.fromInt)
                } else {
                    years = (tempyear + relativeYears.fromInt)..(tempyear + relativeYears.toInt)
                }
            } else {
                years = (tempyear + 100)..(tempyear - 100)
            }
        }

        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'readonly')
        out.println('<div class="row">')
        // Change this hidden to use requestDataValueProcessor
        def dateStructValue = processFormFieldValueIfNecessary("${name}","date.struct","hidden")
        out.println "<input type=\"hidden\" name=\"${name}\" value=\"${dateStructValue}\" />"

        // create day select
        if (precision >= PRECISION_RANKINGS["day"]) {
            out.println '<div class="col-md-3 col-lg-2">'
            out.println "<select name=\"${name}_day\" id=\"${id}_day\" class=\"form-control\""
            if (attrs.disabled) {
                out << ' disabled="disabled"'
            }
            if (attrs.readonly) {
                out << ' readonly="readonly"'
            }
            out << '>'

            if (noSelection) {
                renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 1..31) {
                // Change this option to use requestDataValueProcessor
                def dayIndex = processFormFieldValueIfNecessary("${name}_day","${i}","option")
                out.println "<option value=\"${dayIndex}\"${i == day ? ' selected="selected"' : ''}>${i}</option>"
            }
            out.println '</select>'
            out.println '</div>'
        }

        // create month select
        if (precision >= PRECISION_RANKINGS["month"]) {
            out.println '<div class="col-md-5 col-lg-3">'
            out.println "<select name=\"${name}_month\" id=\"${id}_month\" class=\"form-control\""
            if (attrs.disabled) {
                out << ' disabled="disabled"'
            }
            if (attrs.readonly) {
                out << ' readonly="readonly"'
            }
            out << '>'

            if (noSelection) {
                renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            dfs.months.eachWithIndex {m, i ->
                if (m) {
                    def monthIndex = i + 1
                    monthIndex = processFormFieldValueIfNecessary("${name}_month","${monthIndex}","option")
                    out.println "<option value=\"${monthIndex}\"${i == month ? ' selected="selected"' : ''}>$m</option>"
                }
            }
            out.println '</select>'
            out.println '</div>'
        }

        // create year select
        if (precision >= PRECISION_RANKINGS["year"]) {
            out.println '<div class="col-md-4 col-lg-2">'
            out.println "<select name=\"${name}_year\" id=\"${id}_year\" class=\"form-control\""
            if (attrs.disabled) {
                out << ' disabled="disabled"'
            }
            if (attrs.readonly) {
                out << ' readonly="readonly"'
            }
            out << '>'

            if (noSelection) {
                renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in years) {
                // Change this year option to use requestDataValueProcessor
                def yearIndex  = processFormFieldValueIfNecessary("${name}_year","${i}","option")
                out.println "<option value=\"${yearIndex}\"${i == year ? ' selected="selected"' : ''}>${i}</option>"
            }
            out.println '</select>'
            out.println '</div>'
        }

        // do hour select
        if (precision >= PRECISION_RANKINGS["hour"]) {
            out.println '<div class="col-md-3 col-lg-2">'
            out.println "<select name=\"${name}_hour\" id=\"${id}_hour\" class=\"form-control\""
            if (attrs.disabled) {
                out << ' disabled="disabled"'
            }
            if (attrs.readonly) {
                out << ' readonly="readonly"'
            }
            out << '>'

            if (noSelection) {
                renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 0..23) {
                def h = '' + i
                if (i < 10) h = '0' + h
                // This option add hour to requestDataValueProcessor
                h  = processFormFieldValueIfNecessary("${name}_hour","${h}","option")
                out.println "<option value=\"${h}\"${i == hour ? ' selected="selected"' : ''}>$h</option>"
            }
            out.println '</select></div>'

            // If we're rendering the hour, but not the minutes, then display the minutes as 00 in read-only format
            if (precision < PRECISION_RANKINGS["minute"]) {
                out.println '<div class="col-md-3 col-lg-2">00</div>'
            }
        }

        // do minute select
        if (precision >= PRECISION_RANKINGS["minute"]) {
            out.println '<div class="col-md-3 col-lg-2">'
            out.println "<select name=\"${name}_minute\" id=\"${id}_minute\" class=\"form-control\""
            if (attrs.disabled) {
                out << 'disabled="disabled"'
            }
            if (attrs.readonly) {
                out << 'readonly="readonly"'
            }
            out << '>'

            if (noSelection) {
                renderNoSelectionOptionImpl(out, noSelection.key, noSelection.value, '')
                out.println()
            }

            for (i in 0..59) {
                def m = '' + i
                if (i < 10) m = '0' + m
                m  = processFormFieldValueIfNecessary("${name}_minute","${m}","option")
                out.println "<option value=\"${m}\"${i == minute ? ' selected="selected"' : ''}>$m</option>"
            }
            out.println '</select>'
            out.println '</div>'
        }
        out.println '</div>'
    }

    /**
     * A helper tag for creating HTML selects.<br/>
     *
     * Examples:<br/>
     * &lt;g:select name="user.age" from="${18..65}" value="${age}" /&gt;<br/>
     * &lt;g:select name="user.company.id" from="${Company.list()}" value="${user?.company.id}" optionKey="id" /&gt;<br/>
     *
     * @emptyTag
     *
     * @attr name REQUIRED the select name
     * @attr id the DOM element id - uses the name attribute if not specified
     * @attr from REQUIRED The list or range to select from
     * @attr keys A list of values to be used for the value attribute of each "option" element.
     * @attr optionKey By default value attribute of each &lt;option&gt; element will be the result of a "toString()" call on each element. Setting this allows the value to be a bean property of each element in the list.
     * @attr optionValue By default the body of each &lt;option&gt; element will be the result of a "toString()" call on each element in the "from" attribute list. Setting this allows the value to be a bean property of each element in the list.
     * @attr value The current selected value that evaluates equals() to true for one of the elements in the from list.
     * @attr multiple boolean value indicating whether the select a multi-select (automatically true if the value is a collection, defaults to false - single-select)
     * @attr valueMessagePrefix By default the value "option" element will be the result of a "toString()" call on each element in the "from" attribute list. Setting this allows the value to be resolved from the I18n messages. The valueMessagePrefix will be suffixed with a dot ('.') and then the value attribute of the option to resolve the message. If the message could not be resolved, the value is presented.
     * @attr noSelection A single-entry map detailing the key and value to use for the "no selection made" choice in the select box. If there is no current selection this will be shown as it is first in the list, and if submitted with this selected, the key that you provide will be submitted. Typically this will be blank - but you can also use 'null' in the case that you're passing the ID of an object
     * @attr disabled boolean value indicating whether the select is disabled or enabled (defaults to false - enabled)
     * @attr readonly boolean value indicating whether the select is read only or editable (defaults to false - editable)
     */
    Closure select = { attrs ->
        if (!attrs.name) {
            throwTagError("Tag [select] is missing required attribute [name]")
        }
        if (!attrs.containsKey('from')) {
            throwTagError("Tag [select] is missing required attribute [from]")
        }
        attrs['class'] = attrs['class'] ?: 'form-control'
        def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
        def locale = RequestContextUtils.getLocale(request)
        def writer = out
        def from = attrs.remove('from')
        def keys = attrs.remove('keys')
        def optionKey = attrs.remove('optionKey')
        def optionDisabled = attrs.remove('optionDisabled')
        def optionValue = attrs.remove('optionValue')
        def value = attrs.remove('value')
        if (value instanceof Collection && attrs.multiple == null) {
            attrs.multiple = 'multiple'
        }
        if (value instanceof CharSequence) {
            value = value.toString()
        }
        def valueMessagePrefix = attrs.remove('valueMessagePrefix')
        def noSelection = attrs.remove('noSelection')
        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }
        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'readonly')

        writer << "<select "
        // process remaining attributes
        outputAttributes(attrs, writer, true)

        writer << '>'
        writer.println()

        if (noSelection) {
            renderNoSelectionOptionImpl(writer, noSelection.key, noSelection.value, value)
            writer.println()
        }

        // create options from list
        from.eachWithIndex {el, i ->
            def keyDisabled
            def keyValue
            writer << '<option '
            if (keys) {
                keyValue = keys[i]
                writeValueAndCheckIfSelected(attrs.name, keyValue, value, writer)
            }
            else if (optionKey) {
                def keyValueObject
                if (optionKey instanceof Closure) {
                    keyValue = optionKey(el)
                }
                else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, el.getClass().name)) {
                    keyValue = el.ident()
                    keyValueObject = el
                }
                else {
                    keyValue = el[optionKey]
                    keyValueObject = el
                }
                if(optionDisabled) {
                    if (optionDisabled instanceof Closure) {
                        keyDisabled = optionDisabled(el)
                    }
                    else {
                        keyDisabled = el[optionDisabled]
                    }
                }
                writeValueAndCheckIfSelected(attrs.name, keyValue, value, writer, keyValueObject,keyDisabled)
            }
            else {
                keyValue = el
                writeValueAndCheckIfSelected(attrs.name, keyValue, value, writer)
            }
            writer << '>'
            if (optionValue) {
                if (optionValue instanceof Closure) {
                    writer << optionValue(el).toString().encodeAsHTML()
                }
                else {
                    writer << el[optionValue].toString().encodeAsHTML()
                }
            }
            else if (el instanceof MessageSourceResolvable) {
                writer << messageSource.getMessage(el, locale)
            }
            else if (valueMessagePrefix) {
                def message = messageSource.getMessage("${valueMessagePrefix}.${keyValue}", null, null, locale)
                if (message != null) {
                    writer << message.encodeAsHTML()
                }
                else if (keyValue && keys) {
                    def s = el.toString()
                    if (s) writer << s.encodeAsHTML()
                }
                else if (keyValue) {
                    writer << keyValue.encodeAsHTML()
                }
                else {
                    def s = el.toString()
                    if (s) writer << s.encodeAsHTML()
                }
            }
            else {
                def s = el.toString()
                if (s) writer << s.encodeAsHTML()
            }
            writer << '</option>'
            writer.println()
        }
        // close tag
        writer << '</select>'
    }

    /**
     * A helper tag for creating radio buttons.
     *
     * @emptyTag
     *
     * @attr value REQUIRED The value of the radio button
     * @attr name REQUIRED The name of the radio button
     * @attr checked boolean to indicate that the radio button should be checked
     * @attr disabled boolean to indicate that the radio button should be disabled
     * @attr readonly boolean to indicate that the radio button should not be editable
     * @attr id the DOM element id
     */
    Closure radio = { attrs ->
        def value = attrs.remove('value')
        def name = attrs.remove('name')
        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'readonly')

        def checked = attrs.remove('checked') ? true : false
        value = processFormFieldValueIfNecessary(name, "${value?.toString()?.encodeAsHTML()}", "radio")
        out << "<input type=\"radio\" name=\"${name}\"${checked ? ' checked="checked" ' : ' '}value=\"${value?.toString()?.encodeAsHTML()}\" "
        if (!attrs.containsKey('id')) {
            out << """id="${name}" """
        }
        // process remaining attributes
        outputAttributes(attrs, out)

        // close the tag, with no body
        out << ' />'
    }

    /**
     * A helper tag for creating radio button groups.
     *
     * @attr name REQUIRED The name of the group
     * @attr values REQUIRED The list values for the radio buttons
     * @attr value The current selected value
     * @attr labels Labels for each value contained in the values list. If this is ommitted the label property on the iterator variable (see below) will default to 'Radio ' + value.
     * @attr disabled Disables the resulting radio buttons.
     * @attr readonly Makes the resulting radio buttons to not be editable
     */
    Closure radioGroup = { attrs, body ->
        def value = attrs.remove('value')
        def values = attrs.remove('values')
        def labels = attrs.remove('labels')
        def name = attrs.remove('name')
        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'readonly')

        values.eachWithIndex { val, idx ->
            def it = new Expando()
            def radioWriter = new FastStringWriter()
            radioWriter << "<input type=\"radio\" name=\"${name}\" "
            if (value?.toString().equals(val.toString())) {
                radioWriter << 'checked="checked" '
            }
            // Generate
            def processedVal = processFormFieldValueIfNecessary(name, val.toString().encodeAsHTML(), "radio")
            radioWriter << "value=\"${processedVal}\" "

            // process remaining attributes
            outputAttributes(attrs, radioWriter)
            radioWriter << "/>"

            it.radio = raw(radioWriter.buffer)

            it.label = labels == null ? 'Radio ' + val : labels[idx]

            out << body(it)
            out.println()
        }
    }

    def renderNoSelectionOptionImpl(out, noSelectionKey, noSelectionValue, value) {
        // If a label for the '--Please choose--' first item is supplied, write it out
        out << "<option value=\"${(noSelectionKey == null ? '' : noSelectionKey)}\"${noSelectionKey == value ? ' selected="selected"' : ''}>${noSelectionValue.encodeAsHTML()}</option>"
    }

    private void writeValueAndCheckIfSelected(selectName, keyValue, value, writer) {
        writeValueAndCheckIfSelected(selectName, keyValue, value, writer, null)
    }
    private void writeValueAndCheckIfSelected(selectName, keyValue, value, writer, el) {
        writeValueAndCheckIfSelected(selectName, keyValue, value, writer, el, null)
    }

    private void writeValueAndCheckIfSelected(selectName, keyValue, value, writer, el, keyDisabled) {

        boolean selected = false
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
            selected = (keyValue == value)
        }
        else if (value instanceof Collection) {
            // first try keyValue
            selected = value.contains(keyValue)
            if (!selected && el != null) {
                selected = value.contains(el)
            }
        }
        // GRAILS-3596: Make use of Groovy truth to handle GString <-> String
        // and other equivalent types (such as numbers, Integer <-> Long etc.).
        else if (keyValue == value) {
            selected = true
        }
        else if (keyClass && value != null) {
            try {
                value = conversionService.convert(value, keyClass)
                selected = keyValue == value
            }
            catch (e) {
                // ignore
            }
        }
        keyValue = processFormFieldValueIfNecessary(selectName, "${keyValue}","option")
        writer << "value=\"${keyValue}\" "
        if (selected) {
            writer << 'selected="selected" '
        }
        if(keyDisabled && !selected) {
            writer << 'disabled="disabled" '
        }
    }

    def hiddenFieldImpl(out, attrs) {
        attrs.type = "hidden"
        attrs.tagName = "hiddenField"
        fieldImpl(out, attrs)
    }

    @CompileStatic
    def fieldImpl(GrailsPrintWriter out, Map attrs) {
        attrs['class'] = attrs['class'] ?: 'form-control'
        resolveAttributes(attrs)

        attrs.value = processFormFieldValueIfNecessary(attrs.name, attrs.value, attrs.type)

        out << "<input type=\"${attrs.remove('type')}\" "
        outputAttributes(attrs, out, true)
        out << "/>"
    }

    private void writeHiddenFields(hiddenFields) {
        def writer = getOut()
        hiddenFields.each { key, value -> writer << "<input type=\"hidden\" name=\"${key}\" value=\"${value}\" />\n" }
    }

    /**
     * Check required attributes, set the id to name if no id supplied, extract bean values etc.
     */
    private void resolveAttributes(Map attrs) {
        if (!attrs.name && !attrs.field) {
            throwTagError("Tag [${attrs.tagName}] is missing required attribute [name] or [field]")
        }

        attrs.remove('tagName')

        def val = attrs.remove('bean')
        if (val) {
            if (attrs.name.indexOf('.')) {
                attrs.name.split('\\.').each { val = val?."$it" }
            } else {
                val = val[name]
            }
            attrs.value = val
        }
        attrs.value = attrs.value != null ? attrs.value : '' // can't use ?: since 0 is groovy false

        // Some attributes can be treated as boolean, but must be converted to the
        // expected value.
        booleanToAttribute(attrs, 'disabled')
        booleanToAttribute(attrs, 'checked')
        booleanToAttribute(attrs, 'readonly')
    }

    /**
     * Dump out attributes in HTML compliant fashion.
     */
    @CompileStatic
    void outputAttributes(Map attrs, GrailsPrintWriter writer, boolean useNameAsIdIfIdDoesNotExist = false) {
        attrs.remove('tagName') // Just in case one is left
        Encoder htmlEncoder = codecLookup?.lookupEncoder('HTML')
        attrs.each { k, v ->
            if (v != null) {
                writer << k
                writer << '="'
                writer << (htmlEncoder != null ? htmlEncoder.encode(v) : v)
                writer << '" '
            }
        }
        if (useNameAsIdIfIdDoesNotExist) {
            outputNameAsIdIfIdDoesNotExist(attrs, writer)
        }
    }

    @CompileStatic
    private void outputNameAsIdIfIdDoesNotExist(Map attrs, GrailsPrintWriter out) {
        if (!attrs.containsKey('id') && attrs.containsKey('name')) {
            Encoder htmlEncoder = codecLookup?.lookupEncoder('HTML')
            out << 'id="'
            out << (htmlEncoder != null ? htmlEncoder.encode(attrs.name) : attrs.name)
            out << '" '
        }
    }

    /**
     * Some attributes can be defined as Boolean values, but the html specification
     * mandates the attribute must have the same value as its name. For example,
     * disabled, readonly and checked.
     */
    @CompileStatic
    private void booleanToAttribute(Map attrs, String attrName) {
        def attrValue = attrs.remove(attrName)
        if (attrValue instanceof CharSequence) {
            attrValue = attrValue.toString().trim()
        }
        // If the value is the same as the name or if it is a boolean value,
        // reintroduce the attribute to the map according to the w3c rules, so it is output later
        if ((attrValue instanceof Boolean && attrValue) ||
                (attrValue instanceof String && (((String) attrValue).equalsIgnoreCase('true') || ((String) attrValue).equalsIgnoreCase(attrName)))) {
            attrs.put(attrName, attrName)
        } else if (attrValue instanceof String && !((String) attrValue).equalsIgnoreCase('false')) {
            // If the value is not the string 'false', then we should just pass it on to
            // keep compatibility with existing code
            attrs.put(attrName, attrValue)
        }
    }

    private processFormFieldValueIfNecessary(name, value, type) {
        if (requestDataValueProcessor != null) {
            value = requestDataValueProcessor.processFormFieldValue(request, name, "${value}", type)
        }
        return value
    }

    /**
     * Filters the url through the RequestDataValueProcessor bean if it is registered.
     */
    String processedUrl(String link, request) {
        if (requestDataValueProcessor == null) {
            return link
        }

        return requestDataValueProcessor.processUrl(request, link)
    }

    void afterPropertiesSet() {
        if (applicationContext.containsBean('requestDataValueProcessor')) {
            requestDataValueProcessor = applicationContext.getBean('requestDataValueProcessor', RequestDataValueProcessor)
        }
    }
}
