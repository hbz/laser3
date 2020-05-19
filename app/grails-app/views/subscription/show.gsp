<%@ page import="com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole; java.math.MathContext; com.k_int.kbplus.Subscription; com.k_int.kbplus.Links; java.text.SimpleDateFormat" %>
<%@ page import="com.k_int.properties.PropertyDefinition; com.k_int.kbplus.OrgRole" %>
<%@ page import="com.k_int.kbplus.RefdataCategory;de.laser.helper.RDStore;de.laser.helper.RDConstants;de.laser.interfaces.CalculatedType" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection />
<%-- r:require module="annotations" / --%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'subscription.details.label')}</title>
        <g:javascript src="properties.js"/>
    </head>
    <body>

        <semui:debugInfo>
            <div style="padding: 1em 0;">
                <p>sub.instanceOf: <g:if test="${subscriptionInstance.instanceOf}"> <g:link action="show" id="${subscriptionInstance. instanceOf.id}">${subscriptionInstance.instanceOf.name}</g:link>
                    ${subscriptionInstance.instanceOf.getAllocationTerm()}
                </g:if> </p>
                <p>sub.administrative: ${subscriptionInstance.administrative}</p>
                <p>getCalculatedType(): ${subscriptionInstance.getCalculatedType()}</p>
            </div>
            <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
            <g:render template="/templates/debug/orgRoles"  model="[debug: subscriptionInstance.orgRelations]" />
            <g:render template="/templates/debug/prsRoles"  model="[debug: subscriptionInstance.prsLinks]" />
        </semui:debugInfo>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

