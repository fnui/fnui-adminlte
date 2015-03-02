<%
    def constraints = node['constraints']
    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''

    nameValue = "name=\"${targetVarName}.${node.name}.id\" value=\"\${${propertyVar}?.${node.name}}\""
    from = " from=\"\${${type.classDeclaration}.list()}\""
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <adminlte:select ${nameValue}${from} optionKey="id"${noSelection} />
</div>