<%
def uiDefinition = node['uiDefinition']
def navigation = uiDefinition?.get('navigation')

if (navigation) {
    navigationMap = navigation.collect{ key, value -> "$key:'$value'"}.join(', ')
%>
static navigation = [${navigationMap}]
<% } %>