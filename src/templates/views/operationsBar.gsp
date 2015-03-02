<%
    def links = node.findAllByType('link').collect { it['details'] }
    if (links) {
%>\
<div class="box operations-bar">
    <div class="box-body">
        <ul class="nav nav-pills">
            <% links.each { link -> %>
            <li>
                <%# generateLink(link, '', [:], |%>
                    <i class="fa fa-list"></i>
                    <g:message code="${simpleControllerName}.link.${link.name}" default="${naturalName(link.name)}" />
                <%| ) %>
            </li>
            <% } %>
        </ul>
    </div>
</div>
<% } %>