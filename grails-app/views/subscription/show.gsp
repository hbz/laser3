<%@ page import="de.laser.helper.Icons; de.laser.config.ConfigMapper; de.laser.Person; de.laser.PersonRole; de.laser.Subscription; de.laser.Links; java.text.SimpleDateFormat;de.laser.properties.PropertyDefinition; de.laser.OrgRole; de.laser.License;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.interfaces.CalculatedType; de.laser.FormService; de.laser.AuditConfig" %>
<laser:htmlStart message="subscription.details.label" serviceInjection="true"/>

%{-- flyouts --}%
<laser:render template="/templates/flyouts/help/subscription_show"/>


<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>sub.dateCreated: ${subscription.dateCreated}</p>
        <p>sub.lastUpdated: ${subscription.lastUpdated}</p>
        <p>sub.type: ${subscription.type}</p>

        <p>sub.getSubscriberRespConsortia(): ${subscription.getSubscriberRespConsortia()}</p>

        <p>sub.instanceOf: <g:if test="${subscription.instanceOf}">
            <g:link action="show" id="${subscription.instanceOf.id}">${subscription.instanceOf.name}</g:link>
            ${subscription.instanceOf.getAllocationTerm()}
        </g:if></p>

        <p>sub.administrative: ${subscription.administrative}</p>
        <p>getCalculatedType(): ${subscription._getCalculatedType()}</p>
        <p>orgRole(ctxOrg): ${OrgRole.findAllBySubAndOrg(subscription, contextService.getOrg()).roleType.join(', ')}</p>
    </div>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>
<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}" visibleVendors="${visibleVendors}">
    <laser:render template="iconSubscriptionIsChild"/>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<g:if test="${editable}">
    <ui:auditButton class="la-auditButton-header" auditable="[subscription, 'name']" auditConfigs="${auditConfigs}" withoutOptions="true"/>
</g:if>
<ui:anualRings object="${subscription}" controller="subscription" action="show" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<laser:render template="${customerTypeService.getNavTemplatePath()}"/>

<g:if test="${permanentTitlesProcessRunning}">
    <ui:msg class="warning" showIcon="true" hideClose="true" header="Info" message="subscription.details.permanentTitlesProcessRunning.info" />
</g:if>

<ui:objectStatus object="${subscription}" status="${subscription.status}"/>
<laser:render template="message"/>
<laser:render template="/templates/meta/identifier" model="${[object: subscription, editable: editable]}"/>

