<ul class="nav nav-pills">

  <li <%='index'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails" action="index" params="${[id:params.id]}">${message(code:'subscription.details.current_ent', default:'Current Entitlements')}</g:link></li>

  <li <%='details'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails" action="details" params="${[id:params.id]}">${message(code:'subscription.details.details.label', default:'Details')}</g:link></li>

  <g:if test="${editable}">
    <li <%='linkPackage'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                    action="linkPackage"
                    params="${[id:params.id]}">${message(code:'subscription.details.linkPackage.label', default:'Link Package')}</g:link></li>
  </g:if>

  <g:if test="${editable}">
    <li <%='addEntitlements'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
      action="addEntitlements"
      params="${[id:params.id]}">${message(code:'subscription.details.addEntitlements.label', default:'Add Entitlements')}</g:link></li>
  </g:if>

  <li<%='renewals'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
              action="renewals"
              params="${[id:params.id]}">${message(code:'subscription.details.renewals.label', default:'Renewals')}</g:link></li>

    <li <%='previous'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                                                                      action="previous"
                                                                      params="${[id:params.id]}">${message(code:'subscription.details.previous.label', default:'Previous')}</g:link></li>
    <li <%='expected'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                                                                       action="expected"
                                                                       params="${[id:params.id]}">${message(code:'subscription.details.expected.label', default:'Expected')}</g:link></li>

    <g:if test="${grailsApplication.config.feature_finance}">
    <li <%='costPerUse'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                                                                   action="costPerUse"
                                                                   params="${[id:params.id]}">${message(code:'subscription.details.costPerUse.label', default:'Cost Per Use')}</g:link></li>
    </g:if>

</ul>
<ul class="nav nav-pills">
<li <%='documents'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
            action="documents"
            params="${[id:params.id]}">${message(code:'default.documents.label', default:'Documents')}</g:link></li>

<li<%='notes'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
            action="notes"
            params="${[id:params.id]}">${message(code:'default.notes.label', default:'Notes')}</g:link></li>

<li <%='additionalInfo'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                  action="additionalInfo"
                  params="${[id:params.id]}">${message(code:'default.additionalInfo.label', default:'Additional Info')}</g:link></li>

<li <%='edit_history'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                  action="edit_history"
                  params="${[id:params.id]}">${message(code:'licence.nav.edit_history', default:'Edit History')}</g:link></li>

<li <%='todo_history'== actionName ? ' class="active"' : '' %>><g:link controller="subscriptionDetails"
                action="todo_history"
                params="${[id:params.id]}">${message(code:'licence.nav.todo_history', default:'ToDo History')}</g:link></li>

<g:if test="${grailsApplication.config.feature_finance}">
    %{--Custom URL mapping for re-use of index--}%
    <li <%='finance'== actionName ? ' class="active"' : '' %>>
        <g:link mapping="subfinance" controller="finance" action="index"
                params="${[sub:params.id, shortcode: institution?.shortcode]}">${message(code:'subscription.details.financials.label', default:'Subscription Financials')}</g:link></li>
</g:if>

</ul>
