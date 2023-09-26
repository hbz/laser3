<%@ page import="de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem;de.laser.FormService" %>
<laser:htmlStart message="license.member.plural" serviceInjection="true"/>

    <g:set var="entityName" value="${message(code: 'org.label')}"/>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</ui:debugInfo>

<laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

<ui:controlButtons>
    <%-- is as placeholder for breaking header --%>
</ui:controlButtons>

<ui:messages data="${flash}"/>

<ui:h1HeaderWithIcon>
    <ui:xEditable owner="${license}" field="reference" id="reference"/>
</ui:h1HeaderWithIcon>
<ui:totalNumber total="${totalCount}"/>
<ui:anualRings object="${license}" controller="license" action="linkMemberLicensesToSubs" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

<laser:render template="nav" />

<laser:render template="${customerTypeService.getConsortiaSubscriptionFilterTemplatePath()}" />

<div class="ui buttons">
    <g:link action="linkToSubscription" class="ui button positive" params="${params+[id:license.id,subscription:"all",(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}"><g:message code="license.linkAll"/></g:link>
    <div class="or" data-text="${message(code:'default.or')}"></div>
    <g:link action="linkToSubscription" class="ui button negative" params="${params+[id:license.id,unlink:true,subscription:"all",(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}"><g:message code="license.unlinkAll"/></g:link>
</div>
<laser:render template="/templates/subscription/consortiaSubscriptionTable"/>

<laser:htmlEnd />
