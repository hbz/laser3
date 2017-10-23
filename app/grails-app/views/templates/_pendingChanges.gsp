 <g:if test="${processingpc}">
  <div class="container"><bootstrap:alert class="alert-warning"><g:message code="pendingchange.inprogress"/></bootstrap:alert></div>
</g:if>
<g:if test="${pendingChanges?.size() > 0}">
  <div class="container alert-warn">
    <h6>${message(code:'template.pendingChanges', default:'There are pending change notifications')}</h6>
    <g:if test="${editable && !processingpc}">
      <g:link controller="pendingChange" action="acceptAll" id="${model.class.name}:${model.id}" class="btn btn-success"><i class="icon-white icon-ok "></i>${message(code:'template.pendingChanges.accept_all', default:'Accept All')}</g:link>
      <g:link controller="pendingChange" action="rejectAll" id="${model.class.name}:${model.id}" class="btn btn-danger"><i class="icon-white icon-remove"></i>${message(code:'template.pendingChanges.reject_all', default:'Reject All')}</g:link>
    </g:if>
    <br/>&nbsp;<br/>
    <table class="ui celled table">
      <thead>
        <tr>
          <td>${message(code:'default.info.label', default:'Info')}</td>
          <td>${message(code:'default.actions.label', default:'Action')}</td>
        </tr>
      </thead>
      <tbody>
        <g:each in="${pendingChanges}" var="pc">
          <tr>
            <td>${pc.desc}</td>
            <td>
              <g:if test="${editable && !processingpc}">
                <g:link controller="pendingChange" action="accept" id="${pc.id}" class="btn btn-success"><i class="icon-white icon-ok"></i>${message(code:'default.button.accept.label', default:'Accept')}</g:link>
                <g:link controller="pendingChange" action="reject" id="${pc.id}" class="btn btn-danger"><i class="icon-white icon-remove"></i>${message(code:'default.button.reject.label', default:'Reject')}</g:link>
              </g:if>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </div>
</g:if>
