<%@ page import="de.laser.helper.ConfigUtils; de.laser.helper.RDStore; de.laser.helper.RDConstants;de.laser.Package;de.laser.RefdataCategory;org.springframework.web.servlet.support.RequestContextUtils" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'package.details')}</title>
        <asset:stylesheet src="datatables.css"/><laser:javascript src="datatables.js"/>%{-- dont move --}%
    </head>
    <body>

    <semui:debugInfo>
        %{--<g:render template="/templates/debug/orgRoles" model="[debug: packageInstance.orgs]" />--}%
        %{--<g:render template="/templates/debug/prsRoles" model="[debug: packageInstance.prsLinks]" />--}%
    </semui:debugInfo>

    <g:set var="locale" value="${RequestContextUtils.getLocale(request)}" />

    <semui:modeSwitch controller="package" action="show" params="${params}"/>

    <semui:breadcrumbs>
        <semui:crumb controller="package" action="index" message="package.show.all" />
        <semui:crumb class="active" text="${packageInstance.name}" />
    </semui:breadcrumbs>

    <semui:controlButtons>
        <%-- TODO [ticket=1142,1996]
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
        --%>
        <g:render template="actions" />
    </semui:controlButtons>

      <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
          <g:if test="${editable}"><span id="packageNameEdit"
                    class="xEditableValue"
                    data-type="textarea"
                    data-pk="${packageInstance.class.name}:${packageInstance.id}"
                    data-name="name"
                    data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
          <g:else>${packageInstance.name}</g:else>
      </h1>

    <g:render template="nav" />

    <semui:objectStatus object="${packageInstance}" status="${packageInstance.packageStatus}" />

    <g:render template="/templates/meta/identifier" model="${[object: packageInstance, editable: editable]}" />

    <semui:messages data="${flash}" />

    <semui:errors bean="${packageInstance}" />

    <div class="ui grid">

        <div class="twelve wide column">
            <g:hiddenField name="version" value="${packageInstance?.version}" />
            <div class="la-inline-lists">
                <div class="ui two cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'package.show.start_date')}</dt>
                                <dd>
                                    ${packageInstance.startDate}
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.show.end_date')}</dt>
                                <dd>
                                    ${packageInstance.endDate}
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.listVerifiedDate.label')}</dt>
                                <dd>
                                    ${packageInstance.listVerifiedDate}
                                </dd>
                            </dl>
                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'default.status.label')}</dt>
                                <dd>${packageInstance.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.packageListStatus')}</dt>
                                <dd>
                                    ${packageInstance.packageListStatus}
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.scope')}</dt>
                                <dd>
                                    ${packageInstance.packageScope}
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'package.nominalPlatform')}</dt>
                            <dd>
                                <g:if test="${packageInstance.nominalPlatform}">
                                    <g:link controller="platform" action="show" id="${packageInstance.nominalPlatform.id}">${packageInstance.nominalPlatform.name}</g:link>
                                </g:if>
                            </dd>
                        </dl>

                    </div>
                </div>
                <div class="ui card">
                    <div class="content">

                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgs,
                                            roleObject: packageInstance,
                                            roleRespValue: 'Specific package editor',
                                            editmode: editable,
                                            showPersons: true
                                  ]}" />

                        <g:render template="/templates/links/orgLinksModal"
                              model="${[linkType:packageInstance?.class?.name,
                                        parent: packageInstance.class.name+':'+packageInstance.id,
                                        property: 'orgs',
                                        recip_prop: 'pkg',
                                        tmplRole: RDStore.OR_CONTENT_PROVIDER,
                                        tmplText:'Anbieter hinzufügen',
                                        tmplID:'ContentProvider',
                                        tmplButtonText: 'Anbieter hinzufügen',
                                        tmplModalID:'osel_add_modal_anbieter',
                                        editmode: editable
                              ]}" />

                    </div>
                </div>


                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'package.breakable')}</dt>
                            <dd>
                                ${packageInstance.breakable}
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'package.consistent')}</dt>
                            <dd>
                                ${packageInstance.consistent}
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'package.fixed')}</dt>
                            <dd>
                                ${packageInstance.fixed}
                            </dd>
                        </dl>

                        <g:if test="${statsWibid && packageIdentifier}">
                            <dl>
                               <dt><g:message code="package.show.usage"/></dt>
                               <dd>
                                    <laser:statsLink class="ui basic negative"
                                                     base="${ConfigUtils.getStatsApiUrl()}"
                                                     module="statistics"
                                                     controller="default"
                                                     action="select"
                                                     target="_blank"
                                                     params="[mode:usageMode,
                                                              packages:packageInstance.getIdentifierByType('isil').value,
                                                              institutions:statsWibid
                                                     ]"
                                                     title="${message(code:'default.jumpToNatStat')}">
                                        <i class="chart bar outline icon"></i>
                                    </laser:statsLink>
                                </dd>
                            </dl>
                        </g:if>

                    </div>
                </div>
            </div>
        </div><!-- .twelve -->


        <aside class="four wide column la-sidekick">
            <g:render template="/templates/aside1" model="${[ownobj:packageInstance, owntp:'pkg']}" />
        </aside><!-- .four -->

    </div><!-- .grid -->

    </body>
</html>
