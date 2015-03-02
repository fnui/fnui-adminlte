<%
    def constraints = node['constraints']
    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''

    nameValue = "name=\"${targetVarName}.${node.name}\" value=\"\${fieldValue(bean: ${propertyVar}, field: '${node.name}')}\""

    inputWidget = 'field'
    if (constraints['range'] != null) {
        inputWidget = 'range'
        min = constraints['range'].from
        max = constraints['range'].to
    } else if (constraints['inList'] != null) {
        inputWidget = 'selectList'
        selectValues = constraints['inList']
    } else if (node['type'].isAssignableTo(Byte)) {
        inputWidget = 'range'
        min = -128
        max = 127
    } else {
        min = constraints['min'] ? " min=\"${constraints['min']}\"" : ''
        max = constraints['max'] ? " max=\"${constraints['max']}\"" : ''
    }
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <%
        if (inputWidget == 'range') {
    %>
    <adminlte:select ${nameValue} from="${min}..${max}"${noSelection} />
    <%
        } else if (inputWidget == 'selectList') {
    %>
    <adminlte:select ${nameValue} from="${constraints['inList']}"${noSelection} />
    <%
        } else {
    %>
    <adminlte:field type="number" ${nameValue}${min}${max} />
    <%
        }
    %>
</div>