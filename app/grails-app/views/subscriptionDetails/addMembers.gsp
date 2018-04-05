<%@ page import="com.k_int.kbplus.*" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
        </head>
        <body>
            <semui:breadcrumbs>
                <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
                <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
                <semui:crumb class="active" text="${message(code:'subscription.details.addMembers.label', default:'Add Members')}" />
            </semui:breadcrumbs>
            <semui:controlButtons>
                        <g:render template="actions" />
            </semui:controlButtons>
            <h1 class="ui header"><semui:headerIcon />

                <g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
            </h1>

            <g:render template="nav" contextPath="." />

            <g:if test="${institution?.orgType?.value == 'Consortium'}">

                <semui:filter>
                    <g:form action="addMembers" method="get" params="[id:params.id]" class="ui form">
                        <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}" />
                        <g:render template="/templates/filter/orgFilter" />
                    </g:form>
                </semui:filter>

                <g:form action="processAddMembers" params="${[id:params.id]}" controller="subscriptionDetails" method="post" class="ui form">

                    <input type="hidden" name="asOrgType" value="${institution?.orgType?.id}">

                    <div class="ui field">
                        <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Subscription Status')}" var="rdcSubStatus"/>
                        <label>Status</label>
                        <g:select from="${com.k_int.kbplus.RefdataValue.findAllByOwner(rdcSubStatus)}" class="ui dropdown"
                                  optionKey="id"
                                  optionValue="${{it.getI10n('value')}}"
                                  name="subStatus"
                                  value="${com.k_int.kbplus.RefdataValue.findByValue('Under Consideration')?.id}" />
                    </div>

                    <g:render template="/templates/filter/orgFilterTable" model="[orgList: cons_members, tmplShowCheckbox: true, tmplDisableOrgs: cons_members_disabled]" />

                    <div class="ui field">
                        <div class="ui checkbox">
                            <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                            <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                        </div>
                    </div>

                    <br/>
                    <input type="submit" class="ui button" value="${message(code:'default.button.create.label', default:'Create')}" />
                </g:form>
            </g:if>

  </body>
</html>
