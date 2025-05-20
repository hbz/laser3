<%@ page import="de.laser.properties.SubscriptionProperty; de.laser.storage.PropertyStore; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.Subscription;de.laser.RefdataCategory; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.finance.CostItem" %>
<laser:htmlStart message="myinst.currentSubscriptions.label"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <ui:crumb message="createSubscriptionSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createSubscriptionSurvey.label" type="Survey"/>

<ui:messages data="${flash}"/>

<ui:msg class="info" showIcon="true" hideClose="true" message="allSubscriptions.info"/>

<ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true"/>


<g:render template="/templates/subscription/subscriptionFilter"/>


<g:render template="/survey/subscriptionTableForOwner" model="[selectSubs: false]"/>

<ui:paginate action="$actionName" controller="$controllerName" params="${params}"
             max="${max}" total="${num_sub_rows}"/>


<laser:htmlEnd/>