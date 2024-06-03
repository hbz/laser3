<%@ page import="de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyVendors.linkVendor.plural" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:h1HeaderWithIcon message="surveyVendors.linkVendor"/>
<br>
<br>
<ui:messages data="${flash}"/>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'surveyVendors.label')}
<ui:totalNumber total="${surveyVendorsCount}/${vendorListTotal}"/>
</h2>

<g:render template="/templates/survey/vendors" model="[
        processController: 'survey',
        processAction: 'linkSurveyVendor',
        tmplShowCheckbox: editable,
        linkSurveyVendor: true,
        tmplConfigShow: ['lineNumber', 'sortname', 'name', 'isWekbCurated', 'linkSurveyVendor']]"/>

<laser:htmlEnd />
