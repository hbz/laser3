<%@ page import="com.k_int.kbplus.*" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<% def securityService = grailsApplication.mainContext.getBean("springSecurityService") %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'license.details.addMembers.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentLicenses" text="${message(code: 'license.current')}"/>
    <semui:crumb controller="licenseDetails" action="show" id="${license.id}" text="${license.reference}"/>
    <semui:crumb class="active" text="${message(code: 'license.details.addMembers.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui header"><semui:headerIcon />
    <g:if test="${license.type?.value == 'Template'}">${message(code:'license.label')} (${license.type.getI10n('value')}):</g:if>
    <semui:xEditable owner="${license}" field="reference" id="reference"/>
</h1>

<g:render template="nav" />

<g:if test="${license.instanceOf && (contextOrg == license.getLicensor())}">
    <div class="ui negative message">
        <div class="header"><g:message code="myinst.message.attention" /></div>
        <p>
            <g:message code="myinst.licenseDetails.message.ChildView" />
            <span class="ui label">${license.getLicensee()?.collect{itOrg -> itOrg.name}?.join(',')}</span>.
        <g:message code="myinst.licenseDetails.message.ConsortialView" />
        <g:link controller="licenseDetails" action="show" id="${license.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
        </p>
    </div>
</g:if>

<g:if test="${institution?.orgType?.value == 'Consortium'}">

    <semui:filter>
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}"/>
            <g:render template="/templates/filter/orgFilter"
                      model="[tmplConfigShow: ['name', 'federalState', 'libraryNetwork', 'libraryType']
                      ]"/>
        </g:form>
    </semui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="licenseDetails" method="post" class="ui form">

        <input type="hidden" name="asOrgType" value="${institution?.orgType?.id}">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: cons_members,
                          tmplDisableOrgIds: cons_members_disabled,
                          subInstance: subscriptionInstance,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['name', 'wib', 'isil', 'federalState', 'libraryNetwork', 'libraryType']
                          ]"/>

        <g:if test="${cons_members}">
            <div class="ui two fields">
                <div class="field">
                    <label>Vertrag kopieren</label>
                    <div class="ui radio checkbox">
                        <g:if test="${license.derivedLicenses}">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="one">
                        </g:if>
                        <g:else>
                            <input class="hidden" type="radio" name="generateSlavedLics" value="one" checked="checked">
                        </g:else>
                        <label>${message(code: 'myinst.emptySubscription.seperate_lics_one')}</label>
                    </div>
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" name="generateSlavedLics" value="multiple">
                        <label>${message(code: 'myinst.emptySubscription.seperate_lics_multiple')}</label>
                    </div>
                    <g:if test="${license.derivedLicenses}">
                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="reference" checked="checked">
                            <label>${message(code: 'myinst.emptySubscription.seperate_lics_reference')}</label>
                        </div>

                        <br />
                        <br />
                        <g:select from="${license.derivedLicenses}" class="ui search dropdown"
                                  optionKey="${{ 'com.k_int.kbplus.License:' + it.id }}"
                                  optionValue="${{ it.getGenericLabel() }}"
                                  name="generateSlavedLicsReference"
                            />
                    </g:if>
                </div>
            </div>

            <br/>
            <g:if test="${cons_members}">
                <input type="submit" class="ui button js-click-control"
                        value="${message(code: 'default.button.create.label', default: 'Create')}"/>
            </g:if>
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
