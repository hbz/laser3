<%@ page import="de.laser.RefdataCategory; de.laser.helper.RDConstants" %>
<div class="field required">
    <label for="accessMethod">${message(code: 'accessMethod.label')} ${message(code: 'messageRequiredField')}</label>
    <laser:select class="ui dropdown" id="accessMethod" name="accessMethod"
                  from="${RefdataCategory.getAllRefdataValues(RDConstants.ACCESS_POINT_TYPE)}"
                  optionKey="value"
                  optionValue="value"
                  value="${accessMethod}"
                  onchange="${laser.remoteJsOnChangeHandler(
                          controller: 'accessPoint',
                          action: 'create',
                          data: '{template:this.value}',
                          update: '#details',
                  )}"
    />
</div>

