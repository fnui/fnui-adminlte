<%
    from = " from=\"\${${type.classDeclaration}.values()}\""
    keys = " keys=\"\${${type.classDeclaration}.values()*.name()}\""
    def constraints = node['constraints']
    attrs = constraints['attributes']?.collect { key, value ->
        " ${key}=\"${value}\""
    }?.join() ?: ''
    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <adminlte:select name="${targetVarName}.${node.name}"${from}${keys} value="\${${propertyVar}?.${node.name}?.name()}"${attrs}${noSelection} />
</div>