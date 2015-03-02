<div class="form-group">
    <label for="${targetVarName}.${node.name}"><g:message code="${simpleControllerName}.${targetVarName}.${node.name}" default="${naturalName(node.name)}" /></label>
    <adminlte:textField name="${targetVarName}.${node.name}" placeholder="Enter ${node.name}" value="\${${propertyVar}?.${node.name}}">
</div>