%{--        <g:if test="${params.asAt}">
            <h1 class="ui icon header"><semui:headerIcon />${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])}</h1>
        </g:if>--}%


        <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
            <semui:xEditable owner="${subscriptionInstance}" field="name" />
        </h1>
        <semui:auditButton auditable="[subscriptionInstance, 'name']" />

        <semui:anualRings object="${subscriptionInstance}" controller="subscription" action="show" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" />

    <semui:objectStatus object="${subscriptionInstance}" status="${subscriptionInstance.status}" />

    <g:render template="message" />

    <g:render template="/templates/meta/identifier" model="${[object: subscriptionInstance, editable: editable]}" />

        <semui:messages data="${flash}" />

        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>


    <div id="collapseableSubDetails" class="ui stackable grid">
        <div class="twelve wide column">

            <div class="la-inline-lists">
                <div class="ui two stackable cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'startDate']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'endDate']"/></dd>
                            </dl>
                            <% /*
                            <dl>
                                <dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd>
                            </dl>
                            */ %>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.manualCancellationDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualCancellationDate" type="date"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'manualCancellationDate']" /></dd>
                            </dl>

                            <g:if test="${(subscriptionInstance.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                    subscriptionInstance.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) ||
                                    (subscriptionInstance.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                    subscriptionInstance.getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.isMultiYear.label')}</dt>
                                    <dd><semui:xEditableBoolean owner="${subscriptionInstance}" field="isMultiYear" /></dd>
                                </dl>
                            </g:if>

                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code: 'default.status.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="status" config="${RDConstants.SUBSCRIPTION_STATUS}" constraint="removeValue_deleted" /></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'status']"/></dd>
                            </dl>
                            <sec:ifAnyGranted roles="ROLE_YODA">
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.type.label')}</dt>
                                    <dd>
                                        %{--
                                        <%
                                            //does not work for some reason, proceed to IDs
                                            Set<Long> subscriberIDs = []
                                            subscriptionInstance.getAllSubscribers().each { subscriber ->
                                                subscriberIDs << subscriber.id
                                            }
                                        %>
                                        <g:if test="${subscriptionInstance.administrative || subscriberIDs.contains(contextOrg?.id)}">
                                            ${subscriptionInstance.type?.getI10n('value')}
                                        </g:if>
                                        <g:else>
                                            <semui:xEditableRefData owner="${subscriptionInstance}" field="type"
                                                                    config="${RDConstants.SUBSCRIPTION_TYPE}"
                                                                    constraint="removeValue_administrativeSubscription,removeValue_localSubscription"
                                            />
                                        </g:else>--}%
                                        ${subscriptionInstance.type?.getI10n('value')}
                                    </dd>
                                    <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'type']"/></dd>
                                </dl>
                            </sec:ifAnyGranted>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="kind" config="${RDConstants.SUBSCRIPTION_KIND}"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'kind']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="form" config="${RDConstants.SUBSCRIPTION_FORM}"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'form']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="resource" config="${RDConstants.SUBSCRIPTION_RESOURCE}"/></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'resource']"/></dd>
                            </dl>
                            <g:if test="${!params.orgBasicMemberView && subscriptionInstance.instanceOf && (contextOrg?.id in [subscriptionInstance.getConsortia()?.id,subscriptionInstance.getCollective()?.id])}">
                                <dl>
                                    <dt class="control-label">${message(code:'subscription.isInstanceOfSub.label')}</dt>
                                    <dd>
                                        <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">${subscriptionInstance.instanceOf}</g:link>
                                    </dd>
                                </dl>

                                <dl>
                                    <dt class="control-label">
                                        ${message(code:'license.details.linktoLicense.pendingChange')}
                                    </dt>
                                    <dd>
                                        <semui:xEditableBoolean owner="${subscriptionInstance}" field="isSlaved" />
                                    </dd>
                                </dl>
                            </g:if>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isPublicForApi.label')}</dt>
                                <dd><semui:xEditableBoolean owner="${subscriptionInstance}" field="isPublicForApi" /></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'isPublicForApi']"/></dd>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                <dd><semui:xEditableBoolean owner="${subscriptionInstance}" field="hasPerpetualAccess" /></dd>
                                <dd class="la-js-editmode-container"><semui:auditButton auditable="[subscriptionInstance, 'hasPerpetualAccess']"/></dd>
                            </dl>

                        </div>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <h5 class="ui header">
                           <g:message code="subscription.details.linksHeader"/>
                        </h5>
                        <g:if test="${links.entrySet()}">
                            <table class="ui three column table">
                                <g:each in="${links.entrySet().toSorted()}" var="linkTypes">
                                    <g:if test="${linkTypes.getValue().size() > 0}">
                                        <g:each in="${linkTypes.getValue()}" var="link">
                                            <tr>
                                                <th scope="row" class="control-label la-js-dont-hide-this-card">${linkTypes.getKey()}</th>
                                                <td>
                                                    <g:set var="pair" value="${link.getOther(subscriptionInstance)}"/>
                                                    <g:set var="sdf" value="${new SimpleDateFormat('dd.MM.yyyy')}"/>
                                                    <g:link controller="subscription" action="show" id="${pair.id}">
                                                        ${pair.name}
                                                    </g:link><br>
                                                    ${pair.startDate ? sdf.format(pair.startDate) : ""}–${pair.endDate ? sdf.format(pair.endDate) : ""}
                                                </td>
                                                <td class="right aligned">
                                                    <g:render template="/templates/links/subLinksModal"
                                                              model="${[tmplText:message(code:'subscription.details.editLink'),
                                                                        tmplIcon:'write',
                                                                        tmplCss: 'icon la-selectable-button',
                                                                        tmplID:'editLink',
                                                                        tmplModalID:"sub_edit_link_${link.id}",
                                                                        editmode: editable,
                                                                        context: "${subscriptionInstance.class.name}:${subscriptionInstance.id}",
                                                                        link: link
                                                              ]}" />
                                                    <g:if test="${editable}">
                                                        <g:link class="ui negative icon button la-selectable-button js-open-confirm-modal"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                                data-confirm-term-how="unlink"
                                                                controller="ajax" action="delete" params='[cmd: "deleteLink", oid: "${link.class.name}:${link.id}"]'>
                                                            <i class="unlink icon"></i>
                                                        </g:link>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                    </g:if>
                                </g:each>
                            </table>
                        </g:if>
                        <g:else>
                            <p>
                                <g:message code="subscription.details.noLink"/>
                            </p>
                        </g:else>
                        <div class="ui la-vertical buttons">
                            <g:render template="/templates/links/subLinksModal"
                                      model="${[tmplText:message(code:'subscription.details.addLink'),
                                                tmplID:'addLink',
                                                tmplButtonText:message(code:'subscription.details.addLink'),
                                                tmplModalID:'sub_add_link',
                                                editmode: editable,
                                                context: "${subscriptionInstance.class.name}:${subscriptionInstance.id}"
                                      ]}" />
                        </div>
                    </div>
                </div>

                %{--<div class="ui card">
                    <div class="content">

                            <table class="ui three column table">
                                <tbody>
                                <g:if test="${publicSubscriptionEditors}"></g:if>
                                <g:else>
                                    <dl>
                                        <dt class="control-label"><g:message code="license.responsibilites" />
                                        </dt>
                                    </dl>
                                </g:else>
                                <g:each in="${publicSubscriptionEditors}" var="pse">
                                        <tr>
                                            <th scope="row" class="control-label la-js-dont-hide-this-card">
                                                <g:message code="license.responsibilite" />
                                            </th>
                                            <td>
                                                <g:render template="/templates/cpa/person_full_details" model="${[
                                                        person              : pse,
                                                        personContext       : pse.tenant,
                                                        tmplShowDeleteButton    : true,
                                                        tmplShowAddPersonRoles  : false,
                                                        tmplShowAddContacts     : true,
                                                        tmplShowAddAddresses    : true,
                                                        tmplShowFunctions       : false,
                                                        tmplShowPositions       : false,
                                                        tmplShowResponsiblities : false,
                                                        tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                                                        tmplUnlinkedObj     : PersonRole.findByPrsAndOrgAndSubAndResponsibilityType(pse, pse.tenant, subscriptionInstance, RDStore.PRS_RESP_SPEC_SUB_EDITOR),
                                                        controller          : 'subscription',
                                                        action              : 'show',
                                                        id                  : pse.tenant.id,
                                                        editable            : ((pse.tenant.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                                ]}"/>
                                            </td>
                                        </tr>
                                    </g:each>

                                </tbody>
                            </table>

                            <g:if test="${OrgRole.findAllByOrg(contextOrg)}">
                                <div class="ui la-vertical buttons">
                                    <a role="button" class="ui button" data-semui="modal" href="#prsLinksModal">
                                        ${message(code: 'default.add.label', args: [message(code: 'person.label')])}
                                    </a>
                                </div>
                                <g:render template="/templates/links/prsResponsibilityModal"
                                          model="[
                                                  parent: subscriptionInstance,
                                                  modalVisiblePersons: contextOrg.getPublicPersons().minus(publicSubscriptionEditors),
                                                  org: contextOrg,
                                                  role: modalPrsLinkRole
                                          ]"/>
                            </g:if>
                    </div>
                </div>--}%
              <g:if test="${subscriptionInstance.packages}">
                <div class="ui card la-js-hideable hidden">
                  <div class="content">
                      <g:render template="accessPointLinksAsList"
                                model="${[roleLinks: visibleOrgRelations,
                                          roleObject: subscriptionInstance,
                                          roleRespValue: 'Specific subscription editor',
                                          editmode: editable,
                                          accessConfigEditable : accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && subscriptionInstance.getSubscriber().id == contextOrg.id)
                                ]}" />
                  </div><!-- .content -->
                </div>

                  <g:if test="${editable}">
                      <div class="ui card la-js-hideable hidden">
                          <div class="ui segment accordion">
                              <div class="ui title header">
                                  <i class="dropdown icon la-dropdown-accordion"></i><g:message code="subscription.packages.config.header" />
                              </div>
                              <div class="content">
                                  <g:each in="${subscriptionInstance.packages}" var="subscriptionPackage">
                                      <h5 class="ui header">
                                          <g:message code="subscription.packages.config.label" args="${[subscriptionPackage.pkg.name]}"/>
                                      </h5>
                                      <g:form action="setupPendingChangeConfiguration" params="[id:subscriptionInstance.id,subscriptionPackage:subscriptionPackage.id]">
                                          <dl>
                                              <dt class="control-label"><g:message code="subscription.packages.changeType.label"/></dt>
                                              <dt class="control-label">
                                                  <g:message code="subscription.packages.setting.label"/>
                                              </dt>
                                              <dt class="control-label">
                                                  <g:message code="subscription.packages.notification.label"/>
                                              </dt>
                                              <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR')}">
                                                  <dt class="control-label">
                                                      <i class="ui large icon thumbtack"></i>
                                                  </dt>
                                              </g:if>
                                          </dl>
                                          <g:set var="excludes" value="${[de.laser.domain.PendingChangeConfiguration.PACKAGE_PROP,de.laser.domain.PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                                          <g:each in="${de.laser.domain.PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                                              <dl>
                                                  <dt class="control-label">
                                                      <g:message code="subscription.packages.${settingKey}"/>
                                                  </dt>
                                                  <dd>
                                                      <g:if test="${!(settingKey in excludes)}">
                                                          <laser:select class="ui dropdown"
                                                                        name="${settingKey}!§!setting" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                                        optionKey="id" optionValue="value"
                                                                        value="${subscriptionPackage.getPendingChangeConfig(settingKey) ? subscriptionPackage.getPendingChangeConfig(settingKey).settingValue.id : RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"
                                                          />
                                                      </g:if>
                                                  </dd>
                                                  <dd>
                                                      <g:checkBox class="ui checkbox" name="${settingKey}!§!notification" checked="${subscriptionPackage.getPendingChangeConfig(settingKey)?.withNotification}"/>
                                                  </dd>
                                                  <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR')}">
                                                      <dd>
                                                          <g:if test="${!(settingKey in excludes)}">
                                                              <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable" checked="${subscriptionPackage.getPendingChangeConfig(settingKey) ? auditService.getAuditConfig(subscriptionInstance,settingKey) : false}"/>
                                                          </g:if>
                                                      </dd>
                                                  </g:if>
                                              </dl>
                                          </g:each>
                                          <dl>
                                              <dt class="control-label"><g:submitButton class="ui button btn-primary" name="${message(code:'subscription.packages.submit.label')}"/></dt>
                                          </dl>
                                      </g:form>
                                  </g:each>

                              </div><!-- .content -->
                          </div>
                      </div>
                  </g:if>
              </g:if>

                <div class="ui card la-js-hideable hidden">
                    <div class="content">
                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgRelations,
                                            roleObject: subscriptionInstance,
                                            roleRespValue: 'Specific subscription editor',
                                            editmode: editable,
                                            showPersons: true
                                  ]}" />

                        <div class="ui la-vertical buttons la-js-hide-this-card">

                            <g:render template="/templates/links/orgLinksSimpleModal"
                                      model="${[linkType: subscriptionInstance?.class?.name,
                                                parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id,
                                                property: 'orgs',
                                                recip_prop: 'sub',
                                                tmplRole: RDStore.OR_PROVIDER,
                                                tmplEntity:message(code:'subscription.details.linkProvider.tmplEntity'),
                                                tmplText:message(code:'subscription.details.linkProvider.tmplText'),
                                                tmplButtonText:message(code:'subscription.details.linkProvider.tmplButtonText'),
                                                tmplModalID:'modal_add_provider',
                                                editmode: editable,
                                                orgList: availableProviderList,
                                                signedIdList: existingProviderIdList
                                      ]}" />

                            <g:render template="/templates/links/orgLinksSimpleModal"
                                      model="${[linkType: subscriptionInstance?.class?.name,
                                                parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id,
                                                property: 'orgs',
                                                recip_prop: 'sub',
                                                tmplRole: RDStore.OR_AGENCY,
                                                tmplEntity: message(code:'subscription.details.linkAgency.tmplEntity'),
                                                tmplText: message(code:'subscription.details.linkAgency.tmplText'),
                                                tmplButtonText: message(code:'subscription.details.linkAgency.tmplButtonText'),
                                                tmplModalID:'modal_add_agency',
                                                editmode: editable,
                                                orgList: availableAgencyList,
                                                signedIdList: existingAgencyIdList
                                      ]}" />

                        </div><!-- la-js-hide-this-card -->

                    <% /*
               <dl>
                    <dt><label class="control-label" for="licenseeRef">${message(code:'org.links.label', default:'Org Links')}</label></dt><dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:visibleOrgRelations,editmode:editable]}" />
                    </dd>
               </dl>
               */ %>

                    <% /*g:if test="${params.mode=='advanced'}">
                 <dl><dt><label class="control-label" for="licenseeRef">${message(code:'default.status.label', default:'Status')}</label></dt><dd>
                      <semui:xEditableRefData owner="${subscriptionInstance}" field="status" config="${RDConstants.SUBSCRIPTION_STATUS}" />
                     </dd>
               </dl>
               </g:if */ %>

                    </div>
                </div>

                <div class="ui card la-js-hideable hidden">
                    <div class="content">

                        <table class="ui three column table">
                            <tr>
                                <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code:'license.label')}</th>
                                <td>
                                    <g:if test="${subscriptionInstance.owner == null}">
                                        <semui:xEditableRefData owner="${subscriptionInstance}" field="owner" dataController="subscription" dataAction="possibleLicensesForSubscription" />
                                    </g:if>
                                    <g:else>
                                        <g:link controller="license" action="show" id="${subscriptionInstance.owner.id}">
                                            ${subscriptionInstance.owner}
                                        </g:link>
                                    </g:else>
                                    %{-- <g:if test="${subscriptionInstance.owner != null}">
                                         [<g:link controller="license" action="show" id="${subscriptionInstance.owner.id}">
                                             <i class="icon-share-alt"></i> ${message(code:'default.button.show.label', default:'Show')}
                                         </g:link>]
                                     </g:if>--}%
                                </td>
                                <td class="right aligned">
                                    <g:if test="${editable && subscriptionInstance.owner}">
                                        <div class="ui icon negative buttons">
                                            <a href="?cmd=unlinkLicense" class="ui button la-selectable-button">
                                                <i class="unlink icon"></i>
                                            </a>
                                        </div>
                                        <br />
                                    </g:if>
                                </td>
                            </tr>
                        </table>

                        <g:if test="${editable}">
                            <g:if test="${subscriptionInstance.owner == null}">
                                <g:link controller="myInstitution" class="ui button la-new-item" action="emptyLicense" params="[sub: subscriptionInstance.id]">${message(code:'license.add.blank')}</g:link>
                            </g:if>
                        </g:if>

                        <g:set var="derivedPropDefGroups" value="${subscriptionInstance.owner?.getCalculatedPropDefGroups(contextService.getOrg())}" />

                        <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.orphanedProperties}">
                            <div class="ui la-vertical buttons">
                                <button id="derived-license-properties-toggle" class="ui button la-js-dont-hide-button">Vertragsmerkmale anzeigen</button>
                                <script>
                                    $('#derived-license-properties-toggle').on('click', function() {
                                        $('#derived-license-properties').toggleClass('hidden')
                                        if ($('#derived-license-properties').hasClass('hidden')) {
                                            $(this).text('Vertragsmerkmale anzeigen')
                                        } else {
                                            $(this).text('Vertragsmerkmale ausblenden')
                                        }
                                    })
                                </script>
                            </div>
                        </g:if>

                    </div><!-- .content -->
                </div>

                <g:if test="${derivedPropDefGroups?.global || derivedPropDefGroups?.local || derivedPropDefGroups?.member || derivedPropDefGroups?.orphanedProperties}">
                    <div id="derived-license-properties" class="hidden" style="margin: 1em 0">

                        <g:render template="licProp" model="${[
                                license: subscriptionInstance.owner,
                                derivedPropDefGroups: derivedPropDefGroups
                        ]}" />
                    </div>
                </g:if>

                <%-- FINANCE, to be reactivated as of ERMS-943 --%>
                <%-- assemble data on server side --%>
                <g:if test="${costItemSums.ownCosts || costItemSums.collCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${costItemSums.ownCosts}">
                                <g:if test="${(!(contextOrg.id in [subscription.getConsortia()?.id,subscription.getCollective()?.id]) && subscription.instanceOf) || !subscription.instanceOf}">
                                    <h5 class="ui header">${message(code:'financials.label')} : ${message(code:'financials.tab.ownCosts')}</h5>
                                    <g:render template="financials" model="[data:costItemSums.ownCosts]"/>
                                </g:if>
                            </g:if>
                            <g:if test="${costItemSums.consCosts}">
                                <h5 class="ui header">${message(code:'financials.label')} : ${message(code:'financials.tab.consCosts')}</h5>
                                <g:render template="financials" model="[data:costItemSums.consCosts]"/>
                            </g:if>
                            <g:elseif test="${costItemSums.collCosts}">
                                <h5 class="ui header">${message(code:'financials.label')} : ${message(code:'financials.tab.collCosts')}</h5>
                                <g:render template="financials" model="[data:costItemSums.collCosts]"/>
                            </g:elseif>
                            <g:elseif test="${costItemSums.subscrCosts}">
                                <h5 class="ui header">${message(code:'financials.label')} : ${message(code:'financials.tab.subscrCosts')}</h5>
                                <g:render template="financials" model="[data:costItemSums.subscrCosts]"/>
                            </g:elseif>
                        </div>
                    </div>
                </g:if>
                <g:if test="${usage}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${totalCostPerUse}">
                                <dl>
                                    <dt class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.details.costPerUse.header')}</dt>
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
                                    <table class="ui la-table-small celled la-table-inCard table">
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
                                                <g:set var="reportMetric" value="${l_y_axis_labels[counter++]}" />
                                                <td>${reportMetric}</td>
                                                <g:each in="${v}" var="v2">
                                                    <td>${v2}</td>
                                                </g:each>
                                                <td>
                                                    <g:set var="missingSubMonths"
                                                           value="${missingSubscriptionMonths[reportMetric.split(':')[0]]}"/>
                                                    <g:if test="${missingSubMonths}">
                                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                              data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missingSubMonths.join(',')}">
                                                            <i class="exclamation triangle icon la-popup small"></i>
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
                                <dt class="control-label la-js-dont-hide-this-card">${message(code: 'default.usage.label')}</dt>
                                <dd>
                                    <table class="ui la-table-small celled la-table-inCard table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'default.usage.reportType')}</th>
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
                                                <g:set var="reportMetric" value="${y_axis_labels[counter++]}" />
                                                <td>${reportMetric}</td>
                                                <g:each in="${v}" status="i" var="v2">
                                                    <td>
                                                        <laser:statsLink
                                                            base="${grailsApplication.config.statsApiUrl}"
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
                                                        </laser:statsLink>
                                                    </td>
                                                </g:each>
                                                <g:set var="missing" value="${missingMonths[reportMetric.split(':')[0]]}"/>
                                                <td>
                                                    <g:if test="${missing}">
                                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                              data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missing.join(',')}">
                                                            <i class="exclamation triangle icon la-popup small"></i>
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

                    <g:render template="properties" model="${[
                            subscriptionInstance: subscriptionInstance,
                            authorizedOrgs: authorizedOrgs
                    ]}" />

                </div><!-- #new-dynamic-properties-block -->

               <div class="clear-fix"></div>
            </div>
        </div><!-- .twelve -->

        <aside class="four wide column la-sidekick">
            <g:render template="/templates/aside1" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
        </aside><!-- .four -->

    </div><!-- .grid -->


    <div id="magicArea"></div>

    <r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller:'subscription', action:'unlinkPackage', params:[subscription:subscriptionInstance.id])}&package="+pkg_id

        $.ajax({url: req_url,
          success: function(result){
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }

      function hideModal(){
        $("[name='coreAssertionEdit']").modal('hide');
      }

      function showCoreAssertionModal(){

        $("[name='coreAssertionEdit']").modal('show');

      }

      <g:if test="${editable}">

      $(document).ready(function() {

         $('#collapseableSubDetails').on('show', function() {
            $('.hidden-license-details i').removeClass('icon-plus').addClass('icon-minus');
        });

        // Reverse it for hide:
        $('#collapseableSubDetails').on('hide', function() {
            $('.hidden-license-details i').removeClass('icon-minus').addClass('icon-plus');
        });
      });

      </g:if>

      <g:if test="${params.asAt && params.asAt.length() > 0}"> $(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>

    </r:script>
  </body>
</html>
