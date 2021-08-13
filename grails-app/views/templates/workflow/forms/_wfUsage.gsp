<%@ page import="de.laser.Subscription; de.laser.helper.WorkflowHelper; de.laser.helper.DateUtils; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form url="${formUrl}" method="POST" class="ui form">

    %{-- WORKFLOW --}%
    %{-- WORKFLOW --}%

    <g:if test="${prefix == WfWorkflow.KEY}">
        <g:set var="prefixOverride" value="${WfWorkflow.KEY}" />

        <div class="field">
            <p><strong>${workflow.title}</strong></p>
            <p>${workflow.description}</p>
            <p>
                <g:link controller="subscription" action="show" params="${[id: workflow.subscription.id]}">
                    <i class="icon clipboard"></i>${workflow.subscription.name}
                </g:link>
            </p>
        </div>

        <div class="field">
            <div class="field">
                <label for="${prefixOverride}_status">${message(code:'default.status.label')}</label>
                <laser:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                              value="${workflow.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>
        </div>

        <g:if test="${! subscription}"> %{-- currentWorkflows --}%

            <div class="ui segment internally celled grid">
                <g:set var="tasks" value="${workflow.getSequence()}" />
                <g:each in="${tasks}" var="task" status="ti">
                    <div class="row">
                        <div class="eight wide column">
                            <div class="content">
                                <div class="header">
                                    <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(task.status)}"></i>
                                    <strong>${task.title}</strong>
                                    <span style="color: darkgrey">
                                    ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i> ${task.priority.getI10n('value')} )
                                </span>
                                </div>
                                <div class="description" style="margin:1em 0 0 0">
                                    ${task.description}
                                </div>
                            </div>
                        </div>
                        <div class="eight wide column">
                            <g:if test="${task.condition}">

                                <div class="content">
                                    <div class="header">
                                        <strong>${task.condition.title}</strong>
                                    </div>
                                    <div class="description">
                                        ${task.condition.description} <br />
                                        <!-- -->
                                        <g:each in="${task.condition.getFields()}" var="field" status="fi">

                                            <g:if test="${field.startsWith('checkbox')}">
                                                <br/>${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                <g:if test="${task.condition.getProperty(field) == true}">
                                                    <i class="icon check square outline"></i>
                                                </g:if>
                                                <g:else>
                                                    <i class="icon square outline"></i>
                                                </g:else>
                                            </g:if>
                                            <g:elseif test="${field.startsWith('date')}">
                                                <br/>${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                <g:if test="${task.condition.getProperty(field)}">
                                                    ${DateUtils.getSDF_NoTime().format(task.condition.getProperty(field))}
                                                </g:if>
                                                <g:else>
                                                    -
                                                </g:else>
                                            </g:elseif>
                                            <g:elseif test="${field.startsWith('file')}">
                                                <br/>${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                <g:set var="docctx" value="${task.condition.getProperty(field)}" />
                                                <g:if test="${docctx}">
                                                    <g:link controller="docstore" id="${docctx.owner.uuid}">
                                                        <i class="icon file"></i>
                                                        <g:if test="${docctx.owner?.title}">
                                                            ${docctx.owner.title}
                                                        </g:if>
                                                        <g:elseif test="${docctx.owner?.filename}">
                                                            ${docctx.owner?.filename}
                                                        </g:elseif>
                                                        <g:else>
                                                            ${message(code:'template.documents.missing')}
                                                        </g:else>
                                                    </g:link> (${docctx.owner?.type?.getI10n("value")})
                                                </g:if>
                                                <g:else>
                                                    -
                                                </g:else>
                                            </g:elseif>
                                        </g:each>
                                        <!-- -->
                                    </div>
                                </div>

                            </g:if>
                        </div>
                    </div>

                    <g:if test="${task.child}">
                        <g:each in="${task.child.getSequence()}" var="child" status="ci">
                            <div class="row">
                                <div class="eight wide column">
                                    <div class="content">
                                        <div class="header">
                                            <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(child.status)}"></i>
                                            <strong>${child.title}</strong>
                                            <span style="color: darkgrey">
                                            ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i> ${child.priority.getI10n('value')} )
                                        </span>
                                        </div>
                                        <div class="description" style="margin:1em 0 0 0">
                                            ${child.description}
                                        </div>
                                    </div>
                                </div>
                                <div class="eight wide column">
                                    <g:if test="${child.condition}">

                                        <div class="content">
                                            <div class="header">
                                                <strong>${child.condition.title}</strong>
                                            </div>
                                            <div class="description">
                                                ${child.condition.description} <br />
                                                <!-- -->
                                                <g:each in="${child.condition.getFields()}" var="field" status="fi">

                                                    <g:if test="${field.startsWith('checkbox')}">
                                                        <br/>${child.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                        <g:if test="${child.condition.getProperty(field) == true}">
                                                            <i class="icon check square outline"></i>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="icon square outline"></i>
                                                        </g:else>
                                                    </g:if>
                                                    <g:elseif test="${field.startsWith('date')}">
                                                        <br/>${child.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                        <g:if test="${child.condition.getProperty(field)}">
                                                            ${DateUtils.getSDF_NoTime().format(child.condition.getProperty(field))}
                                                        </g:if>
                                                        <g:else>
                                                            -
                                                        </g:else>
                                                    </g:elseif>
                                                    <g:elseif test="${field.startsWith('file')}">
                                                        <br/>${child.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}:
                                                        <g:set var="docctx" value="${child.condition.getProperty(field)}" />
                                                        <g:if test="${docctx}">
                                                            <g:link controller="docstore" id="${docctx.owner.uuid}">
                                                                <i class="icon file"></i>
                                                                <g:if test="${docctx.owner?.title}">
                                                                    ${docctx.owner.title}
                                                                </g:if>
                                                                <g:elseif test="${docctx.owner?.filename}">
                                                                    ${docctx.owner?.filename}
                                                                </g:elseif>
                                                                <g:else>
                                                                    ${message(code:'template.documents.missing')}
                                                                </g:else>
                                                            </g:link> (${docctx.owner?.type?.getI10n("value")})
                                                        </g:if>
                                                        <g:else>
                                                            -
                                                        </g:else>
                                                    </g:elseif>
                                                </g:each>
                                                <!-- -->
                                            </div>
                                        </div>

                                    </g:if>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                </g:each>
            </div>

        </g:if>
        <input type="hidden" name="cmd" value="usage:${prefix}:${workflow.id}" />
    </g:if>

    %{-- TASK --}%
    %{-- TASK --}%

    <g:if test="${prefix == WfTask.KEY}">
        <g:set var="prefixOverride" value="${WfTask.KEY}" />

        <div class="field">
            <p><strong>${task.title}</strong></p>
            <p>${task.description}</p>
        </div>

        <div class="field">
            <div class="fields two">
                <div class="field">
                    <label for="${prefixOverride}_priority">${message(code:'default.priority.label')}</label>
                    <p>
                        <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                        ${task.priority.getI10n('value')}
                    </p>
                </div>
                <div class="field">
                    <label for="${prefixOverride}_status">${message(code:'default.status.label')}</label>
                    <laser:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                                  from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_STATUS )}"
                                  value="${task.status?.id}"
                                  optionKey="id"
                                  optionValue="value" />
                </div>
            </div>
        </div>

        <div class="field">
            <label for="${prefixOverride}_comment">${message(code:'default.comment.label')}</label>
            <textarea id="${prefixOverride}_comment" name="${prefixOverride}_comment" rows="2">${task.comment}</textarea>
        </div>

        <g:if test="${task.condition}">
            <g:set var="prefixOverride" value="${WfCondition.KEY}" />

            <div class="field">
                <label>${message(code:'workflow.condition.label')}</label>

                <div class="ui segment" style="background-color:#f9fafb; margin-top:0;">
                    <div class="field">
                        <p><strong>${task.condition.title}</strong></p>
                        <p>${task.condition.description}</p>
                    </div>

                    <g:each in="${task.condition.getFields()}" var="field" status="fi">
                        <g:if test="${fi == 0 || fi%2 == 0}">
                            <div class="field">
                                <div class="fields two">
                        </g:if>

                        <g:if test="${field.startsWith('checkbox')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}</label>
                                <div class="ui checkbox">
                                    <input type="checkbox" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                        <% print task.condition.getProperty(field) == true ? 'checked="checked"' : '' %>
                                    />
                                    <g:if test="${task.condition.getProperty(field + '_isTrigger')}">
                                        <label>* Dieser Status ändert den Aufgaben-Status</label>
                                    </g:if>
                                </div>
                            </div>
                        </g:if>
                        <g:elseif test="${field.startsWith('date')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}</label>
                                <input type="date" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                    <% print task.condition.getProperty(field) ? 'value="' + DateUtils.getSDF_ymd().format(task.condition.getProperty(field)) + '"' : '' %>
                                />
                            </div>
                        </g:elseif>
                        <g:elseif test="${field.startsWith('file')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: 'Feld ohne Titel'}</label>
                                <g:set var="docctx" value="${task.condition.getProperty(field)}" />
                                %{-- <g:if test="${docctx}">
                                    <g:link controller="docstore" id="${docctx.owner.uuid}">
                                        <i class="icon file"></i>
                                    </g:link>
                                </g:if> --}%


                                <g:if test="${workflow}"> %{-- currentWorkflows --}%
                                    <g:select class="ui dropdown" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                                              from="${workflow.subscription.documents}"
                                              value="${task.condition.getProperty(field)?.id}"
                                              optionKey="id"
                                              optionValue="${{ (it.owner?.title ? it.owner.title : it.owner?.filename ? it.owner.filename : message(code:'template.documents.missing')) + ' (' + it.owner?.type?.getI10n("value") + ')' }}" />

                                </g:if>
                                <g:else>
                                    <g:select class="ui dropdown" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                                              from="${subscription.documents}"
                                              value="${task.condition.getProperty(field)?.id}"
                                              optionKey="id"
                                              optionValue="${{ (it.owner?.title ? it.owner.title : it.owner?.filename ? it.owner.filename : message(code:'template.documents.missing')) + ' (' + it.owner?.type?.getI10n("value") + ')' }}" />
                                </g:else>



                            </div>
                        </g:elseif>

                        <g:if test="${fi + 1 == task.condition.getFields().size() || fi%2 == 1}">
                                </div>
                            </div>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </g:if>

        <input type="hidden" name="cmd" value="usage:${prefix}:${task.id}" />
    </g:if>

 </g:form>



