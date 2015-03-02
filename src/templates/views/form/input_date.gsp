<%
    def constraints = node['constraints']
    format = constraints?.format ? " format=\"${constraints.format}\"" : ''
    precision = ' precision="minute"'
    attrs = constraints['attributes']?.collect { key, value ->
        " ${key}=\"${value}\""
    }?.join() ?: ''

    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <adminlte:datePicker name="${targetVarName}.${node.name}" value="\${${propertyVar}?.${node.name}}"${format}${precision}${attrs}${noSelection} />
</div>