<laser:htmlStart message="menu.institutions.change_log" />

  <ui:breadcrumbs>
    <ui:crumb message="menu.institutions.change_log" class="active" />
  </ui:breadcrumbs>


  <ui:controlButtons>
    <ui:exportDropdown>
      <ui:exportDropdownItem>
        <g:link controller="myInstitution" class="item" action="changeLog" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
      </ui:exportDropdownItem>
    </ui:exportDropdown>
    <laser:render template="actions"/>
  </ui:controlButtons>

  <ui:h1HeaderWithIcon message="menu.institutions.change_log" />

        <p>
          <strong>
            <span class="ui circular label">${num_changes}</span> <g:message code="default.matches.label"/>
          </strong>
        </p>

        <g:form method="get" action="changeLog" params="${params}">
          ${message(code:'myinst.changeLog.restrictTo')}:
          <select class="ui dropdown" name="restrict" onchange="this.form.submit()">
            <option value="${message(code:'myinst.changelog.all')}">${message(code:'myinst.changelog.all')}</option>
            <g:each in="${institutional_objects}" var="io">
              <option value="${io[0]}" ${(params.restrict?.equals(io[0]) ? 'selected' : '')}>${io[1]}</option>
            </g:each>
          </select>
        </g:form>

      <table class="ui celled la-js-responsive-table la-table table">
        <g:each in="${changes}" var="chg">
          <tr>
            <td><g:formatDate format="yyyy-MM-dd" date="${chg.ts}"/>
            </td>
            <td>
              <g:if test="${chg.subscription != null}">${message(code:'subscription.change.to')} <g:link controller="subscription" action="index" id="${chg.subscription.id}">${chg.subscription.name} </g:link></g:if>
              <g:if test="${chg.license != null}">${message(code:'license.change.to')} <g:link controller="license" action="show" id="${chg.license.id}">${chg.license.reference}</g:link></g:if>
              <g:if test="${chg.pkg != null}">${message(code:'package.change.to')} <g:link controller="package" action="show" id="${chg.pkg.id}">${chg.pkg.name}</g:link></g:if>
            </td>
            <td>
              <% print chg.desc; /* avoid auto encodeAsHTML() */ %>
              ${chg.status} on ${chg.actionDate}
            </td>
          </tr>
        </g:each>
      </table>

        <ui:paginate action="changeLog" controller="myInstitution" params="${params}" max="${max}" total="${num_changes}" />

<laser:htmlEnd />
