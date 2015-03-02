<%
def uiDefinition = node['uiDefinition']
def scope = uiDefinition?.get('scope')

if (scope) {
%>
static scope = '${scope}'
<% } %>