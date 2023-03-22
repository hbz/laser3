<%@ page import="de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div class="ui stackable grid">
    <div class="eleven wide column">
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

                <g:if test="${accessService.checkPerm(CustomerTypeService.ORG_CONSORTIUM_PRO) && surveyOrg}">
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                        </dt>
                        <dd><ui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

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
                        <dd><ui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                    </dl>

                    <ui:card message="${message(code: 'surveyconfig.url.plural.label')}" href="#surveyUrls"
                             editable="${editable}">
                        <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.url.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                    <g:if test="${surveyUrl.url}">
                                        <ui:linkWithIcon href="${surveyUrl.url}"/>
                                    </g:if>

                                </dd>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.urlComment.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="urlComment" type="textarea"/>
                                </dd>

                                <div class="right aligned">
                                    <g:if test="${editable}">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'default.button.delete.label')}">
                                            <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [message(code: 'surveyconfig.url.label', args: [i+1])])}"
                                                    data-confirm-term-how="delete"
                                                    controller="survey" action="addSurveyUrl"
                                                    params="${[deleteSurveyUrl: surveyUrl.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </span>
                                    </g:if>
                                </div>

                            </dl>
                        </g:each>

                        <g:if test="${editable}">
                            <g:render template="/survey/surveyUrlsModal"/>
                        </g:if>

                    </ui:card>

                    <br />

                    <div class="ui la-tab-with-js">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                            <div class="ui top attached tabular menu">
                                <a class="item active" data-tab="comment">
                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-content="${message(code: "surveyconfig.comment.comment")}">
                                        ${message(code: 'surveyconfig.comment.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </a>
                            </div>

                            <div class="ui bottom attached tab segment active" data-tab="comment">
                                <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                    <div id="commentDiv">
                                        <div id="comment">${raw(surveyConfig.comment)}</div>

                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            wysiwyg.initEditor('#commentDiv #comment');
                                        </laser:script>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui form">
                                        <div class="field">
                                            <textarea class="la-textarea-resize-vertical" name="comment"
                                                      rows="15">${surveyConfig.comment}</textarea>
                                        </div>
                                    </div>
                                </g:else>
                                <br>

                                <div class="left aligned">
                                    <button type="submit"
                                            class="ui button">${message(code: 'default.button.save_changes')}</button>
                                </div>
                            </div>
                        </g:form>

                    </div>

                </g:if>
                <g:else>
                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i+1])}
                            </dt>
                            <dd>
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                overwriteEditable="${false}"/>

                                <g:if test="${surveyUrl.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyUrl.urlComment}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>
                                <ui:linkWithIcon href="${surveyUrl.url}"/>
                            </dd>
                        </dl>
                    </g:each>


                    <div class="ui form la-padding-left-07em">
                        <div class="field">
                            <label>
                                <g:message code="surveyConfigsInfo.comment"/>
                            </label>
                            <g:if test="${surveyConfig.comment}">
                                <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                    <div id="comment-wrapper-${surveyConfig.id}">
                                        <article id="comment-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                            ${raw(surveyConfig.comment)}
                                        </article>
                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            wysiwyg.analyzeNote_TMP( $("#comment-${surveyConfig.id}"), $("#comment-wrapper-${surveyConfig.id}"), true );
                                        </laser:script>
                                    </div>
                                </g:if>
                                <g:else>
                                    <textarea class="la-textarea-resize-vertical" readonly="readonly"
                                              rows="1">${surveyConfig.comment}</textarea>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </g:else>


            </div>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'License', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'Provider', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>

    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <div id="container-documents">
                <g:render template="/survey/surveyLinkCard"/>
            </div>

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-tasks">
                    <laser:render template="/templates/tasks/card"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                </div>


                <div id="container-notes">
                    <laser:render template="/templates/notes/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.checkPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_EDITOR')]}"/>
                </div>
            </g:if>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>
        </div>
    </aside>

</div><!-- .grid -->

<g:if test="${actionName == "show" && contextOrg.id == surveyConfig.surveyInfo.owner.id}">
    <g:set var="surveyProperties" value="${surveyConfig.getSortedSurveyConfigProperties()}"/>

    <ui:greySegment>

        <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <ui:totalNumber
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
            <g:each in="${surveyProperties}" var="surveyPropertyConfig" status="i">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyPropertyConfig.surveyProperty.getI10n('name')}

                        <g:if test="${surveyPropertyConfig.surveyProperty.tenant?.id == contextOrg.id}">
                            <i class='shield alternate icon'></i>
                        </g:if>

                        <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>

                    <td>
                        <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            ${surveyPropertyConfig.surveyProperty.getI10n('expl')}
                        </g:if>
                    </td>
                    <td>

                        ${PropertyDefinition.getLocalizedValue(surveyPropertyConfig.surveyProperty.type)}
                        <g:if test="${surveyPropertyConfig.surveyProperty.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyPropertyConfig.surveyProperty.refdataCategory)}"
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
                                (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyPropertyConfig.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                        <g:form action="setSurveyPropertyMandatory" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyPropertyConfig.id]">

                            <div class="ui checkbox">
                                <input type="checkbox"
                                       onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' :  ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                       name="mandatoryProperty" ${surveyPropertyConfig.mandatoryProperty ? 'checked' : ''}>
                            </div>
                        </g:form>
                    </td>
                    <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                            SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyPropertyConfig.surveyProperty)}">
                        <td>
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyPropertyConfig.surveyProperty.getI10n('name')])}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyPropFromConfig"
                                    id="${surveyPropertyConfig.id}"
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
                                <ui:dropdown name="selectedProperty"

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

    </ui:greySegment>
</g:if>

<g:if test="${surveyResults}">
    <ui:greySegment>
        <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
        <ui:totalNumber total="${surveyResults.size()}"/>
        </h3>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${accessService.checkPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
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
                    <g:if test="${accessService.checkPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
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
            <g:each in="${surveyResults}" var="surveyResult" status="i">

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
                            <ui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${surveyResult.type.isStringType()}">
                            <ui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                            <ui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isDateType()}">
                            <ui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isURLType()}">
                            <ui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                             overwriteEditable="${overwriteEditable}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${surveyResult.urlValue}">
                                <ui:linkWithIcon href="${surveyResult.urlValue}"/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${surveyResult.type.isRefdataValueType()}">

                            <g:if test="${surveyResult.type == PropertyStore.SURVEY_PROPERTY_PARTICIPATION && surveyResult.owner.id != contextOrg.id}">
                                <ui:xEditableRefData owner="${surveyResult}" field="refValue" type="text"
                                                        id="participation"
                                                        config="${surveyResult.type.refdataCategory}"/>
                            </g:if>
                            <g:else>
                                <ui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                        config="${surveyResult.type.refdataCategory}"/>
                            </g:else>
                        </g:elseif>
                    </td>
                    <td>
                        <ui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                    </td>
                    <td>
                        <g:if test="${accessService.checkPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                        </g:if>
                        <g:else>
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </table>
    </ui:greySegment>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>