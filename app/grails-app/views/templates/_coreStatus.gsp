<g:set var="tip" value="${issueEntitlement.getTIP()}" />
<g:if test="${tip}">
  <g:set var="dateFormatter" value="${de.laser.helper.DateUtil.getSDF_NoTime()}"/>
  <g:set var="date" value="${date ? dateFormatter.parse(date) : null}"/>
  <g:set var="status" value="${tip.coreStatus(date)}"/>
  <g:set var="date_text" 
         value="${status ? message(code:'default.boolean.true') + '(' + message(code:'default.now') + ')' : status==null ? message(code:'default.boolean.false') + '(' + message(code:'default.never') + ')' : message(code:'default.boolean.false') + '(' + message(code:'default.now') + ')'}"/>
  <g:remoteLink url="[controller: 'ajax', action: 'getTipCoreDates', params:[tipID:tip.id,title:issueEntitlement.tipp?.title?.title]]"
                method="get" name="show_core_assertion_modal" onComplete="showCoreAssertionModal()" class="editable-click"
                update="magicArea">${ date_text }</g:remoteLink>
</g:if>
<g:else>
  ${message(code:'subscription.details.core_status.no_provider')}
</g:else>
