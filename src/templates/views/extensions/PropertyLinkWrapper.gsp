<%
def link = node['link']
if (!link) {
%>\
<%# translateWrapped() %>
<% } else {
    increaseIndentionForWrapped()
%>\
<%# generateLink(link, varName, ['alwaysPrintBody':'${true}'], "\n${translateWrapped()}") %>
<% } %>