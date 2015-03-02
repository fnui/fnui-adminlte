<%
    def constraints = node['constraints']
    attrs = constraints['attributes']?.collect { key, value ->
        " ${key}=\"${value}\""
    }?.join() ?: ''
%>\
<div class="checkbox">
    <label for="${targetVarName}.${node.name}">
        <adminlte:checkBox name="${targetVarName}.${node.name}" value="\${${propertyVar}?.${node.name}}"${attrs} /><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" />
    </label>
</div>