<%
level = node['level']
kind = node['kind']
i18nCode = node['i18nCode']
message = node['message']

if (kind == 'i18n') {
    defaultMessage = message ? ", default: '${message}'" : ''
%>\
flash.${level} = message(code: '${i18nCode}'${defaultMessage})
<% } else { %>\
flash.${level} = '${message}'
<% } %>