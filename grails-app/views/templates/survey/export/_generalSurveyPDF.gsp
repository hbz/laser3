<%@ page import="de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div>
    <div>
        <g:if test="${ownerView && surveyOrg}">
            <dl>
                <dt>
                    ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                </dt>
                <dd>
                    ${surveyOrg.ownerComment}
                </dd>
            </dl>
        </g:if>

        <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
            <dl>
                <dt>
                    ${message(code: 'surveyconfig.url.label', args: [i+1])}
                </dt>
                <dd>
                    <g:link uri="${surveyUrl.url}">
                        ${surveyUrl.url}
                    </g:link>
                    <g:if test="${surveyUrl.urlComment}">
                        <br> ${message(code: 'surveyconfig.urlComment.label', args: [i+1])}: ${surveyUrl.urlComment}">
                    </g:if>
                </dd>
            </dl>
        </g:each>

        <dl>
            <dt>
                <g:message code="surveyConfigsInfo.comment"/>
            </dt>
            <dd>
                <g:if test="${surveyConfig.comment}">
                    ${surveyConfig.comment}
                </g:if>
                <g:else>
                    <g:message code="surveyConfigsInfo.comment.noComment"/>
                </g:else>
            </dd>
        </dl>
    </div>

    <g:if test="${surveyInfo.license}">
        <h3>
            <g:message code="license.label"/>
        </h3>
        <g:link absolute="true" controller="license" action="show" id="${surveyInfo.license.id}">
            ${surveyInfo.license.reference} (${surveyInfo.license.status.getI10n("value")})
        </g:link>
    </g:if>


    <g:if test="${surveyInfo.provider}">
        <h3>
            <g:message code="default.provider.label"/>
        </h3>

        <g:link absolute="true" controller="organisation" action="show" id="${surveyInfo.provider.id}">
            ${surveyInfo.provider.name}
        </g:link>
    </g:if>

</div><!-- .grid -->

<g:if test="${surveyResults}">
    <h3><g:message code="surveyConfigsInfo.properties"/>
    (${surveyResults.size()})
    </h3>

    <table>
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>
                <g:if test="${ownerView}">
                    ${message(code: 'surveyResult.participantComment')}
                </g:if>
                <g:else>
                    ${message(code: 'surveyResult.commentParticipant')}
                </g:else>
            </th>
            <th>
                <g:if test="${ownerView}">
                    ${message(code: 'surveyResult.commentOnlyForOwner')}
                </g:if>
                <g:else>
                    ${message(code: 'surveyResult.commentOnlyForParticipant')}
                </g:else>
            </th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">
            <tr>
                <td>
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult.type.getI10n('name')}

                    <g:set var="surveyConfigProperties"
                           value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                    <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                        *
                    </g:if>
                </td>
                <td>
                    ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                    <g:if test="${surveyResult.type.isRefdataValueType()}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                var="refdataValue">
                            <g:set var="refdataValues"
                                   value="${refdataValues + refdataValue.getI10n('value')}"/>
                        </g:each>
                        <br/>
                        (${refdataValues.join('/')})
                    </g:if>
                </td>
                <td>
                    <g:if test="${surveyResult.type.isIntegerType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                         field="intValue"/>
                    </g:if>
                    <g:elseif test="${surveyResult.type.isStringType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                         field="stringValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                         field="decValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type.isDateType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="date"
                                         field="dateValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type.isURLType()}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="url"
                                         field="urlValue"
                                         class="la-overflow la-ellipsis"/>
                        <g:if test="${surveyResult.urlValue}">
                            <ui:linkWithIcon href="${surveyResult.urlValue}"/>
                        </g:if>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type.isRefdataValueType()}">
                        <ui:xEditableRefData overwriteEditable="${false}" owner="${surveyResult}" type="text"
                                                field="refValue"
                                                config="${surveyResult.type.refdataCategory}"/>
                    </g:elseif>
                </td>
                <td>
                    <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                     field="comment"/>
                </td>
                <td>
                    <g:if test="${ownerView}">
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                         field="ownerComment"/>
                    </g:if>
                    <g:else>
                        <ui:xEditable overwriteEditable="${false}" owner="${surveyResult}" type="textarea"
                                         field="participantComment"/>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>
