<g:each in="\${${varName}.${node.name}}" var="${node.name[0]}">
    <%
        node.name = node.name[0]
    %>
    <%# translate("display/property_plain", node, true, 'PropertyDecoration') %>
</g:each>