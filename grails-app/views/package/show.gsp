<%@ page import="de.laser.utils.DateUtils; de.laser.helper.ConfigMapper; de.laser.storage.RDStore; de.laser.storage.RDConstants;de.laser.Package;de.laser.RefdataValue;org.springframework.web.servlet.support.RequestContextUtils; de.laser.Org; de.laser.Package; de.laser.Platform; java.text.SimpleDateFormat;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'package.details')}</title>
</head>

<body>

<semui:debugInfo>
%{--<laser:render template="/templates/debug/orgRoles" model="[debug: packageInstance.orgs]" />--}%
%{--<laser:render template="/templates/debug/prsRoles" model="[debug: packageInstance.prsLinks]" />--}%
</semui:debugInfo>

<g:set var="locale" value="${RequestContextUtils.getLocale(request)}"/>

<semui:modeSwitch controller="package" action="show" params="${params}"/>

<semui:breadcrumbs>
    <semui:crumb controller="package" action="index" message="package.show.all"/>
    <semui:crumb class="active" text="${packageInstance.name}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
    <g:if test="${editable}"><span id="packageNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${packageInstance.class.name}:${packageInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax"
                                                           action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
    <g:else>${packageInstance.name}</g:else>
</h1>

<laser:render template="nav"/>

<semui:objectStatus object="${packageInstance}" status="${packageInstance.packageStatus}"/>

<laser:render template="/templates/meta/identifier" model="${[object: packageInstance, editable: false]}"/>

<semui:messages data="${flash}"/>

<semui:errors bean="${packageInstance}"/>

