<%
    def constraints = node['constraints']
    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''

    nameValue = "name=\"${targetVarName}.${node.name}.id\" value=\"\${${propertyVar}?.${node.name}}\""
    from = " from=\"\${${type.describedType.classDeclaration}.list()}\""
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <adminlte:select ${nameValue}${from} multiple="multiple" optionKey="id" size="5"${noSelection} />
</div>