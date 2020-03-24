<semui:form>
  <g:form action="create_${accessMethod}" controller="accessPoint" method="post" class="ui form">
    <g:render template="access_method" model="${[accessMethod: accessMethod]}"/>
    <div required="" class="field required">
      <label>${message(code: 'accessPoint.oa.name.label')}
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-tooltip="${message(code:'accessPoint.oa.help')}">
          <i class="question circle icon la-popup"></i></span>
      </label>
      <g:field type="text" name="name" value="" />
    </div>
    <div class="field required">
      <label>${message(code: 'accessPoint.entitiyId.label', default: 'EntityId')}</label>
      <g:textField name="entityId" value="${entityId}" />
    </div>
    <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label')}"/>
  </g:form>
</semui:form>