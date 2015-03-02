<%
    def link = node['target']
    if (link.kind == 'action') {
        def controller = link.controller ? "controller: '${link.controller}', " : ''
        def params = node['parameterVar'] ? ", params: ${link.generateParams(node['parameterVar'])}" : ''
%>\
redirect ${controller}action: '${link.action}'${params}
<% } else if (link.kind == 'uri') { %>\
redirect uri: '${link.uri}'
<% } %>