<div class="control-group">
  <label class="control-label" for="orgType">${message(code:'org.incomingCombos.label', default:'Incoming Combos')}</label>
  <div class="controls">
    <g:if test="${orgInstance.incomingCombos && orgInstance.incomingCombos.size() > 0}">
      <ul>
        <g:each in="${orgInstance.incomingCombos}" var="ic">
          <li><g:link controller="organisations" action="show" id="${ic.fromOrg.id}" >${ic.fromOrg.name}</g:link></li>
        </g:each>
      </ul>
    </g:if>
    <g:if test="${orgInstance.orgType?.value == 'Consortium'}">
        <g:form name="addIncomingCombo" controller="organisations" action="addOrgCombo" class="form-search" method="get">
          <g:hiddenField name="toOrg" value="${orgInstance.id}" />
          <g:select name="fromOrg"
                    from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? and o <> ? order by o.name', ['Higher Education', orgInstance])}"
                    optionKey="id"
                    optionValue="name"
                    class="input-medium"/>
          <button id="submitAICForm"
                  data-complete-text="Add Org Combo"
                  type="submit"
                  class="btn btn-primary btn-small">
            ${message(code: 'default.add.label', args:[message(code:'combo.label', default:'Combo')],default:'Add Combo')}
          </button>
        </g:form>
    </g:if>
  </div>
</div>
