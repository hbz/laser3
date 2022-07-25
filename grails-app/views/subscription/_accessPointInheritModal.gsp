<g:if test="${editmode}">
  <a class="ui basic ${tmplCss}" data-ui="modal" href="#${tmplModalID}">
</g:if>
  <g:if test="${tmplIcon}">
    <i class="${tmplIcon} icon"></i>
  </g:if>
  <g:if test="${tmplButtonText}">
    ${tmplButtonText}
  </g:if>
<g:if test="${editmode}">
  </a>
</g:if>
<g:if test="${editmode && tmplID == 'addDerivation'}">
  <ui:modal id="${tmplModalID}" text="${tmplText}" formID="addDerivationForm-${subscriptionPackage.id}" msgSave="${message(code:'subscription.details.linkAccessPoint.accessConfig.modal.addDerivation.msgSave')}">
    <g:form id="addDerivationForm-${subscriptionPackage.id}" method="get" class="form-inline ui small form"
            url="[controller: 'platform', action: 'addDerivation']">
      <input type="hidden" name="platform_id" value="${platformInstance.id}">
      <input type="hidden" name="sp" value="${subscriptionPackage.id}">
      <div class="field">
        <div class="ui grid">
          <div class="row">
            <div class="column">
              ${message(code:'subscription.details.linkAccessPoint.accessConfig.modal.addDerivation.content')}
            </div>
          </div>
      </div>
    </g:form>
  </ui:modal>
</g:if>
<g:elseif test="${editmode}">
  <ui:modal id="${tmplModalID}" text="${tmplText}" formID="removeDerivationForm-${subscriptionPackage.id}" msgSave="${message(code:'subscription.details.linkAccessPoint.accessConfig.modal.removeDerivation.msgSave')}">
    <g:form id="removeDerivationForm-${subscriptionPackage.id}" method="get" class="form-inline ui small form"
            url="[controller: 'platform', action: 'removeDerivation']">
      <input type="hidden" name="platform_id" value="${platformInstance.id}">
      <input type="hidden" name="sp" value="${subscriptionPackage.id}">
      <div class="field">
        <div class="ui grid">
          <div class="row">
            <div class="column">
              ${message(code:'subscription.details.linkAccessPoint.accessConfig.modal.removeDerivation.content', args: [platformInstance.name])}
            </div>
          </div>
      </div>
    </g:form>
  </ui:modal>
</g:elseif>
