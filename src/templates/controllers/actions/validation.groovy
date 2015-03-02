<%
kind = node['kind']

def validationExpression = null
if (kind == 'notNull') {
    validationExpression = node['validatables'].collect { "!${it}" }.join(' || ')
} else if (kind == 'hasErrors') {
    validationExpression = node['validatables'].collect{ "${it}.hasErrors()" }.join(' || ')
} else if (kind == 'notNullHasErrors') {
    validationExpression = node['validatables'].collect{ "!${it} || ${it}.hasErrors()" }.join(' || ')
}

if (validationExpression) {
%>\
if (${validationExpression}) {
    <% node.children.each { c -> %>
    <%# translate("actions/${c.type}", c, true) %>
    <% } %>
}

<% } else { %>\
// validation of kind '${kind}' was unknown
<% } %>