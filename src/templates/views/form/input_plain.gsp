<%
    def constraints = node['constraints']
    noSelection = !constraints['required'] ? ' noSelection="[\'\': \'\']"' : ''

    nameValue = "name=\"${targetVarName}.${node.name}\" value=\"\${${propertyVar}?.${node.name}}\""
    maxSize = constraints['maxSize'] ? " maxlength=\"${constraints['maxSize']}\"" : ''
    matches = constraints['matches'] ? " pattern=\"\${${formType.classDeclaration}.constraints.${node.name}.matches}\"" : ''
    baseString = "${nameValue}${maxSize}${matches}"

    inputWidget = 'field'
    if (constraints['password']) {
        inputWidget = 'specialField'
        special = 'password'
    } else if (constraints['url']) {
        inputWidget = 'specialField'
        special = 'url'
    } else if (constraints['email']) {
        inputWidget = 'specialField'
        special = 'email'
    } else if (constraints['widget'] == 'textarea' || constraints['maxSize'] > 250) {
        inputWidget = 'textarea'
    } else if (constraints['inList']) {
        inputWidget = 'select'
    }
%>\
<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>

    <%
        if (inputWidget == 'specialField') {
    %>
    <adminlte:field type="${special}" ${baseString} />
    <%
        } else if (inputWidget == 'textarea') {
    %>
    <adminlte:textArea ${baseString} cols="60" rows="5" />
    <%
        } else if (inputWidget == 'select') {
    %>
    <adminlte:select ${baseString} from="${constraints['inList']}" valueMessagePrefix="${targetVarName}.${node.name}"${noSelection} />
    <%
        } else {
    %>
    <adminlte:textField ${baseString} />
    <%
        }
    %>
</div>