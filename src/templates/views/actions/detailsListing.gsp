<div class="row">
    <div class="col-xs-12">
        <div class="box">
            <div class="box-body">
                <dl class="dl-horizontal">
                    <%
                        varName = node['varName']
                        node.findByType('properties')?.findAllByType('property').each { property ->
                            widget = property['constraints'].get('show')
                    %>
                    <%# extend('BeforePropertyDisplay', property) %>
                    <dt id="${property.name}-label"><g:message code="${simpleControllerName}.${property.name}.label" default="${property.name.capitalize()}" /></dt>
                    <dd>
                        <%# translate("display/property_${widget}", property, true, 'PropertyDecoration') ?:
                                "No template 'display/property_${widget}.gsp' for ${property.name}" %>
                    </dd>
                    <%# extend('AfterPropertyDisplay', property) %>
                    <% } %>
                </dl>
                <%# translate('context/contextOperations', node) %>
            </div><!-- /.box-body -->
        </div>
    </div>
</div>