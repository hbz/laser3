<%@ page import="de.laser.ui.Btn; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:htmlStart message="myinst.currentSubscriptions.label" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <ui:messages data="${flash}"/>

    <ui:h1HeaderWithIcon>
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>
    <ui:totalNumber total="${num_sub_rows}"/>
    <ui:anualRings object="${license}" controller="license" action="linkLicenseToSubs" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <laser:render template="${customerTypeService.getSubscriptionFilterTemplatePath()}"/>

    <div class="ui buttons">
        <g:link action="linkToSubscription" class="${Btn.POSITIVE}" params="${params+[id:license.id, subscription:"all"]}"><g:message code="license.linkAll"/></g:link>
        <div class="or" data-text="${message(code:'default.or')}"></div>
        <g:link action="linkToSubscription" class="${Btn.NEGATIVE}" params="${params+[id:license.id,unlink:true,subscription:"all"]}"><g:message code="license.unlinkAll"/></g:link>
    </div>
    <laser:render template="/templates/subscription/subscriptionTable"/>

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

<laser:htmlEnd />
