<laser:serviceInjection />

<g:if test="${actionName == 'index'}">
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'json']}">JSON</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'xml']}">XML</g:link>
        </semui:exportDropdownItem>
        <g:each in="${transforms}" var="transkey,transval">
            <semui:exportDropdownItem>
                <g:if test="${params.filter || params.asAt}">
                    <%-- TODO: ask Ingrid where to inject this code: ${message(code: '', default: 'Achtung! Sie haben zur Zeit einen Filter gesetzt! Dadurch wird nur eine Teilmenge des Bestandes exportiert! Dennoch fortfahren?')} --%>
                    <g:link onclick="return confirm('Achtung! Sie haben zur Zeit einen Filter gesetzt! Dadurch wird nur eine Teilmenge des Bestandes exportiert! Dennoch fortfahren?')" class="item" action="index" id="${params.id}" params="${[format:'xml', transformId:transkey, mode: params.mode, filter: params.filter, asAt: params.asAt]}">${transval.name}</g:link>
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

        <semui:actionsDropdownItem controller="subscriptionDetails" action="copySubscription" params="${[id: params.id]}" message="myinst.copySubscription" />

        <semui:actionsDropdownItem controller="subscriptionDetails" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
        <semui:actionsDropdownItem controller="subscriptionDetails" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />

        <g:if test="${(subscriptionInstance?.getConsortia()?.id == contextService.getOrg()?.id) && !subscriptionInstance.instanceOf}">
            <semui:actionsDropdownItem controller="subscriptionDetails" action="addMembers" params="${[id:params.id]}" message="subscription.details.addMembers.label" />
        </g:if>

        <g:if test="${subscriptionInstance?.type == com.k_int.kbplus.RefdataValue.getByValueAndCategory("Local Licence", "Subscription Type") && !(com.k_int.kbplus.Subscription.findAllByPreviousSubscription(subscriptionInstance))}">
            <semui:actionsDropdownItem controller="subscriptionDetails" action="launchRenewalsProcess"
                                   params="${[id: params.id]}" message="subscription.details.renewals.label"/>
            <semui:actionsDropdownItem controller="myInstitution" action="renewalsUpload"
                                   message="menu.institutions.imp_renew"/>
        </g:if>
        <g:if test="${subscriptionInstance?.type == com.k_int.kbplus.RefdataValue.getByValueAndCategory("Consortial Licence", "Subscription Type") && (com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  contextService.getOrg()?.getallOrgRoleTypeIds()) && !(com.k_int.kbplus.Subscription.findAllByPreviousSubscription(subscriptionInstance))}">
            <semui:actionsDropdownItem controller="subscriptionDetails" action="renewSubscriptionConsortia"
                                       params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>
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
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalgruppen konfigurieren" />
            </g:if>

            <g:if test="${showConsortiaFunctions}">
                <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
                    <div class="divider"></div>
                    <semui:actionsDropdownItem id="audit_config_opener" message="property.audit.menu"/>
                </g:if>
            </g:if>
        </g:if>

    </semui:actionsDropdown>

    <g:render template="/templates/documents/modal" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
    <g:render template="/templates/notes/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>

    <g:render template="/templates/audit/modal_script" model="${[ownobj: subscriptionInstance]}" />
</g:if>

<g:if test="${editable || accessService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')}">
    <g:render template="/templates/tasks/modal_create" model="${[ownobj: subscriptionInstance, owntp: 'subscription']}"/>
</g:if>