<g:if test="${packageInstanceRecord}">
    <div class="ui grid">

        <div class="twelve wide column">
            <div class="la-inline-lists">
                <div class="ui two cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'default.status.label')}</dt>
                                <dd>${packageInstance.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.show.altname')}</dt>
                                <dd>
                                    <div class="ui bulleted list">
                                        <g:each in="${packageInstanceRecord.altname}" var="altname">
                                            <div class="item">${altname}</div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.curatoryGroup.label')}</dt>
                                <dd>
                                    <div class="ui bulleted list">
                                        <g:each in="${packageInstanceRecord.curatoryGroups}" var="curatoryGroup">
                                            <div class="item"><g:link url="${editUrl}resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}</g:link></div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.lastUpdated.label')}</dt>
                                <dd>
                                    <g:if test="${packageInstanceRecord.lastUpdatedDisplay}">
                                        <g:formatDate formatName="default.date.format.notime"
                                                      date="${DateUtils.parseDateGeneric(packageInstanceRecord.lastUpdatedDisplay)}"/>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.source.label')}</dt>
                                <dd>
                                    <g:if test="${packageInstanceRecord.source?.automaticUpdates}">
                                        <g:message code="package.index.result.automaticUpdates"/>
                                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                              data-content="${packageInstanceRecord.source.frequency}">
                                            <i class="question circle icon"></i>
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <g:message code="package.index.result.noAutomaticUpdates"/>
                                    </g:else>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.file')}</dt>
                                <dd>${packageInstance.file?.getI10n("value")}</dd>
                            </dl>
                        </div>
                    </div>

                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'package.contentType.label')}</dt>
                                <dd>${packageInstance.contentType?.getI10n("value")}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.breakable')}</dt>
                                <dd>${packageInstanceRecord.breakable ? RefdataValue.getByValueAndCategory(packageInstanceRecord.breakable, RDConstants.PACKAGE_BREAKABLE).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>
                            <%--<dl>
                                <dt>${message(code: 'package.consistent')}</dt>
                                <dd>${packageInstanceRecord.consistent ? RefdataValue.getByValueAndCategory(packageInstanceRecord.consistent, RDConstants.PACKAGE_CONSISTENT).getI10n("value") : message(code: 'default.not.available')}</dd>
                            </dl>--%>
                            <dl>
                                <dt>${message(code: 'package.scope.label')}</dt>
                                <dd>
                                    ${packageInstanceRecord.scope ? RefdataValue.getByValueAndCategory(packageInstanceRecord.scope, RDConstants.PACKAGE_SCOPE).getI10n("value") : message(code: 'default.not.available')}
                                    <g:if test="${packageInstanceRecord.scope == RDStore.PACKAGE_SCOPE_NATIONAL.value}">
                                        <dl>
                                            <dt>${message(code: 'package.nationalRange.label')}</dt>
                                            <g:if test="${packageInstanceRecord.nationalRanges}">
                                                <dd>
                                                    <div class="ui bulleted list">
                                                        <g:each in="${packageInstanceRecord.nationalRanges}" var="nr">
                                                            <div class="item">${RefdataValue.getByValueAndCategory(nr.value,RDConstants.COUNTRY) ? RefdataValue.getByValueAndCategory(nr.value,RDConstants.COUNTRY).getI10n('value') : nr}</div>
                                                        </g:each>
                                                    </div>
                                                </dd>
                                            </g:if>
                                        </dl>
                                        <dl>
                                            <dt>${message(code: 'package.regionalRange.label')}</dt>
                                            <g:if test="${packageInstanceRecord.regionalRanges}">
                                                <dd>
                                                    <div class="ui bulleted list">
                                                        <g:each in="${packageInstanceRecord.regionalRanges}" var="rr">
                                                            <div class="item">${RefdataValue.getByValueAndCategory(rr.value,RDConstants.REGIONS_DE) ? RefdataValue.getByValueAndCategory(rr.value,RDConstants.REGIONS_DE).getI10n('value') : rr}</div>
                                                        </g:each>
                                                    </div>
                                                </dd>
                                            </g:if>
                                        </dl>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.paymentType.label')}</dt>
                                <dd>${RefdataValue.getByValueAndCategory(packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE) ? RefdataValue.getByValueAndCategory(packageInstanceRecord.paymentType,RDConstants.PAYMENT_TYPE).getI10n("value") : packageInstanceRecord.paymentType}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.openAccess.label')}</dt>
                                <dd>${packageInstanceRecord.openAccess ? RefdataValue.getByValueAndCategory(packageInstanceRecord.openAccess, RDConstants.LICENSE_OA_TYPE)?.getI10n("value") : RefdataValue.getByValueAndCategory('Empty', RDConstants.LICENSE_OA_TYPE).getI10n("value")}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.ddc.label')}</dt>
                                <dd>
                                    <div class="ui bulleted list">
                                        <g:each in="${packageInstanceRecord.ddcs}" var="ddc">
                                            <div class="item">${RefdataValue.getByValueAndCategory(ddc.value,RDConstants.DDC) ? RefdataValue.getByValueAndCategory(ddc.value,RDConstants.DDC).getI10n('value') : message(code:'package.ddc.invalid')}</div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.archivingAgency.label')}</dt>
                                <dd>
                                    <div class="ui bulleted list">
                                        <g:each in="${packageInstanceRecord.packageArchivingAgencies}" var="arcAgency">
                                            <div class="item">
                                                <ul style="list-style-type: none">
                                                    <li>${arcAgency.archivingAgency ? RefdataValue.getByValueAndCategory(arcAgency.archivingAgency, RDConstants.ARCHIVING_AGENCY).getI10n("value") : message(code: 'package.archivingAgency.invalid')}</li>
                                                    <li>${message(code: 'package.archivingAgency.openAccess.label')}: ${arcAgency.openAccess ? RefdataValue.getByValueAndCategory(arcAgency.openAccess, RDConstants.Y_N_P).getI10n("value") : ""}</li>
                                                    <li>${message(code: 'package.archivingAgency.postCancellationAccess.label')}: ${arcAgency.postCancellationAccess ? RefdataValue.getByValueAndCategory(arcAgency.postCancellationAccess, RDConstants.Y_N_P).getI10n("value") : ""}</li>
                                                </ul>
                                            </div>
                                        </g:each>
                                    </div>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'platform.label')}</dt>
                            <dd>
                                <g:if test="${packageInstance.nominalPlatform}">
                                    <g:link controller="platform" action="show"
                                            id="${packageInstance.nominalPlatform.id}">${packageInstance.nominalPlatform.name}</g:link>

                                    <g:if test="${packageInstance.nominalPlatform.primaryUrl}">
                                        <semui:linkIcon
                                                href="${packageInstance.nominalPlatform.primaryUrl?.startsWith('http') ? packageInstance.nominalPlatform.primaryUrl : 'http://' + packageInstance.nominalPlatform.primaryUrl}"/>
                                    </g:if>
                                </g:if>
                            </dd>
                        </dl>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">

                        <laser:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks    : visibleOrgs,
                                            roleObject   : packageInstance,
                                            roleRespValue: 'Specific package editor',
                                            editmode     : editable,
                                            showPersons  : true
                                  ]}"/>
                    </div>
                </div>


                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'default.description.label')}</dt>
                            <dd>
                                <g:if test="${packageInstanceRecord.description}">
                                    ${packageInstanceRecord.description}
                                </g:if>
                            </dd>
                        </dl>
                        <g:if test="${packageInstanceRecord.descriptionURL}">
                            <dl>
                                <dt>${message(code: 'default.url.label')}</dt>
                                <dd>
                                    ${packageInstanceRecord.descriptionURL}
                                    <semui:linkIcon
                                            href="${packageInstanceRecord.descriptionURL.startsWith('http') ? packageInstanceRecord.descriptionURL : 'http://' + packageInstanceRecord.descriptionURL}"/>
                                </dd>
                            </dl>
                        </g:if>
                        %{--<dl>
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
    --}%
                        <%-- deactivated U.F.N. - do not delete as prespectively needed!
                        <g:if test="${statsWibid && packageIdentifier}">
                            <dl>
                                <dt><g:message code="package.show.usage"/></dt>
                                <dd>
                                    <laser:statsLink class="ui basic negative"
                                                     base="${ConfigMapper.getStatsApiUrl()}"
                                                     module="statistics"
                                                     controller="default"
                                                     action="select"
                                                     target="_blank"
                                                     params="[mode        : usageMode,
                                                              packages    : packageInstance.getIdentifierByType('isil').value,
                                                              institutions: statsWibid
                                                     ]"
                                                     title="${message(code: 'default.jumpToNatStat')}">
                                        <i class="chart bar outline icon"></i>
                                    </laser:statsLink>
                                </dd>
                            </dl>
                        </g:if>
                        --%>
                    </div>
                </div>
            </div>
        </div><!-- .twelve -->


    %{-- <aside class="four wide column la-sidekick">
         <laser:render template="/templates/aside1" model="${[ownobj:packageInstance, owntp:'pkg']}" />
     </aside><!-- .four -->--}%

    </div><!-- .grid -->
</g:if>

</body>
</html>
