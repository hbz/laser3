<%@ page import="de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyPackages.linkPackage.plural" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:h1HeaderWithIcon message="surveyPackages.linkPackage"/>
<br>
<br>
<ui:messages data="${flash}"/>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'package.plural')}
<ui:totalNumber total="${surveyPackagesCount}/${recordsCount}"/>
</h2>

<g:render template="/templates/survey/packages" model="[
        processController: 'survey',
        processAction: 'linkSurveyPackage',
        tmplShowCheckbox: editable,
        linkSurveyPackage: true,
        tmplConfigShow: ['lineNumber', 'name', 'status', 'titleCount', 'provider', 'platform', 'curatoryGroup', 'automaticUpdates', 'lastUpdatedDisplay', 'linkSurveyPackage']]"/>

<laser:htmlEnd />