<ui:messages data="${flash}"/>
<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="eleven wide column">
        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label"><g:message code="org.altname.label" /></dt>
                            <dd>
                                <div id="altnames" class="ui divided middle aligned selection list la-flex-list accordion la-accordion-showMore">
                                    <g:if test="${subscription.altnames}">
                                        <div class="item title" id="altname_title">
                                            <div class="item" data-objId="${genericOIDService.getOID(subscription.altnames[0])}">
                                                <div class="content la-space-right">
                                                    <g:if test="${!subscription.altnames[0].instanceOf}">
                                                        <ui:xEditable owner="${subscription.altnames[0]}" field="name"/>
                                                    </g:if>
                                                    <g:else>
                                                        ${subscription.altnames[0].name}
                                                    </g:else>
                                                </div>
                                                <g:if test="${editable}">
                                                    <g:if test="${showConsortiaFunctions}">
                                                        <g:if test="${!subscription.altnames[0].instanceOf}">
                                                            <g:if test="${! AuditConfig.getConfig(subscription.altnames[0])}">
                                                                <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                               controller="ajax"
                                                                               action="toggleAlternativeNameAuditConfig"
                                                                               params='[ownerId: "${subscription.id}",
                                                                                        ownerClass: "${subscription.class}",
                                                                                        showConsortiaFunctions: true,
                                                                                        (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                               ]'
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [subscription.altnames[0].name])}"
                                                                               data-confirm-term-how="inherit"
                                                                               id="${subscription.altnames[0].id}"
                                                                               data-content="${message(code:'property.audit.off.tooltip')}"
                                                                               role="button"
                                                                >
                                                                    <i class="icon la-thumbtack slash"></i>
                                                                </ui:link>
                                                                <div class="ui buttons">
                                                                    <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                                   data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                        <i class="${Icons.CMD.DELETE}"></i>
                                                                    </ui:remoteLink>
                                                                </div>
                                                            </g:if>
                                                            <g:else>
                                                                <ui:link class="ui icon green button la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                               controller="ajax" action="toggleAlternativeNameAuditConfig"
                                                                               params='[ownerId: "${subscription.altnames[0].id}",
                                                                                        ownerClass: "${subscription.altnames[0].class}",
                                                                                        showConsortiaFunctions: true,
                                                                                        (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                               ]'
                                                                               id="${subscription.altnames[0].id}"
                                                                               data-content="${message(code:'property.audit.on.tooltip')}"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [subscription.altnames[0].name])}"
                                                                               data-confirm-term-how="inherit"
                                                                               role="button"
                                                                >
                                                                    <i class="thumbtack icon"></i>
                                                                </ui:link>
                                                            </g:else>
                                                        </g:if>
                                                        <g:else>
                                                            <div class="ui buttons">
                                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(subscription.altnames[0])}')">
                                                                    <i class="${Icons.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </div>
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${subscription.altnames[0].instanceOf}">
                                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                    </g:elseif>
                                                    <g:else>
                                                        <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                       data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                       data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(subscription.altnames[0])}')">
                                                            <i class="${Icons.CMD.DELETE}"></i>
                                                        </ui:remoteLink>
                                                    </g:else>
                                                </g:if>
                                            </div>
                                            <div class="ui icon blue button la-show-button la-modern-button la-popup-tooltip la-delay"
                                                 data-content="${message(code: 'org.altname.show')}">
                                                <i class="${Icons.CMD.SHOW_MORE}"></i>
                                            </div>
                                        </div>
                                        <div class="content">
                                            <g:each in="${subscription.altnames.drop(1)}" var="altname">
                                                <div class="ui item" data-objId="${genericOIDService.getOID(altname)}">
                                                    <div class="content la-space-right">
                                                        <g:if test="${!altname.instanceOf}">
                                                            <ui:xEditable owner="${altname}" field="name"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${altname.name}
                                                        </g:else>
                                                    </div>
                                                    <g:if test="${editable}">
                                                        <g:if test="${showConsortiaFunctions}">
                                                            <g:if test="${!altname.instanceOf}">
                                                                <g:if test="${! AuditConfig.getConfig(altname)}">
                                                                    <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                   controller="ajax"
                                                                                   action="toggleAlternativeNameAuditConfig"
                                                                                   params='[ownerId: "${subscription.id}",
                                                                                            ownerClass: "${subscription.class}",
                                                                                            showConsortiaFunctions: true,
                                                                                            (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                   ]'
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                   data-confirm-term-how="inherit"
                                                                                   id="${altname.id}"
                                                                                   data-content="${message(code:'property.audit.off.tooltip')}"
                                                                                   role="button"
                                                                    >
                                                                        <i class="icon la-thumbtack slash"></i>
                                                                    </ui:link>
                                                                    <div class="ui buttons">
                                                                        <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                                       data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                       data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                            <i class="${Icons.CMD.DELETE}"></i>
                                                                        </ui:remoteLink>
                                                                    </div>
                                                                </g:if>
                                                                <g:else>
                                                                    <ui:link class="ui icon green button la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                   controller="ajax" action="toggleAlternativeNameAuditConfig"
                                                                                   params='[ownerId: "${altname.id}",
                                                                                            ownerClass: "${altname.class}",
                                                                                            showConsortiaFunctions: true,
                                                                                            (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                   ]'
                                                                                   id="${altname.id}"
                                                                                   data-content="${message(code:'property.audit.on.tooltip')}"
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                   data-confirm-term-how="inherit"
                                                                                   role="button"
                                                                    >
                                                                        <i class="thumbtack icon"></i>
                                                                    </ui:link>
                                                                </g:else>
                                                            </g:if>
                                                            <g:else>
                                                                <div class="ui buttons">
                                                                    <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                   data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                        <i class="${Icons.CMD.DELETE}"></i>
                                                                    </ui:remoteLink>
                                                                </div>
                                                            </g:else>
                                                        </g:if>
                                                        <g:elseif test="${altname.instanceOf}">
                                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                        </g:elseif>
                                                        <g:else>
                                                            <div class="ui buttons">
                                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                    <i class="${Icons.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </div>
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${altname.instanceOf}">
                                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                    </g:elseif>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:if>
                                </div>
                            </dd>
                        </dl>
                        <g:if test="${editable}">
                            <dl>
                                <dt></dt>
                                <dd><input name="addAltname" id="addAltname" type="button" class="ui button addListValue" data-objtype="altname" value="${message(code:'org.altname.add')}"></dd>
                            </dl>
                        </g:if>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="startDate" type="date" validation="datesCheck"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'startDate']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="endDate" type="date"
                                                 validation="datesCheck" overwriteEditable="${editable && !subscription.isAutomaticRenewAnnually}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'endDate']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.manualCancellationDate.label.shy')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="manualCancellationDate" type="date"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'manualCancellationDate']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.referenceYear.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="referenceYear" type="year"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'referenceYear']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="status"
                                                     config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                     constraint="removeValue_deleted"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'status']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) ||
                                (subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                        subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isMultiYear.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isMultiYear"/></dd>
                            </dl>
                        </g:if>

                        <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isAutomaticRenewAnnually.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isAutomaticRenewAnnually"
                                                            overwriteEditable="${editable && subscription.isAllowToAutomaticRenewAnnually()}"/></dd>
                            </dl>
                        </g:if>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <sec:ifAnyGranted roles="ROLE_YODA">
                            <dl>
                                <dt class="control-label">alter Lizenztyp</dt>
                                <dd>
                                    <ui:xEditableRefData owner="${subscription}" field="type"
                                                            config="${RDConstants.SUBSCRIPTION_TYPE}"
                                                            constraint="removeValue_administrativeSubscription,removeValue_localSubscription"/>
                                </dd>
                                <dd>
                                    <ui:auditButton auditable="[subscription, 'type']" auditConfigs="${auditConfigs}"/>
                                </dd>
                            </dl>
                        </sec:ifAnyGranted>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="kind" config="${RDConstants.SUBSCRIPTION_KIND}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'kind']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="form" config="${RDConstants.SUBSCRIPTION_FORM}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'form']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="resource" config="${RDConstants.SUBSCRIPTION_RESOURCE}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                        auditable="[subscription, 'resource']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia().id}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isInstanceOfSub.label')}</dt>
                                <dd>
                                    <g:link controller="subscription" action="show" id="${subscription.instanceOf.id}">${subscription.instanceOf}</g:link>
                                </dd>
                            </dl>
                        </g:if>

                        <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isPublicForApi.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isPublicForApi"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'isPublicForApi']" auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                <%--<dd><ui:xEditableRefData owner="${subscription}" field="hasPerpetualAccess" config="${RDConstants.Y_N}" /></dd>--%>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="hasPerpetualAccess"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'hasPerpetualAccess']" auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.hasPublishComponent.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="hasPublishComponent"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'hasPublishComponent']" auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.holdingSelection.label')}</dt>
                                <dd><ui:xEditableRefData owner="${subscription}" field="holdingSelection" config="${RDConstants.SUBSCRIPTION_HOLDING}"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'holdingSelection']" auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>
                        </g:if>

                    </div>
                </div>
            </div>


            <g:if test="${subscription.packages}">
                <div id="packages" class="la-padding-top-1em"></div>
            </g:if>
            <div class="ui card" id="licenses"></div>
            <g:if test="${usage}">
                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <g:if test="${totalCostPerUse}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.details.costPerUse.header')}</dt>
                                <dd><g:formatNumber number="${totalCostPerUse}" type="currency"
                                                    currencyCode="${currencyCode}" maxFractionDigits="2"
                                                    minFractionDigits="2" roundingMode="HALF_UP"/>
                                (${message(code: 'subscription.details.costPerUse.usedMetric')}: ${costPerUseMetric})
                                </dd>
                            </dl>

                            <div class="ui divider"></div>
                        </g:if>
                        <g:if test="${lusage}">
                            <dl>
                                <dt class="control-label">${message(code: 'default.usage.licenseGrid.header')}</dt>
                                <dd>
                                    <table class="ui compact celled la-table-inCard table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'default.usage.reportType')}</th>
                                            <g:each in="${l_x_axis_labels}" var="l">
                                                <th>${l}</th>
                                            </g:each>
                                            <th></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:set var="counter" value="${0}"/>
                                        <g:each in="${lusage}" var="v">
                                            <tr>
                                                <g:set var="reportMetric" value="${l_y_axis_labels[counter++]}"/>
                                                <td>${reportMetric}
                                                </td>
                                                <g:each in="${v}" var="v2">
                                                    <td>${v2}</td>
                                                </g:each>
                                                <td>
                                                    <g:set var="missingSubMonths"
                                                           value="${missingSubscriptionMonths[reportMetric.split(':')[0]]}"/>
                                                    <g:if test="${missingSubMonths}">
                                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                              data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missingSubMonths.join(',')}">
                                                            <i class="${Icons.IMPORTANT_TOOLTIP2} la-popup small"></i>
                                                        </span>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </dd>
                            </dl>

                            <div class="ui divider"></div>
                        </g:if>
                        <dl>
                            <dt class="control-label">${message(code: 'default.usage.label')}</dt>
                            <dd>
                                <table class="ui compact celled la-table-inCard la-ignore-fixed table">
                                    <thead>
                                    <tr>
                                        <th>${message(code: 'default.usage.reportType')}
                                        </th>
                                        <g:each in="${x_axis_labels}" var="l">
                                            <th>${l}</th>
                                        </g:each>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:set var="counter" value="${0}"/>
                                    <g:each in="${usage}" var="v">
                                        <tr>
                                            <g:set var="reportMetric" value="${y_axis_labels[counter++]}"/>
                                            <td>${reportMetric}
                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                      data-html="${message(code: 'default.usage.reportUpToInfo')}: ${lastUsagePeriodForReportType[reportMetric.split(':')[0]]}">
                                                    <i class="${Icons.INFO_TOOLTIP} small la-popup"></i>
                                                </span>
                                            </td>
                                            <g:each in="${v}" status="i" var="v2">
                                                <td>
                                                    <ui:statsLink
                                                            base="${ConfigMapper.getStatsApiUrl()}"
                                                            module="statistics"
                                                            controller="default"
                                                            action="select"
                                                            target="_blank"
                                                            params="[mode        : usageMode,
                                                                     packages    : subscription.getCommaSeperatedPackagesIsilList(),
                                                                     vendors     : natStatSupplierId,
                                                                     institutions: statsWibid,
                                                                     reports     : reportMetric.split(':')[0],
                                                                     years       : x_axis_labels[i]
                                                            ]"
                                                            title="Springe zu Statistik im Nationalen Statistikserver">
                                                        ${v2}
                                                    </ui:statsLink>
                                                </td>
                                            </g:each>
                                            <g:set var="missing" value="${missingMonths[reportMetric.split(':')[0]]}"/>
                                            <td>
                                                <g:if test="${missing}">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                          data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missing.join(',')}">
                                                        <i class="${Icons.IMPORTANT_TOOLTIP2} la-popup small"></i>
                                                    </span>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </dd>
                        </dl>
                    </div>
                </div>
            </g:if>

            <div id="new-dynamic-properties-block">
                <laser:render template="properties" model="${[subscription: subscription]}"/>
            </div><!-- #new-dynamic-properties-block -->

            <div class="clear-fix"></div>
        </div>
    </div><!-- .eleven -->
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
            <div id="container-provider">
                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header">${message(code: 'provider.label')}</h2>
                        <laser:render template="/templates/links/providerLinksAsList"
                                  model="${[providerRoles: providerRoles,
                                            roleObject   : subscription,
                                            roleRespValue: 'Specific subscription editor',
                                            editmode     : editable,
                                            showPersons  : true
                                  ]}"/>

                        <div class="ui la-vertical buttons">

                            <laser:render template="/templates/links/providerLinksSimpleModal"
                                      model="${[linkType      : subscription.class.name,
                                                parent        : genericOIDService.getOID(subscription),
                                                recip_prop    : 'subscription',
                                                tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                tmplModalID   : 'modal_add_provider',
                                                editmode      : editable
                                      ]}"/>

                        </div>

                    </div>
                </div>
            </div>

            <div id="container-vendor">
                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header">${message(code: 'vendor.label')}</h2>
                        <laser:render template="/templates/links/vendorLinksAsList"
                                  model="${[vendorRoles  : vendorRoles,
                                            roleObject   : subscription,
                                            roleRespValue: 'Specific subscription editor',
                                            editmode     : editable,
                                            showPersons  : true
                                  ]}"/>

                        <div class="ui la-vertical buttons">
                            <laser:render template="/templates/links/vendorLinksSimpleModal"
                                      model="${[linkType      : subscription.class.name,
                                                parent        : genericOIDService.getOID(subscription),
                                                recip_prop    : 'subscription',
                                                tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                tmplButtonText: message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                tmplModalID   : 'modal_add_agency',
                                                editmode      : editable
                                      ]}"/>

                        </div>

                    </div>
                </div>
            </div>
            </g:if>

            <div id="container-billing">
                <g:if test="${costItemSums.ownCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${costItemSums.ownCosts}">
                                <g:if test="${(contextOrg.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                                    <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.ownCosts')}</h2>
                                    <laser:render template="financials" model="[data: costItemSums.ownCosts]"/>
                                </g:if>
                            </g:if>
                            <g:if test="${costItemSums.consCosts}">
                                <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.consCosts')}</h2>
                                <laser:render template="financials" model="[data: costItemSums.consCosts]"/>
                            </g:if>
                            <g:elseif test="${costItemSums.subscrCosts}">
                                <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.subscrCosts')}</h2>
                                <laser:render template="financials" model="[data: costItemSums.subscrCosts]"/>
                            </g:elseif>
                        </div>
                    </div>
                </g:if>
            </div>
            <div id="container-links">
                <div class="ui card" id="links"></div>
            </div>
            <laser:render template="/templates/sidebar/aside" model="${[ownobj: subscription, owntp: 'subscription']}"/>
        </div>
    </aside><!-- .four -->
