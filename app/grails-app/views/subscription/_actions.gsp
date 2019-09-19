<%@ page import="de.laser.interfaces.TemplateSupport; com.k_int.kbplus.OrgRole; com.k_int.kbplus.Org; de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Links;com.k_int.kbplus.Subscription;com.k_int.kbplus.SubscriptionPackage" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes" %>

<laser:serviceInjection />

<g:set var="user" value="${contextService.user}"/>
<g:set var="org" value="${contextService.org}"/>

    <g:if test="${actionName == 'index'}">
    <semui:exportDropdown>
        <%--<semui:exportDropdownItem>
            <g:link class="item" controller="subscription" action="index" id="${subscriptionInstance.id}" params="${params + [format:'json']}">JSON</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" controller="subscription" action="index" id="${subscriptionInstance.id}" params="${params + [format:'xml']}">XML</g:link>
        </semui:exportDropdownItem>--%>
        <semui:exportDropdownItem>
            <g:if test="${params.filter || params.asAt}">
                <g:link  class="item js-open-confirm-modal"
                         data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                         data-confirm-term-how="ok"
                         action="index"
                         id="${params.id}"
                         params="${[exportKBart:true, mode: params.mode, filter: params.filter, asAt: params.asAt]}">KBart Export
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="index" id="${params.id}" params="${[exportKBart:true, mode: params.mode]}">KBart Export</g:link>
            </g:else>
        </semui:exportDropdownItem>
        <g:each in="${transforms}" var="transkey,transval">
            <semui:exportDropdownItem>
                <g:if test="${params.filter || params.asAt}">
                    <g:link  class="item js-open-confirm-modal"
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok"
                            action="index"
                            id="${params.id}"
                            params="${[format:'xml', transformId:transkey, mode: params.mode, filter: params.filter, asAt: params.asAt]}">${transval.name}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="index" id="${params.id}" params="${[format:'xml', transformId:transkey, mode: params.mode]}">${transval.name}</g:link>
                </g:else>
            </semui:exportDropdownItem>
        </g:each>
    </semui:exportDropdown>
