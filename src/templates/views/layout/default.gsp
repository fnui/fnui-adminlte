<!DOCTYPE html>%{--
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
ATTENTION:
This gsp was generated by (FN)UI and should not be modified because any
modification to this file will be lost when (FN)UI is executed again.
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
--}%
<html>
<head>
    <% actionName = node.name %>
    <meta name="layout" content="AdminLTE/main" />
    <title><g:message code="${simpleControllerName}.${node.name}.title" default="${naturalName(node.name)}" /></title>
</head>
<body>
    <!-- Content Header (Page header) -->
    <section class="content-header">
        <h1>
            <g:message code="${simpleControllerName}.${node.name}.headline" default="${naturalName(node.name)}" />
            <small><g:message code="${simpleControllerName}.${node.name}.description" default="" /></small>
        </h1>
    </section>

    <!-- Main content -->
    <section class="content">
        <%# translate('operationsBar', node.findByType('operationsBar')) %>

        <g:if test="\${flash.message}">
            <div class="callout callout-info">
                <p>\${flash.message}</p>
            </div>
        </g:if>
        <g:if test="\${flash.error}">
            <div class="callout callout-danger">
                <p>\${flash.error}</p>
            </div>
        </g:if>
        <g:hasErrors bean="\${flash.errors}">
            <div class="callout callout-danger">
                <ul>
                    <g:eachError bean="\${flash.errors}" var="error">
                        <li><g:message error="\${error}"/></li>
                    </g:eachError>
                </ul>
            </div>
        </g:hasErrors>

        <%# translate('main', node.findByType('main'))%>
    </section><!-- /.content -->
</body>
</html>