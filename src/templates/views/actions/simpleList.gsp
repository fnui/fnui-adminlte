<div class="row">
    <div class="col-xs-12">
        <div class="box">
            <div class="box-body table-responsive no-padding">
                <table class="table table-hover">
                    <% def propertyNodes = node.findByType('properties')?.findAllByType('property') %>
                    <tr>
                        <% propertyNodes.each { property -> %>
                        <%# extend('BeforePropertyDisplay', property) %>
                        <th><g:message code="${simpleControllerName}.${property.name}.label" default="${naturalName(property.name)}" /></th>
                        <%# extend('AfterPropertyDisplay', property) %>
                        <% } %>
                    </tr>
                    <%
                        listVarName = node['varName']
                        varName = listVarName.length() > 1 ? listVarName.substring(0,1) : 'v'
                    %>
                    <g:each in="\${${listVarName}}" status="i" var="${varName}">
                        <tr>
                            <% propertyNodes.each { property -> %>
                            <%# extend('BeforePropertyDisplay', property) %>
                            <td>
                                <%# translate('display/property', property, false, 'PropertyDecoration') %>
                            </td>
                            <%# extend('AfterPropertyDisplay', property) %>
                            <% } %>
                        </tr>
                    </g:each>
                </table>
            </div><!-- /.box-body -->
        </div>
    </div>
</div>