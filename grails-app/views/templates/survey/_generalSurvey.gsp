<%@ page import="de.laser.properties.PropertyDefinition; de.laser.helper.RDStore; de.laser.SurveyOrg; de.laser.SurveyConfigProperties; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div class="ui stackable grid">
    <div class="ten wide column">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">
            <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
            <div class="ui horizontal segments">

                <div class="ui segment left aligned">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
                    <g:link controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]">
                        <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
                    </g:link>
                </div>
            </div>
        </g:if>

        <div class="ui card ">
            <div class="content">

                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM') && surveyOrg}">
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                        </dt>
                        <dd><semui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

                    </dl>
                </g:if>

                <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                    <dl>
                        <dt class="control-label">
                            <div class="ui icon la-popup-tooltip la-delay"
                                 data-content="${message(code: "surveyconfig.internalComment.comment")}">
                                ${message(code: 'surveyconfig.internalComment.label')}
                                <i class="question small circular inverted icon"></i>
                            </div>
                        </dt>
                        <dd><semui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                    </dl>

                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url" type="text"/>
                            <g:if test="${surveyConfig.url}">
                                <semui:linkIcon href="${surveyConfig.url}"/>
                            </g:if>

                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.urlComment.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment" type="textarea"/>
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url2.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url2" type="text"/>
                            <g:if test="${surveyConfig.url2}">
                                <semui:linkIcon href="${surveyConfig.url2}"/>
                            </g:if>
                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.urlComment2.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment2" type="textarea"/>
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.url3.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="url3" type="text"/>
                            <g:if test="${surveyConfig.url3}">
                                <semui:linkIcon href="${surveyConfig.url3}"/>
                            </g:if>
                        </dd>
                        <dt class="control-label">
                            ${message(code: 'surveyconfig.urlComment3.label')}
                        </dt>
                        <dd>
                            <semui:xEditable owner="${surveyConfig}" field="urlComment3" type="textarea"/>
                        </dd>

                    </dl>

                    <br />

                    <div class="ui form">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                            <div class="field">
                                <label><div class="ui icon la-popup-tooltip la-delay"
                                            data-content="${message(code: "surveyconfig.comment.comment")}">
                                    ${message(code: 'surveyconfig.comment.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div></label>
                                <textarea name="comment" rows="15">${surveyConfig.comment}</textarea>
                            </div>

                            <div class="left aligned">
                                <button type="submit"
                                        class="ui button">${message(code: 'default.button.save_changes')}</button>
                            </div>
                        </g:form>
                    </div>

                </g:if>
                <g:else>
                    <g:if test="${surveyConfig.url}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>
                                <semui:linkIcon href="${surveyConfig.url}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <g:if test="${surveyConfig.url2}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url2.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url2" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment2}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment2}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>

                                <semui:linkIcon href="${surveyConfig.url2}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <g:if test="${surveyConfig.url3}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url3.label')}
                            </dt>
                            <dd>
                                <semui:xEditable owner="${surveyConfig}" field="url3" type="text"
                                                 overwriteEditable="${false}"/>

                                <g:if test="${surveyConfig.urlComment3}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyConfig.urlComment3}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>

                                <semui:linkIcon href="${surveyConfig.url3}"/>
                            </dd>

                        </dl>
                    </g:if>

                    <div class="ui form la-padding-left-07em">
                        <div class="field">
                            <label>
                                <g:message code="surveyConfigsInfo.comment"/>
                            </label>
                            <g:if test="${surveyConfig.comment}">
                                <textarea readonly="readonly" rows="15">${surveyConfig.comment}</textarea>
                            </g:if>
                            <g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </g:else>


            </div>
        </div>
    </div>
    <aside class="six wide column la-sidekick">
        <div class="ui one cards">
            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-tasks">
                    <g:render template="/templates/tasks/card"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                </div>


                <div id="container-notes">
                    <g:render template="/templates/notes/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')]}"/>
                </div>

                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">

                    <g:render template="/templates/tasks/modal_create"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>

                </g:if>
                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">
                    <g:render template="/templates/notes/modal_create"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
                </g:if>
            </g:if>

            <div id="container-documents">
                <g:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>
        </div>
    </aside>

</div><!-- .grid -->

<g:if test="${actionName == "show" && contextOrg.id == surveyConfig.surveyInfo.owner.id}">
    <g:set var="surveyProperties" value="${surveyConfig.getSortiedSurveyConfigProperties()}"/>

    <semui:form>

        <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <semui:totalNumber
                total="${surveyProperties.size()}"/></h4>

        <table class="ui celled sortable la-js-responsive-table table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.name')}</th>
                <th>${message(code: 'surveyProperty.expl.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyProperty.mandatoryProperty')}</th>
                <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING && surveyProperties}">
                    <th>${message(code:'default.actions.label')}</th>
                </g:if>
            </tr>
            </thead>

            <tbody>
            <g:each in="${surveyProperties}" var="surveyProperty" status="i">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyProperty.surveyProperty.getI10n('name')}

                        <g:if test="${surveyProperty.surveyProperty.tenant?.id == contextOrg.id}">
                            <i class='shield alternate icon'></i>
                        </g:if>

                        <g:if test="${surveyProperty.surveyProperty.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty.surveyProperty.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>

                    <td>
                        <g:if test="${surveyProperty.surveyProperty.getI10n('expl')}">
                            ${surveyProperty.surveyProperty.getI10n('expl')}
                        </g:if>
                    </td>
                    <td>

                        ${PropertyDefinition.getLocalizedValue(surveyProperty.surveyProperty.type)}
                        <g:if test="${surveyProperty.surveyProperty.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyProperty.surveyProperty.refdataCategory)}"
                                    var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                            <br />
                            (${refdataValues.join('/')})
                        </g:if>

                    </td>

                    <td>
                        <g:set var="surveyPropertyMandatoryEditable" value="${(editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                                (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyProperty.surveyProperty != RDStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                        <g:form action="setSurveyPropertyMandatory" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyProperty.id]">

                            <div class="ui checkbox">
                                <input type="checkbox"
                                       onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' :  ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                       name="mandatoryProperty" ${surveyProperty.mandatoryProperty ? 'checked' : ''}>
                            </div>
                        </g:form>
                    </td>
                    <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                            SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty.surveyProperty)}">
                        <td>
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyProperty.surveyProperty.getI10n('name')])}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyPropFromConfig"
                                    id="${surveyProperty.id}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </td>
                    </g:if>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <g:if test="${editable && properties && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                    <td colspan="6">
                        <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                            <g:hiddenField id="surveyInfo_id_${surveyInfo.id}" name="id" value="${surveyInfo.id}"/>
                            <g:hiddenField id="surveyConfig_id_${surveyConfig?.id}" name="surveyConfigID" value="${surveyConfig?.id}"/>

                            <div class="field required">
                                <label>${message(code: 'surveyConfigs.property')} <g:message code="messageRequiredField" /></label>
                                <semui:dropdown name="selectedProperty"

                                                class="la-filterPropDef"
                                                from="${properties}"
                                                iconWhich="shield alternate"
                                                optionKey="${{ "${it.id}" }}"
                                                optionValue="${{ it.getI10n('name') }}"
                                                noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                                required=""/>

                            </div>
                            <input type="submit" class="ui button"
                                   value="${message(code: 'surveyConfigsInfo.add.button')}"/>

                        </g:form>
                    </td>
                </g:if>
            </tr>
            </tfoot>

        </table>

    </semui:form>
