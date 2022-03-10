<semui:modal id="modalPropertiesChanged" text="${message(code: 'renewalEvaluation.propertiesChanged')}"
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
                <td>${changedProperty.participant}</td>
                <td>
                    ${propertyDefinition.getI10n('name')}
                </td>
                <td>
                    <g:if test="${changedProperty.subscriptionProperty.getValue() != "" && changedProperty.subscriptionProperty.getValue() != null}">
                        <g:if test="${changedProperty.subscriptionProperty.type.isIntegerType()}">
                            <semui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="intValue"
                                             overwriteEditable="${false}"/>
                        </g:if>

                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isStringType()}">
                            <semui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="stringValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isBigDecimalType()}">
                            <semui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="text" field="decValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isDateType()}">
                            <semui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="date" field="dateValue"
                                             overwriteEditable="${false}"/>
                        </g:elseif>
                        <g:elseif
                                test="${changedProperty.subscriptionProperty.type.isURLType()}">
                            <semui:xEditable owner="${changedProperty.subscriptionProperty}"
                                             type="url" field="urlValue"
                                             overwriteEditable="${false}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${changedProperty.subscriptionProperty.value}">
                                <semui:linkIcon
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
</semui:modal>
