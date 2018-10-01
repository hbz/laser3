<%@ page import="com.k_int.kbplus.*" %>
<laser:serviceInjection />

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

<g:if test="${license.instanceOf && (contextOrg == license.getLicensingConsortium())}">
    <div class="ui negative message">
        <div class="header"><g:message code="myinst.message.attention" /></div>
        <p>
            <g:message code="myinst.licenseDetails.message.ChildView" />
            <span class="ui label">${license.getAllLicensee()?.collect{itOrg -> itOrg.name}?.join(',')}</span>.
        <g:message code="myinst.licenseDetails.message.ConsortialView" />
        <g:link controller="licenseDetails" action="show" id="${license.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
        </p>
    </div>
</g:if>

<g:if test="${(com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType') in  institution.getallOrgRoleType())}">

    <div class="ui info message">
        <div class="header">
            Hinweis
        </div>
        <p>
            Angezeigt werden die Konsorten der verknüpften Lizenzen.
            Werden keine Konsorten angezeigt, dann sind evtl. die Lizenzen nicht verknüpft oder es sind diesen noch keine Teilnehmer zugeordnet.
        </p>
    </div>

    <semui:filter>
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}"/>
            <g:render template="/templates/filter/orgFilter"
                      model="[tmplConfigShow: ['name', 'federalState', 'libraryNetwork', 'libraryType']
                      ]"/>
        </g:form>
    </semui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="licenseDetails" method="post" class="ui form">

        <g:render template="/templates/filter/orgFilterTable"
                  model="[orgList: cons_members,
                          tmplDisableOrgIds: cons_members_disabled,
                          subInstance: subscriptionInstance,
                          tmplShowCheckbox: true,
                          tmplConfigShow: ['sortname', 'name', 'wibid', 'isil', 'federalState', 'libraryNetwork', 'libraryType']
                          ]"/>

        <g:if test="${cons_members}">
            <div class="ui two fields">
                <div class="field">
                    <label>Vertrag kopieren</label>
                    <div class="ui radio checkbox">
                        <input class="hidden" type="radio" name="generateSlavedLics" value="shared" checked="checked">
                        <label>${message(code: 'rolemyinst.emptySubscription.seperate_lics_shared')}</label>
                    </div>

                    <div class="ui radio checkbox">
                        <g:if test="${license.derivedLicenses}">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="explicit">
                        </g:if>
                        <g:else>
                            <input class="hidden" type="radio" name="generateSlavedLics" value="explicit">
                        </g:else>
                        <label>${message(code: 'myinst.emptySubscription.seperate_lics_explicit')}</label>
                    </div>

                    <g:if test="${license.derivedLicenses}">
                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" name="generateSlavedLics" value="reference">
                            <label>${message(code: 'myinst.emptySubscription.seperate_lics_reference')}</label>
                        </div>

                        <div class="generateSlavedLicsReference-wrapper hidden">
                            <br />
                            <g:select from="${license.derivedLicenses}" class="ui search dropdown"
                                      optionKey="${{ 'com.k_int.kbplus.License:' + it.id }}"
                                      optionValue="${{ it.getGenericLabel() }}"
                                      name="generateSlavedLicsReference"
                                />
                        </div>
                        <r:script>
                            $('*[name=generateSlavedLics]').change( function(){
                                $('*[name=generateSlavedLics][value=reference]').prop('checked') ?
                                        $('.generateSlavedLicsReference-wrapper').removeClass('hidden') :
                                        $('.generateSlavedLicsReference-wrapper').addClass('hidden') ;
                            })
                            $('*[name=generateSlavedLics]').trigger('change')
                        </r:script>
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

    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM") && (com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType') in  contextService.getOrg().orgRoleType)}">
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
