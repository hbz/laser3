<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.config.ConfigMapper; de.laser.addressbook.Person; de.laser.addressbook.PersonRole; de.laser.Subscription; de.laser.Links; java.text.SimpleDateFormat;de.laser.properties.PropertyDefinition; de.laser.OrgRole; de.laser.License;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.interfaces.CalculatedType; de.laser.FormService; de.laser.AuditConfig; de.laser.survey.SurveyConfig" %>
<laser:htmlStart message="subscription.details.label" />

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>sub.dateCreated: ${subscription.dateCreated}</p>

        <p>sub.lastUpdated: ${subscription.lastUpdated}</p>

        <p>sub.type: ${subscription.type}</p>

        <p>sub.getSubscriber(): ${subscription.getSubscriber()}</p>

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

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}"
                     visibleVendors="${visibleVendors}">
    <laser:render template="iconSubscriptionIsChild"/>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<g:if test="${editable}">
    <ui:auditButton class="la-auditButton-header" auditable="[subscription, 'name']" auditConfigs="${auditConfigs}"
                    withoutOptions="true"/>
</g:if>
<ui:anualRings object="${subscription}" controller="subscription" action="show" navNext="${navNextSubscription}"
               navPrev="${navPrevSubscription}"/>

<laser:render template="${customerTypeService.getNavTemplatePath()}"/>

<g:if test="${permanentTitlesProcessRunning}">
    <ui:msg class="warning" showIcon="true" hideClose="true" header="Info"
            message="subscription.details.permanentTitlesProcessRunning.info"/>
</g:if>

<ui:objectStatus object="${subscription}" />
<laser:render template="message"/>
<laser:render template="/templates/meta/identifier" model="${[object: subscription, editable: editable]}"/>

