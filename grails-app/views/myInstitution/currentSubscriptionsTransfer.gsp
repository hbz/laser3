<%@ page import="de.laser.CustomerTypeService; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>

<laser:htmlStart message="myinst.currentSubscriptions.label" serviceInjection="true" />

        <ui:breadcrumbs>
            <ui:crumb message="myinst.currentSubscriptions.label" class="active" />
        </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true" />

    <ui:messages data="${flash}"/>

    <laser:render template="/templates/subscription/subscriptionFilter"/>

    <laser:render template="/templates/subscription/subscriptionTable"/>

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

<laser:htmlEnd />
