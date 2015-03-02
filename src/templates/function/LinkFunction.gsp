<%
if (link.kind == 'action') {
    namespace = link.namespace ? " namespace=\"${link.namespace}\"" : ''
    controller = link.controller ? " controller=\"${link.controller}\"" : ''
    action = link.action ? " action=\"${link.action}\"" : ''
    def paramsValue = link.generateParams(contextVar)
    params = paramsValue ? " params=\"$paramsValue\"" : ''
    def reqObject = link.requirements
    requirements = reqObject ? " requirements=\"${reqObject.closureClass.name.replace('$', '\\$')}\"" :''
    if (requirements) {
        def requirementsParams = reqObject ? reqObject.generateParams(contextVar) : ''
        requirements += requirementsParams ? " requirementsParams=\"\${${requirementsParams}}\"" : ''
    }
    attrs = attributes.collect { key, value ->
        " $key=\"$value\""
    }.join('')
%>\
<adminlte:link${namespace}${controller}${action}${params}${requirements}${attrs}>
${body}
</adminlte:link>
<%
} else if (link.kind == 'uri') {
    uri = link.uri ? " uri=\"${link.uri}\"" : ''
%>\
<adminlte:link${uri}>
${body}
</adminlte:link>
<%
} else {
%>
${body}
<%
}
%>