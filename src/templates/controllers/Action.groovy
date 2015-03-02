<%
actionName = node.name
actionSignature = node.findAllByType('parameter').collect { parameterNode ->
        "${parameterNode['type'].classDeclaration} ${parameterNode['varName']}"
    }.join(', ')

actionBody = node.findByType('actionBody')
%>
<%# extend('ActionAnnotations', node) %>
def ${actionName}(${actionSignature}) {
    <% actionBody.children.each { c -> %>
    <%# translate("actions/${c.type}", c, true) %>
    <% } %>
}
<%
node.findAllByType('view')?.each { view ->
    addView(view)
}
%>