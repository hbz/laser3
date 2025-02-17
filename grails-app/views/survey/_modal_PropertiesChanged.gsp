<ui:modal id="modalPropertiesChanged" text="${message(code: 'renewalEvaluation.propertiesChanged')}"
             hideSubmitButton="true">

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
            <th>${message(code: 'propertyDefinition.label')}</th>
            <th>${message(code: 'subscription')}-${message(code: 'propertyDefinition.label')}</th>
            <th>${message(code: 'survey.label')}-${message(code: 'propertyDefinition.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${changedProperties}" var="changedProperty" status="i">
            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td><g:link controller="organisation" action="show" id="${changedProperty.participant.id}">${changedProperty.participant.sortname}</g:link></td>
                <td>
                    ${propertyDefinition.getI10n('name')}
                </td>
                <td>
                    <g:if test="${changedProperty.subscriptionProperty.getValue() != "" && changedProperty.subscriptionProperty.getValue() != null}">
                        <g:if test="${changedProperty.subscriptionProperty.type.isLongType()}">
                            <ui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="longValue"
                                             overwriteEditable="${false}"/>
                        </g:if>

                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isStringType()}">
                            <ui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="stringValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isBigDecimalType()}">
                            <ui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="decValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isDateType()}">
                            <ui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="date" field="dateValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isURLType()}">
                            <ui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="url" field="urlValue"
                                             overwriteEditable="${false}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${changedProperty.subscriptionProperty.value}">
                                <ui:linkWithIcon
                                        href="${changedProperty.subscriptionProperty.value}"/>
                            </g:if>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isRefdataValueType()}">
                            <span data-position="top left" class="la-popup-tooltip la-delay"
                                  data-content="${changedProperty.subscriptionProperty.refValue?.getI10n("value")}">
                                ${changedProperty.subscriptionProperty.refValue?.getI10n("value")}
                            </span>
                        </g:elseif>
                        <g:else>
                            <div>
                                ${changedProperty.subscriptionProperty.value}
                            </div>
                        </g:else>
                    </g:if>

                </td>
                <td>${changedProperty.surveyResult.getResult()}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
</ui:modal>
