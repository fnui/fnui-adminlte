<%
typeName = node['type'] ? "${node['type'].classDeclaration} " : ''
varName = node['varName'] ? "${node['varName']} = " : ''
serviceVarName = node['serviceVarName']
methodName = node['methodName']
parameters = node.findAllByType('parameter').collect { "${it.data['varName']}" }.join(', ')
%>\
${typeName}${varName}${serviceVarName}.${methodName}(${parameters})
