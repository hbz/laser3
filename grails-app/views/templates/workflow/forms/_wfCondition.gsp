<%@ page import="de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.workflow.WorkflowHelper; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form url="${formUrl}" method="POST" class="ui form">

    <div class="field required">
        <label for="${prefix}_title">${message(code:'default.title.label')}</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${condition?.title}" required="required" />
    </div>

    <div class="field">
        <label for="${prefix}_description">${message(code:'default.description.label')}</label>
        <input type="text" name="${prefix}_description" id="${prefix}_description" value="${condition?.description}" />
    </div>

    <div class="field required">
        <label for="${prefix}_type">
            ${message(code:'default.type.label')}
            <span style="float:right; font-weight:normal; color:#ee1111">Die Änderung des Typs löscht ALLE typabhängigen Datenfelder</span>
        </label>
        <g:select class="ui dropdown la-not-clearable" id="${prefix}_type" name="${prefix}_type"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      required="required"
                      from="${WfConditionBase.TYPES}"
                      value="${condition?.type}"
                      optionKey="${{ it }}"
                      optionValue="${{ RefdataValue.findByOwnerAndValue( RefdataCategory.findByDesc('workflow.condition.type'), 'type_' + it).getI10n('value') }}" />
    </div>

    %{--<g:if test="${prefix == WfCondition.KEY}">

        <div class="field required">
            <label for="${prefix}_status">Status</label>
            <ui:select class="ui dropdown la-not-clearable" id="${prefix}_status" name="${prefix}_status"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          required="required"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                          value="${condition?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

    </g:if> --}%

    <g:if test="${condition?.getFields()}">

            <div class="ui top attached header" style="background-color: #f9fafb;">
                Typabhängige Datenfelder - Definition / Vorschau
            </div>
            <div class="ui attached segment">
                <g:each in="${condition.getFields()}" var="field">
                    <g:if test="${field.startsWith('checkbox')}">
                        <div class="fields two" style="margin-bottom:0;">
                            <div class="field">
                                <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                                <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}" />
                            </div>
                            <div class="field">
                                <label for="${prefix}_${field}_isTrigger">&nbsp;</label>
                                <div class="ui checkbox">
                                    <input type="checkbox" name="${prefix}_${field}_isTrigger" id="${prefix}_${field}_isTrigger"
                                        <% if (condition?.getProperty(field + '_isTrigger')) { print 'checked="checked"' } %>
                                    />
                                    <label>Soll den Aufgaben-Status auf 'Erledigt' setzen</label>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${field.startsWith('date')}">
                        <div class="fields two" style="margin-bottom:0;">
                            <div class="field">
                                <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                                <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}" />
                            </div>
                            <div class="field">
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${field.startsWith('file')}">
                        <div class="fields two" style="margin-bottom:0;">
                            <div class="field">
                                <label for="${prefix}_${field}_title">Titel für ${condition.getFieldLabel(field)}</label>
                                <input type="text" name="${prefix}_${field}_title" id="${prefix}_${field}_title" value="${condition.getProperty(field + '_title')}" />
                            </div>
                            <div class="field">
                            </div>
                        </div>
                    </g:if>
                </g:each>
            </div>
            <div class="ui bottom attached segment" style="background-color:#f9fafb;">
                <g:each in="${condition.getFields()}" var="field" status="fi">
                    <g:if test="${fi == 0 || fi%2 == 0}">
                        <div class="field">
                            <div class="fields two">
                    </g:if>

                    <g:if test="${field.startsWith('checkbox')}">
                        <div class="field">
                            <label for="${prefix}_${field}">${condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}</label>
                            <div class="ui checkbox">
                                <input type="checkbox" name="${prefix}_${field}" id="${prefix}_${field}"
                                    <% print condition.getProperty(field) == true ? 'checked="checked"' : '' %>
                                />
                                <g:if test="${condition.getProperty(field + '_isTrigger')}">
                                    <label><sup>*</sup>erledigt die Aufgabe</label>
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${field.startsWith('date')}">
                        <div class="field">
                            <label for="${prefix}_${field}">${condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}</label>
                            <input type="date" name="${prefix}_${field}" id="${prefix}_${field}"
                                <% print condition.getProperty(field) ? 'value="' + DateUtils.getSDF_yyyyMMdd().format(condition.getProperty(field)) + '"' : '' %>
                            />
                        </div>
                    </g:if>
                    <g:if test="${field.startsWith('file')}">
                        <div class="field">
                            <label for="${prefix}_${field}">${condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}</label>

                            <g:set var="docctx" value="${condition.getProperty(field)}" />
                            <g:if test="${docctx}">
                                <div class="ui la-flexbox">
                                    <i class="icon file la-list-icon"></i>
                                    <g:link controller="docstore" id="${docctx.owner.uuid}">
                                        <g:if test="${docctx.owner?.title}">
                                            ${docctx.owner.title}
                                        </g:if>
                                        <g:elseif test="${docctx.owner?.filename}">
                                            ${docctx.owner?.filename}
                                        </g:elseif>
                                        <g:else>
                                            ${message(code:'template.documents.missing')}
                                        </g:else>
                                        (${docctx.owner?.type?.getI10n("value")})
                                    </g:link>
                                    <input type="hidden" name="${prefix}_${field}" value="${docctx.id}" />
                                </div>
                            </g:if>
                            <g:else>
                                <input type="text" name="${prefix}_${field}" id="${prefix}_${field}" readonly="readonly" value="Dieses Feld kann hier nicht bearbeitet werden." />
                                %{-- TODO

                                <g:select class="ui dropdown" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          from="${targetObject.documents}"
                                          value="${task.condition.getProperty(field)?.id}"
                                          optionKey="id"
                                          optionValue="${{ (it.owner?.title ? it.owner.title : it.owner?.filename ? it.owner.filename : message(code:'template.documents.missing')) + ' (' + it.owner?.type?.getI10n("value") + ')' }}" />
                                --}%

                            </g:else>
                        </div>
                    </g:if>

                    <g:if test="${fi + 1 == condition.getFields().size() || fi%2 == 1}">
                            </div>
                        </div>
                    </g:if>
                </g:each>
            </div>

    </g:if>

    <g:if test="${prefix == WfConditionPrototype.KEY}">
        <g:if test="${condition}">
            <div class="field">
                <label for="${prefix}_task">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}</label>
                <p>
                    <g:each in="${WfTaskPrototype.findAllByCondition(condition, [sort: 'id'])}" var="task">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + task.id]}">
                            <i class="icon circle blue"></i> ${task.title}
                        </g:link>
                        &nbsp;&nbsp;&nbsp;
                    </g:each>
                </p>
                %{--
                <g:select class="ui dropdown disabled" id="${prefix}_task" name="${prefix}_task"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_taskList}"
                          value="${condition?.task?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
                --}%
            </div>
        </g:if>

        <g:if test="${condition}">
            <div class="field">
                <div class="field">
                    <label>${message(code:'default.lastUpdated.label')}</label>
                    <p>${DateUtils.getLocalizedSDF_noTime().format(condition.lastUpdated)}</p>
    %{--                ${message(code:'default.dateCreated.label')}: ${DateUtils.getLocalizedSDF_noTime().format(condition.dateCreated)}--}%
                </div>
            </div>
        </g:if>
    </g:if>

    <g:if test="${prefix == WfCondition.KEY}">
        %{--
            <div class="field">
                <label for="${prefix}_parent">${message(code: 'workflow.object.' + WfTask.KEY)} <i class="icon angle double up"></i></label>
                <p>${condition?.task?.title}</p>
                <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_taskList}"
                          value="${condition?.task?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />

        </div>
        --}%
    </g:if>

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${condition.id}" />
    </g:if>
    <g:else>
        <input type="hidden" name="cmd" value="${cmd}:${prefix}" />
    </g:else>
    <g:if test="${tab}">
        <input type="hidden" name="tab" value="${tab}" />
    </g:if>
    <g:if test="${info}">
        <input type="hidden" name="info" value="${info}" />
    </g:if>

</g:form>