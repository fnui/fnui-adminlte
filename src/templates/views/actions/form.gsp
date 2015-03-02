<div class="row">
    <div class="col-xs-12 col-md-10 col-lg-7">
        <div class="box">
            <%
                node.findAllByType('formObject').each { formObject ->
            %>
            <g:hasErrors bean="\${${formObject['varName']}}">
            <div class="callout callout-danger">
                <ul>
                <g:eachError bean="\${${formObject['varName']}}" var="error">
                    <li><g:message error="\${error}"/></li>
                </g:eachError>
                </ul>
            </div>
            </g:hasErrors>
            <% } %>
            <%
                def defaultSubmitLink = node['defaultSubmit']
                def containsFile = node.findAllByType('formEntry').any { formEntry ->
                    formEntry['constraints']['form'] == 'file'
                }
            %>
            <adminlte:${containsFile?'uploadForm':'form'} namespace="generated" action="${defaultSubmitLink.action}" role="form">
                <div class="box-body">
                    <%
                        node.findAllByType('formObject').each { formObject ->
                            propertyVar = formObject['varName']
                            targetVarName = formObject['targetVarName']
                            formType = formObject['type']
                            formObject.findAllByType('formEntry').each { formEntry ->
                                feature = formEntry['feature']
                                type = formEntry['type']
                                widget = formEntry['constraints']['form']
                    %>
                    <%# extend('BeforeFormInput', formEntry) %>
                    <%# translate("form/input_${widget}", formEntry, true) ?:
                            "No template 'form/input_${widget}.gsp' for ${formEntry.name}" %>
                    <%# extend('AfterFormInput', formEntry) %>
                    <%      }
                        } %>
                </div>
                <div class="box-footer">
                    <adminlte:submitButton name="create" value="\${message(code: 'default.button.${actionName}.label', default: '${naturalName(actionName)}')}" />
                </div>
            </adminlte:form>
        </div>
    </div>
</div>