<g:set var="tip" value="${issueEntitlement.getTIP()}" />
<g:if test="${tip}">
  <g:set var="dateFormatter" value="${new java.text.SimpleDateFormat(session.sessionPreferences?.globalDateFormat)}"/>
  <g:set var="date" value="${date ? dateFormatter.parse(date) : null}"/>
  <g:set var="status" value="${tip.coreStatus(date)}"/>
  <g:set var="date_text" 
         value="${status ? message(code:'default.boolean.true', default:'True') + '(' + message(code:'default.now', default:'Now') + ')' : status==null ? message(code:'default.boolean.false', default:'False') + '(' + message(code:'default.never', default:'Never') + ')' : message(code:'default.boolean.false', default:'False') + '(' + message(code:'default.now', default:'Now') + ')'}"/>
  <g:remoteLink url="[controller: 'ajax', action: 'getTipCoreDates', params:[tipID:tip.id,title:issueEntitlement.tipp?.title?.title]]"
                method="get" name="show_core_assertion_modal" onComplete="showCoreAssertionModal()" class="editable-click"
                update="magicArea">${ date_text }</g:remoteLink>
</g:if>
<g:else>
  ${message(code:'subscription.details.core_status.no_provider', default:'Content Provider missing.  Add one as Org Link of the Package.')}
</g:else>
