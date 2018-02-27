 <g:if test="${processingpc}">
     <bootstrap:alert class="alert-warning"><g:message code="pendingchange.inprogress"/></bootstrap:alert>
</g:if>
<g:if test="${pendingChanges?.size() > 0}">
    <div class="alert-warn">
        <h6 class="ui header">${message(code:'template.pendingChanges', default:'There are pending change notifications')}</h6>
        <g:if test="${editable && !processingpc}">
            <g:link controller="pendingChange" action="acceptAll" params="[OID: model.class.name + ':' + model.id]"
                    class="ui positive button">
              ${message(code:'template.pendingChanges.accept_all', default:'Accept All')}
          </g:link>
            <g:link controller="pendingChange" action="rejectAll" params="[OID: model.class.name + ':' + model.id]"
                    class="ui negative button">
              ${message(code:'template.pendingChanges.reject_all', default:'Reject All')}
          </g:link>
        </g:if>
        <br/>&nbsp;<br/>
        <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'default.info.label', default:'Info')}</th>
                <th>${message(code: 'event.timestamp', default: 'Timestamp')}</th>
              <th>${message(code:'default.actions.label', default:'Action')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${pendingChanges}" var="pc">
              <tr>
                <td>${pc.desc}</td>
                  <td><g:formatDate format="${message(code: 'default.date.format')}" date="${pc.ts}"/></td>
                <td>
                  <g:if test="${editable && !processingpc}">
                    <g:link controller="pendingChange" action="accept" id="${pc.id}" class="ui icon positive button">
                      <i class="checkmark icon"></i>
                      <!--${message(code:'default.button.accept.label', default:'Accept')}-->
                    </g:link>
                    <g:link controller="pendingChange" action="reject" id="${pc.id}" class="ui icon negative button">
                      <i class="minus icon"></i>
                      <!--${message(code:'default.button.reject.label', default:'Reject')}-->
                    </g:link>
                  </g:if>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
    </div>

    <br />
</g:if>
