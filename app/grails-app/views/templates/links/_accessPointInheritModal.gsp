<g:if test="${editmode}">
  <a class="ui basic ${tmplCss}" data-semui="modal" href="#${tmplModalID}">
    <g:if test="${tmplIcon}">
      <i class="${tmplIcon} icon"></i>
    </g:if>
    <g:if test="${tmplButtonText}">
      ${tmplButtonText}
    </g:if>
  </a>
</g:if>
<g:if test="${tmplID == 'addDerivation'}">
  <semui:modal id="${tmplModalID}" text="${tmplText}" formID="addDerivationForm-${subscriptionPackage.id}" msgSave="Vererben">
    <g:form id="addDerivationForm-${subscriptionPackage.id}" method="get" class="form-inline ui small form"
            url="[controller: 'platform', action: 'addDerivation']">
      <input type="hidden" name="platform_id" value="${platformInstance.id}">
      <input type="hidden" name="sp" value="${subscriptionPackage.id}">
      <div class="field">
        <div class="ui grid">
          <div class="row">
            <div class="column">
              Sollen die Zugangskonfigurationen von der platform geerbt werden? Achtung! Auf Lizenzebene erstellte Verknüpfungen werden gelöscht!
            </div>
          </div>
      </div>
    </g:form>
  </semui:modal>
</g:if>
<g:else>
  <semui:modal id="${tmplModalID}" text="${tmplText}" formID="removeDerivationForm-${subscriptionPackage.id}" msgSave="Vererbung aufheben">
    <g:form id="removeDerivationForm-${subscriptionPackage.id}" method="get" class="form-inline ui small form"
            url="[controller: 'platform', action: 'removeDerivation']">
      <input type="hidden" name="platform_id" value="${platformInstance.id}">
      <input type="hidden" name="sp" value="${subscriptionPackage.id}">
      <div class="field">
        <div class="ui grid">
          <div class="row">
            <div class="column">
              Zugangskonfiguration X wird von Platform Y vererbt, wollen Sie die Vererbung aufheben?
            </div>
          </div>
      </div>
    </g:form>
  </semui:modal>
</g:else>