</div><!-- .grid -->

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('.addListValue').click(function() {
        let url;
        let returnSelector;
        switch($(this).attr('data-objtype')) {
            case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: genericOIDService.getOID(subscription)]"/>';
                returnSelector = '#altnames';
                break;
        }

        $.ajax({
            url: url,
            success: function(result) {
                $(returnSelector).append(result);
                r2d2.initDynamicUiStuff(returnSelector);
                r2d2.initDynamicXEditableStuff(returnSelector);
            }
        });
    });
    JSPC.app.removeListValue = function(objId) {
        $("div[data-objId='"+objId+"']").remove();
    }

    JSPC.app.unlinkPackage = function(pkg_id) {
      var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage', params: [subscription: subscription.id])}&package="+pkg_id

            $.ajax({url: req_url,
              success: function(result){
                 //$("#unlinkPackageModal").clear();
                 //$('#magicArea').html(result);
              },
              complete: function(){
                $("#unlinkPackageModal").show();
              }
            });
          }
          JSPC.app.loadLinks = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getLinks"/>",
                  data: {
                      entry:"${genericOIDService.getOID(subscription)}"
                  }
              }).done(function(response){
                  $("#links").html(response);
                  r2d2.initDynamicUiStuff('#links');
              })
          }
          JSPC.app.loadLicenses = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getLinks"/>",
                  data: {
                      entry:"${genericOIDService.getOID(subscription)}",
                      subscriptionLicenseLink: true
                  }
              }).done(function(response){
                  $("#licenses").html(response);
                  r2d2.initDynamicUiStuff("#licenses");
              })
          }
          JSPC.app.loadPackages = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getPackageData"/>",
                  data: {
                      subscription: "${subscription.id}"
                  }
              }).done(function(response){
                  $("#packages").html(response);
                  r2d2.initDynamicUiStuff("#packages");
              })
          }

          JSPC.app.loadLinks();
          JSPC.app.loadLicenses();
          JSPC.app.loadPackages();
</laser:script>

<laser:htmlEnd />
