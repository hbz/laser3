<%@ page import="de.laser.ui.Icon; de.laser.storage.PropertyStore; de.laser.storage.RDStore;" %>

    <g:if test="${surResult.type.isLongType()}">
        <ui:xEditable owner="${surResult}" type="text" field="longValue"/>
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
            <ui:linkWithIcon/>
        </g:if>
    </g:elseif>
    <g:elseif test="${surResult.type.isRefdataValueType()}">
        <g:if test="${surResult.type == PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING}">
            <ui:xEditableRefData owner="${surResult}" type="text" field="refValue" constraint="removeValues_invoiceProcessing"
                                 config="${surResult.type.refdataCategory}"/>
        </g:if>
        <g:else>
            <ui:xEditableRefData owner="${surResult}" type="text"
                             field="refValue"
                             config="${surResult.type.refdataCategory}"/>
        </g:else>

    </g:elseif>
    <g:if test="${surResult.comment}">
        <span class="la-long-tooltip la-popup-tooltip"
              data-position="right center"
              data-content="${surResult.comment}">
            <i class="${Icon.TOOLTIP.HELP}"></i>
        </span>
    </g:if>

    <g:if test="${surResult.type.id == PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id && surResult.getResult() == RDStore.YN_NO.getI10n('value')}">
        <span class="la-long-tooltip la-popup-tooltip"
              data-position="top right"
              data-variation="tiny"
              data-content="${message(code: 'surveyResult.particiption.terminated')}">
            <i class="minus circle big red icon"></i>
        </span>
    </g:if>