</g:if>
<semui:actionsDropdown>
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR')}">
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <div class="divider"></div>
        <%
            Subscription sub = Subscription.get(params.id)
            boolean isCopySubEnabled = sub?.orgRelations?.find{it.org.id == org.id && (it.roleType.id == RDStore.OR_SUBSCRIPTION_CONSORTIA.id || it.roleType.id == RDStore.OR_SUBSCRIBER.id)}
        %>
        <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_YODA">
            <% isCopySubEnabled = true %>
        </sec:ifAnyGranted>
        <g:if test="${isCopySubEnabled}">
            <semui:actionsDropdownItem controller="subscription" action="copySubscription" params="${[id: params.id]}" message="myinst.copySubscription" />
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled controller="subscription" action="copySubscription" params="${[id: params.id]}" message="myinst.copySubscription" />
        </g:else>
        <semui:actionsDropdownItem controller="subscription" action="copyElementsIntoSubscription" params="${[id: params.id]}" message="myinst.copyElementsIntoSubscription" />

        <g:if test="${editable}">
            <semui:actionsDropdownItem controller="subscription" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
            <semui:actionsDropdownItem controller="subscription" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled controller="subscription" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
            <semui:actionsDropdownItemDisabled controller="subscription" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />
        </g:else>

        <%-- TODO: once the hookup has been decided, the ifAnyGranted securing can be taken down --%>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:if test="${subscriptionInstance.instanceOf}">
                <g:if test="${params.pkgfilter}">
                    <g:set var="pkg" value="${SubscriptionPackage.get(params.pkgfilter)}"/>
                    <g:if test="${!pkg.finishDate}">
                        <semui:actionsDropdownItem controller="subscription" action="renewEntitlements" params="${[targetSubscriptionId:params.id,packageId:params.pkgfilter]}" message="subscription.details.renewEntitlements.label"/>
                    </g:if>
                    <g:else>
                        <semui:actionsDropdownItemDisabled message="subscription.details.renewEntitlements.label" tooltip="${message(code:'subscription.details.renewEntitlements.packageRenewalAlreadySubmitted')}"/>
                    </g:else>
                </g:if>
                <g:else>
                    <semui:actionsDropdownItemDisabled message="subscription.details.renewEntitlements.label" tooltip="${message(code:'subscription.details.renewEntitlements.packageMissing')}"/>
                </g:else>
            </g:if>
        </sec:ifAnyGranted>

        <g:if test="${showConsortiaFunctions || showCollectiveFunctions || subscriptionInstance.administrative}">
            <semui:actionsDropdownItem controller="subscription" action="addMembers" params="${[id:params.id]}" message="subscription.details.addMembers.label" />
        </g:if>

        <g:set var="previousSubscriptions" value="${Links.findByLinkTypeAndObjectTypeAndDestination(RDStore.LINKTYPE_FOLLOWS,Subscription.class.name,subscriptionInstance.id)}"/>
        <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_YODA">
            <div class="divider">OLD:</div>
            <g:if test="${subscriptionInstance.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_LOCAL && !previousSubscriptions}">
                <semui:actionsDropdownItem controller="subscription" action="launchRenewalsProcess"
                                       params="${[id: params.id]}" message="subscription.details.renewals.label"/>
                <semui:actionsDropdownItem controller="myInstitution" action="renewalsUpload"
                                       message="menu.institutions.imp_renew"/>
            </g:if>

            <g:if test="${subscriptionInstance.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_CONSORTIAL,TemplateSupport.CALCULATED_TYPE_COLLECTIVE] && accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM") && !previousSubscriptions}">
                <semui:actionsDropdownItem controller="subscription" action="renewSubscriptionConsortia"
                                           params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>
            </g:if>
            <div class="divider"></div>
        </sec:ifAnyGranted>

        <g:if test="${subscriptionInstance.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_CONSORTIAL,TemplateSupport.CALCULATED_TYPE_COLLECTIVE] && accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")}">
            <g:if test="${previousSubscriptions}">
                <semui:actionsDropdownItemDisabled controller="subscription" action="renewSubscription_Consortia"
                                                   params="${[id: params.id]}" tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewals.label"/>
            </g:if>
            <g:else>
                <semui:actionsDropdownItem controller="subscription" action="renewSubscription_Consortia"
                                       params="${[id: params.id]}" message="subscription.details.renewals.label"/>
            </g:else>
        </g:if>
        <g:if test ="${subscriptionInstance.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_LOCAL}">
            <g:if test ="${previousSubscriptions}">
                <semui:actionsDropdownItemDisabled controller="subscription" action="renewSubscription_Local"
                                                   params="${[id: params.id]}" message="subscription.details.renewals.label"/>
            </g:if>
            <g:else>
                <semui:actionsDropdownItem controller="subscription" action="renewSubscription_Local"
                                       params="${[id: params.id]}" message="subscription.details.renewals.label"/>
            </g:else>
        </g:if>

          <g:if test="${subscriptionInstance.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_CONSORTIAL,TemplateSupport.CALCULATED_TYPE_COLLECTIVE] && accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")}">

              <semui:actionsDropdownItem controller="subscription" action="linkLicenseMembers"
                                         params="${[id: params.id]}"
                                         message="subscription.details.subscriberManagement.label"/>
        </g:if>

        <g:if test="${actionName == 'members'}">
            <g:if test="${validSubChilds}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </g:if>

        <g:if test="${actionName == 'show'}">
            <g:if test="${editable}">
                <g:if test="${accessService.checkMinUserOrgRole(user,org,"INST_EDITOR")}">
                    <div class="divider"></div>
                    <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen konfigurieren" />
                </g:if>
            </g:if>

            <g:if test="${editable}">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> Lizenz löschen</g:link>
            </g:if>
            <g:else>
                <a class="item disabled" href="#"><i class="trash alternate icon"></i> Lizenz löschen</a>
            </g:else>
        </g:if>

    </g:if>
</semui:actionsDropdown>

<g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
    <g:render template="/templates/documents/modal" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
    <g:render template="/templates/tasks/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
</g:if>
<g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR')}">
    <g:render template="/templates/notes/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
</g:if>
