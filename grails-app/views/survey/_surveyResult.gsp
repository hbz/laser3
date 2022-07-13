<%@ page import="de.laser.storage.PropertyStore; de.laser.storage.RDStore;" %>
<g:if test="${surResult.surveyConfig.subSurveyUseForTransfer && surveyOrg.existsMultiYearTerm()}">

    <g:message code="surveyOrg.perennialTerm.available"/>

    <g:if test="${surResult.comment}">
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-position="right center"
              data-content="${surResult.comment}">
            <i class="question circle icon"></i>
        </span>
    </g:if>
</g:if>
<g:else>

    <g:if test="${surResult.type.isIntegerType()}">
        <ui:xEditable owner="${surResult}" type="text" field="intValue"/>
    </g:if>
    <g:elseif test="${surResult.type.isStringType()}">
        <ui:xEditable owner="${surResult}" type="text" field="stringValue"/>
    </g:elseif>
    <g:elseif test="${surResult.type.isBigDecimalType()}">
        <ui:xEditable owner="${surResult}" type="text" field="decValue"/>
    </g:elseif>
    <g:elseif test="${surResult.type.isDateType()}">
        <ui:xEditable owner="${surResult}" type="date" field="dateValue"/>
    </g:elseif>
    <g:elseif test="${surResult.type.isURLType()}">
        <ui:xEditable owner="${surResult}" type="url" field="urlValue"
                         overwriteEditable="${overwriteEditable}"
                         class="la-overflow la-ellipsis"/>
        <g:if test="${surResult.urlValue}">
            <ui:linkIcon/>
        </g:if>
    </g:elseif>
    <g:elseif test="${surResult.type.isRefdataValueType()}">
        <ui:xEditableRefData owner="${surResult}" type="text"
                                field="refValue"
                                config="${surResult.type.refdataCategory}"/>
    </g:elseif>
    <g:if test="${surResult.comment}">
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-position="right center"
              data-content="${surResult.comment}">
            <i class="question circle icon"></i>
        </span>
    </g:if>

    <g:if test="${surResult.type.id == PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id && surResult.getResult() == RDStore.YN_NO.getI10n('value')}">
        <span class="la-long-tooltip la-popup-tooltip la-delay"
              data-position="top right"
              data-variation="tiny"
              data-content="${message(code: 'surveyResult.particiption.terminated')}">
            <i class="minus circle big red icon"></i>
        </span>
    </g:if>

</g:else>