</g:if>

<g:if test="${surveyResults}">
    <semui:form>
        <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
        <semui:totalNumber
                total="${surveyResults.size()}"/>
        </h3>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                        ${message(code: 'surveyResult.participantComment')}
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
                <th>
                    <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentOnlyForParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyResults.sort { it.type.getI10n('name') }}" var="surveyResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult.type.getI10n('name')}

                        <g:if test="${surveyResult.type.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${surveyResult.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:set var="surveyConfigProperties"
                               value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                        <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                <i class="info circle icon"></i>
                            </span>
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
                            <br />
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${surveyResult.type.isIntegerType()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${surveyResult.type.isStringType()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                            <semui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isDateType()}">
                            <semui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isURLType()}">
                            <semui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                             overwriteEditable="${overwriteEditable}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${surveyResult.urlValue}">
                                <semui:linkIcon/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isRefdataValueType()}">

                            <g:if test="${surveyResult.type == RDStore.SURVEY_PROPERTY_PARTICIPATION && surveyResult.owner.id != contextOrg.id}">
                                <semui:xEditableRefData owner="${surveyResult}" field="refValue" type="text"
                                                        id="participation"
                                                        config="${surveyResult.type.refdataCategory}"/>
                            </g:if>
                            <g:else>
                                <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                        config="${surveyResult.type.refdataCategory}"/>
                            </g:else>
                        </g:elseif>
                    </td>
                    <td>
                        <semui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                    </td>
                    <td>
                        <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_USER')}">
                            <semui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                        </g:if>
                        <g:else>
                            <semui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </table>
    </semui:form>
</g:if>
