<%@ page import="com.k_int.kbplus.*" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<% def securityService = grailsApplication.mainContext.getBean("springSecurityService") %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.addMembers.label')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="currentSubscriptions"
                     text="${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}"/>
        <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"
                     text="${subscriptionInstance.name}"/>
        <semui:crumb class="active"
                     text="${message(code: 'subscription.details.addMembers.label', default: 'Add Members')}"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
        <g:render template="actions"/>
    </semui:controlButtons>

    <h1 class="ui header"><semui:headerIcon/>
        <g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name"
                   class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
    </h1>

    <g:render template="nav" contextPath="."/>

<g:if test="${institution?.orgType?.value == 'Consortium'}">

    <semui:filter>
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}"/>
            <g:render template="/templates/filter/orgFilter"
                      model="[tmplConfigShow: ['name', 'federalState', 'libraryNetwork', 'libraryType']
                      ]"/>
        </g:form>
    </semui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="subscriptionDetails" method="post" class="ui form">

        <input type="hidden" name="asOrgType" value="${institution?.orgType?.id}">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: cons_members,
                          tmplDisableOrgIds: cons_members_disabled,
                          subInstance: subscriptionInstance,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['name', 'wib', 'isil', 'federalState', 'libraryNetwork', 'libraryType', 'addSubMembers']
                          ]"/>

        <g:if test="${cons_members}">
            <div class="ui two fields">
                <div class="field">
                    <label>Lizenz kopieren</label>

                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked"
                               readonly="readonly">
                        <label>${message(code: 'myinst.emptySubscription.seperate_subs', default: 'Generate seperate Subscriptions for all Consortia Members')}</label>
                    </div>


                    <g:set value="${com.k_int.kbplus.RefdataCategory.findByDesc('Subscription Status')}" var="rdcSubStatus"/>

                    <br />
                    <br />
                    <g:select from="${com.k_int.kbplus.RefdataValue.findAllByOwner(rdcSubStatus)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subStatus"
                              value="${com.k_int.kbplus.RefdataValue.findByValueAndOwner('Current', rdcSubStatus)?.id}"/>
                </div>

                <div class="field">
                    <label>Vertrag kopieren</label>
                    <g:if test="${subscriptionInstance.owner}">
                        <div class="ui radio checkbox">
                            <g:if test="${subscriptionInstance.owner.derivedLicenses}">
                                <input class="hidden" type="radio" name="generateSlavedLics" value="no">
                            </g:if>
                            <g:else>
                                <input class="hidden" type="radio" name="generateSlavedLics" value="no" checked="checked">
                            </g:else>
                            <label>${message(code: 'myinst.emptySubscription.seperate_lics_no')}</label>
                        </div>

                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="multiple">
                            <label>${message(code: 'myinst.emptySubscription.seperate_lics_multiple')}</label>
                        </div>

                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="one">
                            <label>${message(code: 'myinst.emptySubscription.seperate_lics_one')}</label>
                        </div>

                        <g:if test="${subscriptionInstance.owner.derivedLicenses}">
                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" name="generateSlavedLics" value="reference" checked="checked">
                                <label>${message(code: 'myinst.emptySubscription.seperate_lics_reference')}</label>
                            </div>

                            <br />
                            <br />
                            <g:select from="${subscriptionInstance.owner?.derivedLicenses}" class="ui search dropdown"
                                      optionKey="${{ 'com.k_int.kbplus.License:' + it.id }}"
                                      optionValue="${{ it.getGenericLabel() }}"
                                      name="generateSlavedLicsReference"
                                />
                        </g:if>

                    </g:if>
                    <g:else>
                        <semui:msg class="info" text="Es ist kein Vertrag vorhanden." />
                    </g:else>
                </div>
            </div>
        </g:if>

        <br/>
        <g:if test="${cons_members}">
            <input type="submit" class="ui button js-click-control"
                   value="${message(code: 'default.button.create.label', default: 'Create')}"/>
        </g:if>
    </g:form>

    <g:if test="${securityService.getCurrentUser().hasAffiliation("INST_ADM") && contextService.getOrg().orgType?.value == 'Consortium'}">
        <hr />

        <div class="ui info message">
            <div class="header">Konsorten verwalten</div>
            <p>
                Sie können bei Bedarf über
                <g:link controller="myInstitution" action="addConsortiaMembers">diesen Link</g:link>
                Ihre Konsorten verwalten ..
            </p>
        </div>
    </g:if>
</g:if>

</body>
</html>
