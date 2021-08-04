<%@ page import="de.laser.helper.WorkflowHelper; de.laser.helper.DateUtils; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form controller="myInstitution" action="currentWorkflows" method="POST" class="ui form">

    <g:if test="${workflow}">
        <g:set var="prefixOverride" value="${WfWorkflow.KEY}" />

        <div class="field">
            <p><strong>${workflow.title}</strong></p>
            <p>${workflow.description}</p>
        </div>

        <div class="field">
            <div class="field">
                <label for="${prefixOverride}_status">Status</label>
                <laser:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                              value="${workflow.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>
        </div>

        <div class="ui segments">
            <g:set var="tasks" value="${workflow.getSequence()}" />
            <g:each in="${tasks}" var="task" status="ti">
                <div class="ui horizontal segments">
                    <div class="ui segment">
                        <div class="content">
                            <div class="header">
                                <i class="ui icon ${WorkflowHelper.getCssIconAndColorByStatus(task.status)}"></i> ${task.title}
                                <span style="margin-left: 1em; color: darkgrey; font-weight: normal">(
                                    <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                    ${task.priority.getI10n('value')}
                                    )</span>
                            </div>
                            <div class="description">${task.description}</div>
                        </div>
                    </div>
                    <div class="ui segment">
                        <g:if test="${task.condition}">

                            <div class="content">
                                <div class="header">
                                    <i class="ui icon caret right grey"></i> ${task.condition.title}
                                </div>
                                <div class="description">
                                    ${task.condition.description} <br />
                                    <!-- -->
                                    <g:each in="${task.condition.getFields()}" var="field" status="fi">
                                        <br/> - ${task.condition.getProperty(field + '_title')}:

                                        <g:if test="${field.startsWith('checkbox')}">
                                            <g:if test="${task.condition.getProperty(field) == true}">
                                                <i class="ui icon green check square outline"></i>
                                            </g:if>
                                            <g:else>
                                                <i class="ui icon square outline"></i>
                                            </g:else>
                                        </g:if>
                                        <g:if test="${field.startsWith('date') && task.condition.getProperty(field)}">
                                            ${DateUtils.getSDF_NoTime().format(task.condition.getProperty(field))}
                                        </g:if>
                                    </g:each>
                                    <!-- -->
                                </div>
                            </div>

                        </g:if>
                    </div>
                </div>

                <g:if test="${task.child}">
                    <g:each in="${task.child.getSequence()}" var="child" status="ci">
                        <div class="ui horizontal segments">
                            <div class="ui segment">
                                <div class="content">
                                    <div class="header">
                                        <i class="ui icon ${WorkflowHelper.getCssIconAndColorByStatus(child.status)}"></i> ${child.title}
                                        <span style="margin-left: 1em; color: darkgrey; font-weight: normal">(
                                            <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                            ${child.priority.getI10n('value')}
                                            )</span>
                                    </div>
                                    <div class="description">${child.description}</div>
                                </div>
                            </div>
                            <div class="ui segment">
                                <g:if test="${child.condition}">

                                    <div class="content">
                                        <div class="header">
                                            <i class="ui icon caret right grey"></i> ${child.condition.title}
                                        </div>
                                        <div class="description">
                                            ${child.condition.description} <br />
                                            <!-- -->
                                            <g:each in="${child.condition.getFields()}" var="field" status="fi">
                                                <br/> - ${child.condition.getProperty(field + '_title')}:

                                                <g:if test="${field.startsWith('checkbox')}">
                                                    <g:if test="${child.condition.getProperty(field) == true}">
                                                        <i class="ui icon green check square outline"></i>
                                                    </g:if>
                                                    <g:else>
                                                        <i class="ui icon square outline"></i>
                                                    </g:else>
                                                </g:if>
                                                <g:if test="${field.startsWith('date') && child.condition.getProperty(field)}">
                                                    ${DateUtils.getSDF_NoTime().format(child.condition.getProperty(field))}
                                                </g:if>
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

        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${workflow.id}" />
    </g:if>


    <g:if test="${task}">
        <g:set var="prefixOverride" value="${WfTask.KEY}" />

        <div class="field">
            <p><strong>${task.title}</strong></p>
            <p>${task.description}</p>
        </div>

        <div class="field">
            <div class="fields two">
                <div class="field">
                    <label for="${prefixOverride}_priority">Priorität</label>
                    <p>
                        <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                        ${task.priority.getI10n('value')}
                    </p>
                </div>
                <div class="field">
                    <label for="${prefixOverride}_status">Status</label>
                    <laser:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                                  from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_STATUS )}"
                                  value="${task.status?.id}"
                                  optionKey="id"
                                  optionValue="value" />
                </div>
            </div>
        </div>

        <div class="field">
            <label for="${prefixOverride}_comment">Kommentar</label>
            <textarea id="${prefixOverride}_comment" name="${prefixOverride}_comment" rows="2">${task.comment}</textarea>
        </div>

        <g:if test="${task.condition}">
            <g:set var="prefixOverride" value="${WfCondition.KEY}" />

            <div class="field">
                <label>Bedingung</label>

                <div class="ui segment">
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
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title')}</label>
                                <div class="ui checkbox">
                                    <input type="checkbox" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                        <% print task.condition.getProperty(field) == true ? 'checked="checked"' : '' %>
                                    />
                                    <g:if test="${task.condition.getProperty(field + '_isTrigger')}">
                                        <label>Angabe ändert Aufgaben-Status</label>
                                    </g:if>
                                </div>
                            </div>
                        </g:if>
                        <g:if test="${field.startsWith('date')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title')}</label>
                                <input type="date" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                    <% print task.condition.getProperty(field) ? 'value="' + DateUtils.getSDF_ymd().format(task.condition.getProperty(field)) + '"' : '' %>
                                />
                            </div>
                        </g:if>

                        <g:if test="${fi + 1 == task.condition.getFields().size() || fi%2 == 1}">
                                </div>
                            </div>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </g:if>

        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${task.id}" />
    </g:if>

 </g:form>



