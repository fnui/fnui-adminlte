<%
def contextOperations = node.findByType('contextOperations')
if (contextOperations) {
    def contextVar = contextOperations['contextVar']
%>
<div class="btn-group box-footer">
    <% contextOperations.findAllByType('link').collect { it['details']}.each { link -> %>
    <%# generateLink(link, contextVar, ['class':'btn btn-default'], |%>
        <g:message code="${simpleControllerName}.link.${link.name}" default="${naturalName(link.name)}" />
    <%| ) %>
    <% } %>
</div>
<% } %>