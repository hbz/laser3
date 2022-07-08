<%@ page import="de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem;de.laser.FormService" %>
<laser:htmlStart message="license.member.plural" serviceInjection="true"/>

    <g:set var="entityName" value="${message(code: 'org.label')}"/>

<semui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</semui:debugInfo>

<laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

<semui:controlButtons>
    <%-- is as placeholder for breaking header --%>
</semui:controlButtons>

<semui:messages data="${flash}"/>

<semui:h1HeaderWithIcon>
    <semui:xEditable owner="${license}" field="reference" id="reference"/>
    <semui:totalNumber total="${totalCount}"/>
</semui:h1HeaderWithIcon>

<semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

<laser:render template="nav" />

<laser:render template="/templates/subscription/consortiaSubscriptionFilter"/>
<div class="ui buttons">
    <g:link action="linkToSubscription" class="ui button positive" params="${params+[id:license.id,subscription:"all",(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}"><g:message code="license.linkAll"/></g:link>
    <div class="or" data-text="${message(code:'default.or')}"></div>
    <g:link action="linkToSubscription" class="ui button negative" params="${params+[id:license.id,unlink:true,subscription:"all",(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}"><g:message code="license.unlinkAll"/></g:link>
</div>
<laser:render template="/templates/subscription/consortiaSubscriptionTable"/>

<laser:htmlEnd />