<ui:messages data="${flash}"/>
<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}"/>

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="eleven wide column">
        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label"><g:message code="altname.plural"/></dt>
                            <dd id="altnames" class="ui accordion la-accordion-showMore la-accordion-altName" style="padding-bottom: 0">
                                <g:if test="${subscription.altnames}">
                                    <div class="ui divided middle aligned selection list la-flex-center">
                                        <div class="item title" data-objId="altname-${subscription.altnames[0].id}" >
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
                                                            <g:if test="${!AuditConfig.getConfig(subscription.altnames[0])}">
                                                                <ui:link class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                                         controller="ajax"
                                                                         action="toggleAlternativeNameAuditConfig"
                                                                         params='[ownerId                         : "${subscription.id}",
                                                                                  ownerClass                      : "${subscription.class}",
                                                                                  showConsortiaFunctions          : true,
                                                                                  (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                         ]'
                                                                         data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [subscription.altnames[0].name])}"
                                                                         data-confirm-term-how="inherit"
                                                                         id="${subscription.altnames[0].id}"
                                                                         data-content="${message(code: 'property.audit.off.tooltip')}"
                                                                         role="button">
                                                                    <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                                                </ui:link>
                                                                <ui:remoteLink role="button"
                                                                               class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                               controller="ajaxJson" action="removeObject"
                                                                               params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                               data-confirm-term-how="delete"
                                                                               data-done="JSPC.app.removeListValue('altname-${subscription.altnames[0].id}')">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </g:if>
                                                            <g:else>
                                                                <ui:link class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                                 controller="ajax"
                                                                 action="toggleAlternativeNameAuditConfig"
                                                                 params='[ownerId                         : "${subscription.altnames[0].id}",
                                                                          ownerClass                      : "${subscription.altnames[0].class}",
                                                                          showConsortiaFunctions          : true,
                                                                          (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                 ]'
                                                                 id="${subscription.altnames[0].id}"
                                                                 data-content="${message(code: 'property.audit.on.tooltip')}"
                                                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [subscription.altnames[0].name])}"
                                                                 data-confirm-term-how="inherit"
                                                                 role="button">
                                                            <i class="${Icon.SIG.INHERITANCE}"></i>
                                                        </ui:link>
                                                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                    <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                </div>
                                                            </g:else>
                                                        </g:if>
                                                        <g:else>
                                                            <ui:remoteLink role="button"
                                                                           class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                           controller="ajaxJson" action="removeObject"
                                                                           params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                           data-confirm-term-how="delete"
                                                                           data-done="JSPC.app.removeListValue('altname-${subscription.altnames[0].id}')">
                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                            </ui:remoteLink>
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${subscription.altnames[0].instanceOf}">
                                                        <ui:auditIcon type="auto"/>
                                                    </g:elseif>
                                                    <g:else>
                                                        <ui:remoteLink role="button" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                       controller="ajaxJson" action="removeObject"
                                                                       params="[object: 'altname', objId: subscription.altnames[0].id]"
                                                                       data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [subscription.altnames[0].name])}"
                                                                       data-confirm-term-how="delete"
                                                                       data-done="JSPC.app.removeListValue('altname-${subscription.altnames[0].id}')">
                                                            <i class="${Icon.CMD.DELETE}"></i>
                                                        </ui:remoteLink>
                                                    </g:else>
                                                </g:if>
                                            <div class="${Btn.MODERN.SIMPLE_TOOLTIP} la-show-button"
                                                 data-content="${message(code: 'altname.showAll')}">
                                                <i class="${Icon.CMD.SHOW_MORE}"></i>
                                            </div>
                                        </div>

                                        <div  class="content" style="padding:0">
                                            <g:each in="${subscription.altnames.drop(1)}" var="altname">
                                                <div class="ui item" data-objId="altname-${altname.id}">
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
                                                                <g:if test="${!AuditConfig.getConfig(altname)}">
                                                                    <ui:link
                                                                        class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                                        controller="ajax"
                                                                        action="toggleAlternativeNameAuditConfig"
                                                                        params='[ownerId                         : "${subscription.id}",
                                                                                 ownerClass                      : "${subscription.class}",
                                                                                 showConsortiaFunctions          : true,
                                                                                 (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                        ]'
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                        data-confirm-term-how="inherit"
                                                                        id="${altname.id}"
                                                                        data-content="${message(code: 'property.audit.off.tooltip')}"
                                                                        role="button">
                                                                        <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                                                    </ui:link>
                                                                    <ui:remoteLink role="button"
                                                                                   class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                                   controller="ajaxJson"
                                                                                   action="removeObject"
                                                                                   params="[object: 'altname', objId: altname.id]"
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                   data-confirm-term-how="delete"
                                                                                   data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                        <i class="${Icon.CMD.DELETE}"></i>
                                                                    </ui:remoteLink>
                                                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                        <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                    </div>
                                                                </g:if>
                                                                <g:else>
                                                                    <ui:link
                                                                        class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                                        controller="ajax"
                                                                        action="toggleAlternativeNameAuditConfig"
                                                                        params='[ownerId                         : "${altname.id}",
                                                                                 ownerClass                      : "${altname.class}",
                                                                                 showConsortiaFunctions          : true,
                                                                                 (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                        ]'
                                                                        id="${altname.id}"
                                                                        data-content="${message(code: 'property.audit.on.tooltip')}"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                        data-confirm-term-how="inherit"
                                                                        role="button">
                                                                        <i class="${Icon.SIG.INHERITANCE}"></i>
                                                                    </ui:link>
                                                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                        <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                    </div>

                                                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                        <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                    </div>
                                                                </g:else>
                                                            </g:if>
                                                            <g:else>
                                                                <ui:remoteLink role="button"
                                                                               class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                               controller="ajaxJson"
                                                                               action="removeObject"
                                                                               params="[object: 'altname', objId: altname.id]"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                               data-confirm-term-how="delete"
                                                                               data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </g:else>
                                                        </g:if>
                                                        <g:elseif test="${altname.instanceOf}">
                                                            <ui:auditIcon type="auto"/>
                                                        </g:elseif>
                                                        <g:else>
                                                            <ui:remoteLink role="button"
                                                                           class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                           controller="ajaxJson" action="removeObject"
                                                                           params="[object: 'altname', objId: altname.id]"
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                           data-confirm-term-how="delete"
                                                                           data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                            </ui:remoteLink>
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${altname.instanceOf}">
                                                        <ui:auditIcon type="auto"/>
                                                    </g:elseif>
                                                </div>
                                            </g:each>
                                        </div>
                                    </div>
                                </g:if>
                            </dd>
                            <dd>
                                <g:if test="${editable}">
                                    <button  data-content="${message(code: 'altname.add')}" data-objtype="altname" id="addAltname"  class="${Btn.MODERN.POSITIVE} la-js-addItem blue la-popup-tooltip">
                                        <i class="${Icon.CMD.ADD}"></i>
                                    </button>
                                </g:if>
                            </dd>
                        </dl>
                        <div></div>%{-- Breaks DL for a reason --}%
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="startDate" type="date"
                                              validation="datesCheck"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                    auditable="[subscription, 'startDate']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="endDate" type="date"
                                              validation="datesCheck"
                                              overwriteEditable="${editable && !subscription.isAutomaticRenewAnnually}"/></dd>
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
                            <dd><ui:xEditableRefData owner="${subscription}" field="kind"
                                                     config="${RDConstants.SUBSCRIPTION_KIND}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                    auditable="[subscription, 'kind']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="form"
                                                     config="${RDConstants.SUBSCRIPTION_FORM}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                    auditable="[subscription, 'form']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="resource"
                                                     config="${RDConstants.SUBSCRIPTION_RESOURCE}"/></dd>
                            <g:if test="${editable}">
                                <dd><ui:auditButton
                                    auditable="[subscription, 'resource']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortium().id}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isInstanceOfSub.label')}</dt>
                                <dd>
                                    <g:link controller="subscription" action="show"
                                            id="${subscription.instanceOf.id}">${subscription.instanceOf}</g:link>
                                </dd>
                            </dl>
                        </g:if>

                        <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isPublicForApi.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isPublicForApi"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'isPublicForApi']"
                                                        auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                <%--<dd><ui:xEditableRefData owner="${subscription}" field="hasPerpetualAccess" config="${RDConstants.Y_N}" /></dd>--%>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="hasPerpetualAccess"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'hasPerpetualAccess']"
                                                        auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.hasPublishComponent.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="hasPublishComponent"/></dd>
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'hasPublishComponent']"
                                                        auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.holdingSelection.label')}</dt>
                                <dd>
                                    <g:if test="${showConsortiaFunctions}">
                                        <ui:xEditableRefData owner="${subscription}" field="holdingSelection" id="holdingSelection"
                                                             data_confirm_term_how="ok"
                                                             class="js-open-confirm-modal-xEditableRefData"
                                                             config="${RDConstants.SUBSCRIPTION_HOLDING}"
                                                             overwriteEditable="${editable && !AuditConfig.getConfig(subscription.instanceOf, 'holdingSelection') && (!SurveyConfig.findBySubscriptionAndType(subscription, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) && !SurveyConfig.findBySubscriptionAndType(subscription.instanceOf, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT))}"/>
                                    </g:if>
                                    <g:else>
                                        <ui:xEditableRefData owner="${subscription}" field="holdingSelection"
                                                             config="${RDConstants.SUBSCRIPTION_HOLDING}" overwriteEditable="${editable && !AuditConfig.getConfig(subscription.instanceOf, 'holdingSelection') && (!SurveyConfig.findBySubscriptionAndType(subscription, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) && !SurveyConfig.findBySubscriptionAndType(subscription.instanceOf, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT))}"/>
                                    </g:else>
                                </dd>
                                <%-- not needed because inheritance is defined implicitly by value
                                <g:if test="${editable}">
                                    <dd>
                                        <ui:auditButton auditable="[subscription, 'holdingSelection']"
                                                        auditConfigs="${auditConfigs}"/>
                                    </dd>
                                </g:if>
                                --%>
                            <%-- Extra Call from editable cause validation needed only in Case of Selection "Ja" --%>
                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('#holdingSelection').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown clearable"></select>'
                                    }).on('shown', function() {
                                        r2d2.initDynamicUiStuff('body');
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown').dropdown({
                                            clearable: true,
                                            onChange: function(value, text, $selectedItem) {

                                            if ( value =="${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id}" ) {
                                                console.log("alle");
                                                $('#holdingSelection').attr( "data_confirm_value", "${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id}" )
                                                                        .attr( "data_confirm_tokenMsg", "alle" );
                                                                        r2d2.initDynamicUiStuff('#holdingSelection');
                                            }
                                            else if ( value == "${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_PARTIAL.id}" ) {
                                                console.log("Einel");
                                                $('#holdingSelection').attr( "data_confirm_value", "${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_PARTIAL.id}" )
                                                                        .attr( "data_confirm_tokenMsg", "einzel" );
                                                                        r2d2.initDynamicUiStuff('#holdingSelection');
                                            }
                                    r2d2.initDynamicUiStuff('#holdingSelection');


%{--                                                if(value == ${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_PARTIAL.id}){
                                                    console.log("Einzel")
                                                }
                                                if(value == ${RefdataValue.class.name}:${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id}) {
                                                    console.log("Alle")
                                                }--}%
                                            }
                                        });
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });
                                </laser:script>
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
                                                        <span class="la-long-tooltip la-popup-tooltip"
                                                              data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missingSubMonths.join(',')}">
                                                            <i class="${Icon.TOOLTIP.IMPORTANT} la-popup small"></i>
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
                                                <span class="la-long-tooltip la-popup-tooltip"
                                                      data-html="${message(code: 'default.usage.reportUpToInfo')}: ${lastUsagePeriodForReportType[reportMetric.split(':')[0]]}">
                                                    <i class="${Icon.TOOLTIP.INFO} small la-popup"></i>
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
                                                    <span class="la-long-tooltip la-popup-tooltip"
                                                          data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missing.join(',')}">
                                                        <i class="${Icon.TOOLTIP.IMPORTANT} la-popup small"></i>
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

            <g:if test="${subscription.instanceOf && !contextService.getOrg().isCustomerType_Support()}">
                <div id="container-consortium">
                    <div class="ui card">
                        <div class="content">
                            <h2 class="ui header">${message(code: 'consortium.label')}</h2>
                            <laser:render template="/templates/links/consortiumLinksAsList"
                                          model="${[consortium   : subscription.getConsortium(),
                                                    roleObject   : subscription,
                                                    roleRespValue: 'Specific subscription editor'
                                          ]}"/>
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                <div id="container-provider">
                    <div class="ui card">
                        <div class="content">
                            <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                                <h2>${message(code: 'provider.label')}</h2>
                                <laser:render template="/templates/links/providerLinksSimpleModal"
                                              model="${[linkType      : subscription.class.name,
                                                        parent        : genericOIDService.getOID(subscription),
                                                        recip_prop    : 'subscription',
                                                        tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                        tmplIcon        : 'add',
                                                        tmplTooltip     : message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_provider',
                                                        editmode      : editable,
                                                        tmplCss       : ''
                                              ]}"/>
                            </div>
                            <laser:render template="/templates/links/providerLinksAsList"
                                          model="${[providerRoles: providerRoles,
                                                    roleObject   : subscription,
                                                    roleRespValue: RDStore.PRS_RESP_SPEC_SUB_EDITOR.value,
                                                    editmode     : editable,
                                                    showPersons  : true
                                          ]}"/>

                        </div>
                    </div>
                </div>

                <div id="container-vendor">
                    <div class="ui card">
                        <div class="content">
                            <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                                <h2>${message(code: 'vendor.label')}</h2>
                                <laser:render template="/templates/links/vendorLinksSimpleModal"
                                              model="${[linkType      : subscription.class.name,
                                                        parent        : genericOIDService.getOID(subscription),
                                                        recip_prop    : 'subscription',
                                                        tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                        tmplIcon      : 'add',
                                                        tmplTooltip   : message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_agency',
                                                        editmode      : editable,
                                                        tmplCss       : ''

                                              ]}"/>
                            </div>
                            <laser:render template="/templates/links/vendorLinksAsList"
                                          model="${[vendorRoles  : vendorRoles,
                                                    roleObject   : subscription,
                                                    roleRespValue: RDStore.PRS_RESP_SPEC_SUB_EDITOR.value,
                                                    editmode     : editable,
                                                    showPersons  : true,
                                                    tmplCss      : ''
                                          ]}"/>
                        </div>
                    </div>
                </div>
            </g:if>

            <div id="container-billing">
                <g:if test="${costItemSums.ownCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${costItemSums.ownCosts}">
                                <g:if
                                    test="${(contextService.getOrg().id != subscription.getConsortium()?.id && subscription.instanceOf) || !subscription.instanceOf}">
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

    $('.la-js-addItem').click(function() {
        let url;
        let returnSelector;
        switch($(this).attr('data-objtype')) {
            case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject"
                                                 params="[object: 'altname', owner: genericOIDService.getOID(subscription)]"/>';
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

<laser:htmlEnd/>
