<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.wekb.Platform; de.laser.wekb.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyPackages.linkPackage.plural" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>
<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="${Icon.SUBSCRIPTION} bordered inverted orange la-object-extended"
                     href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<h2> <g:message code="surveyPackages.linkPackage"/>:</h2>
<br>
<br>
<g:link class="${Btn.SIMPLE} right floated" controller="survey" action="surveyPackages"
        params="${[id: params.id, surveyConfigID: surveyConfig.id]}"><g:message code="default.button.back"/></g:link>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'package.plural')}
<ui:totalNumber total="${surveyPackagesCount}/${recordsCount}"/>
</h2>
<br>
<br>
<ui:messages data="${flash}"/>


<g:render template="/templates/survey/packages" model="[
        processController: 'survey',
        processAction: 'linkSurveyPackage',
        tmplShowCheckbox: editable,
        linkSurveyPackage: true,
        tmplConfigShow: ['lineNumber', 'name', 'status', 'titleCount', 'provider', 'platform', 'curatoryGroup', 'automaticUpdates', 'lastUpdatedDisplay', 'linkSurveyPackage']]"/>

<laser:htmlEnd />
