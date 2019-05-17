<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Links;com.k_int.kbplus.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes" %>

<laser:serviceInjection />

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
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
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
<g:if test="${editable}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />

        <div class="divider"></div>

        <semui:actionsDropdownItem controller="subscription" action="copySubscription" params="${[id: params.id]}" message="myinst.copySubscription" />
        %{--Todo Remove ifAnyGranted wenn Task erms-776 fertig ist!--}%
        <sec:ifAnyGranted roles="INST_EDITOR, INST_ADM, ROLE_DATAMANAGER, ROLE_ADMIN, ROLE_YODA">
            <semui:actionsDropdownItem controller="subscription" action="copyElementsIntoSubscription" params="${[id: params.id]}" message="myinst.copyElementsIntoSubscription" />
        </sec:ifAnyGranted>

        <semui:actionsDropdownItem controller="subscription" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
        <semui:actionsDropdownItem controller="subscription" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />

        <g:if test="${(subscriptionInstance?.getConsortia()?.id == contextService.getOrg()?.id) && !subscriptionInstance.instanceOf}">
            <semui:actionsDropdownItem controller="subscription" action="addMembers" params="${[id:params.id]}" message="subscription.details.addMembers.label" />
        </g:if>

        <g:set var="previousSubscriptions" value="${Links.findByLinkTypeAndObjectTypeAndDestination(RDStore.LINKTYPE_FOLLOWS,Subscription.class.name,subscriptionInstance.id)}"/>
        <g:if test="${subscriptionInstance?.type == RefdataValue.getByValueAndCategory("Local Licence", "Subscription Type") && !previousSubscriptions}">
            <semui:actionsDropdownItem controller="subscription" action="launchRenewalsProcess"
                                   params="${[id: params.id]}" message="subscription.details.renewals.label"/>
            <semui:actionsDropdownItem controller="myInstitution" action="renewalsUpload"
                                   message="menu.institutions.imp_renew"/>
        </g:if>

        <g:if test="${subscriptionInstance?.type == RefdataValue.getByValueAndCategory("Consortial Licence", "Subscription Type") && (RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  contextService.getOrg()?.getallOrgTypeIds()) && !previousSubscriptions}">
            <semui:actionsDropdownItem controller="subscription" action="renewSubscriptionConsortia"
                                       params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>      
        </g:if>
        
          <g:if test="${subscriptionInstance?.type == RefdataValue.getByValueAndCategory("Consortial Licence", "Subscription Type") && (RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in contextService.getOrg()?.getallOrgTypeIds())}">

              <semui:actionsDropdownItem controller="subscription" action="linkLicenseConsortia"
                                         params="${[id: params.id]}"
                                         message="subscription.details.subscriberManagement.label"/>
        </g:if>

        <g:if test="${actionName == 'members'}">
            <g:if test="${validSubChilds}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses"/>
            </g:if>
        </g:if>

        <g:if test="${actionName == 'show'}">
            <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen konfigurieren" />
            </g:if>

            <%--
            <g:if test="${showConsortiaFunctions}">
                <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
                    <div class="divider"></div>
                    <semui:actionsDropdownItem id="audit_config_opener" message="property.audit.menu"/>
                </g:if>
            </g:if>
            --%>

            <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> Lizenz löschen</g:link>
            </g:if>
        </g:if>

    </semui:actionsDropdown>

    <g:render template="/templates/documents/modal" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
    <g:render template="/templates/notes/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>



    <%--<g:render template="/templates/audit/modal_script" model="${[ownobj: subscriptionInstance]}" />--%>
</g:if>

<g:if test="${editable || accessService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')}">
    <g:render template="/templates/tasks/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
</g:if>
