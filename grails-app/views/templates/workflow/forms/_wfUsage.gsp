<%@ page import="de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.Subscription; de.laser.workflow.WorkflowHelper; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form id="wfForm" url="${formUrl}" method="POST" class="ui form">

    %{-- WORKFLOW --}%
    %{-- WORKFLOW --}%

    <g:if test="${prefix == WfWorkflow.KEY}">
        <g:set var="prefixOverride" value="${WfWorkflow.KEY}" />

        <div class="field">
            <p><strong>${workflow.title}</strong></p>
            <p>${workflow.description}</p>
            <p>
                <div class="ui la-flexbox">
                    <i class="icon clipboard la-list-icon"></i>
                    <g:link controller="subscription" action="show" params="${[id: workflow.subscription.id]}">
                        ${workflow.subscription.name}
                    </g:link>
                </div>
            </p>
        </div>

        <div class="field">
            <label for="${prefixOverride}_comment">${message(code:'default.comment.label')}</label>
            <textarea id="${prefixOverride}_comment" name="${prefixOverride}_comment" rows="2">${workflow.comment}</textarea>
        </div>

        <div class="field">
            <div class="field">
                <label for="${prefixOverride}_status">${message(code:'default.status.label')}</label>
                <ui:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                              value="${workflow.status?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>
        </div>

        %{-- <g:if test="${! subscription}"> currentWorkflows --}%

        <div class="field">
            <label>Offene Aufgaben</label>

            <div class="ui segment internally celled grid">
                <g:set var="tasks" value="${workflow.getSequence()}" />
                <g:each in="${tasks}" var="task" status="ti">
                    <g:if test="${task.status == RDStore.WF_TASK_STATUS_OPEN}">
                        <div class="row">
                            <div class="sixteen wide column">
                                <div class="content">
                                    <div class="header">
                                        %{-- <i class="icon ${WorkflowHelper.getCssIconAndColorByStatus(task.status)}"></i> --}%
                                        <strong>${task.title}</strong>
                                        <span style="color: darkgrey">
                                        ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i> ${task.priority.getI10n('value')} )
                                    </span>
                                    </div>
                                    <div class="description" style="margin:1em 0 0 0">
                                        ${task.description}
                                    </div>
                                    <g:if test="${task.comment}">
                                        <div style="margin: 1em; padding-left: 1em; border-left: 5px solid #E0E0E0; font-style: italic;">
                                            ${task.comment}
                                        </div>
                                    </g:if>
                                </div>
                            </div>
                            %{--
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
                                                <br />
                                                <workflow:taskConditionField condition="${task.condition}" field="${field}" />
                                            </g:each>
                                            <!-- -->
                                        </div>
                                    </div>

                                </g:if>
                            </div>
                            --}%
                        </div>
                    </g:if>

                    <g:if test="${task.child}">
                        <g:each in="${task.child.getSequence()}" var="child" status="ci">
                            <g:if test="${task.child.status == RDStore.WF_TASK_STATUS_OPEN}">
                                <div class="row">
                                    <div class="sixteen wide column">
                                        <div class="content">
                                            <div class="header">
                                                %{-- <i class="icon ${WorkflowHelper.getCssIconAndColorByStatus(child.status)}"></i> --}%
                                                <strong>${child.title}</strong>
                                                <span style="color: darkgrey">
                                                ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i> ${child.priority.getI10n('value')} )
                                            </span>
                                            </div>
                                            <div class="description" style="margin:1em 0 0 0">
                                                ${child.description}
                                            </div>
                                            <g:if test="${child.comment}">
                                                <div style="margin: 1em; padding-left: 1em; border-left: 5px solid #E0E0E0; font-style: italic;">
                                                    ${child.comment}
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>
                                    %{--
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
                                                        <br />
                                                        <workflow:taskConditionField condition="${child.condition}" field="${field}" />
                                                    </g:each>
                                                    <!-- -->
                                                </div>
                                            </div>

                                        </g:if>
                                    </div>
                                    --}%
                                </div>
                            </g:if>
                        </g:each>
                    </g:if>
                </g:each>
            </div>

        </div>
        %{--</g:if>--}%
        <g:if test="${info}">
            <input type="hidden" name="info" value="${info}" />
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
            <p>
                <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}" style="color:darkgrey"></i>
                ${task.priority.getI10n('value')}
            </p>
        </div>

        <div class="field">
            <label for="${prefixOverride}_comment">${message(code:'default.comment.label')}</label>
            <textarea id="${prefixOverride}_comment" name="${prefixOverride}_comment" rows="2">${task.comment}</textarea>
        </div>

        <div class="field">
            <label for="${prefixOverride}_status">${message(code:'default.status.label')}</label>
            <ui:select class="ui dropdown la-not-clearable" id="${prefixOverride}_status" name="${prefixOverride}_status"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_STATUS )}"
                          value="${task.status?.id}"
                          optionKey="id"
                          optionValue="value" />
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
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}</label>
                                <div class="ui checkbox">
                                    <input type="checkbox" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                        <% print task.condition.getProperty(field) == true ? 'checked="checked"' : '' %>
                                    />
                                    <g:if test="${task.condition.getProperty(field + '_isTrigger')}">
                                        <label><sup>*</sup>erledigt die Aufgabe</label>
                                    </g:if>
                                </div>
                            </div>
                        </g:if>
                        <g:elseif test="${field.startsWith('date')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}</label>
                                <input type="date" name="${prefixOverride}_${field}" id="${prefixOverride}_${field}"
                                    <% print task.condition.getProperty(field) ? 'value="' + DateUtils.getSDF_yyyyMMdd().format(task.condition.getProperty(field)) + '"' : '' %>
                                />
                            </div>
                        </g:elseif>
                        <g:elseif test="${field.startsWith('file')}">
                            <div class="field">
                                <label for="${prefixOverride}_${field}">${task.condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label')}
                                    <a id="fileUploadWrapper_toggle" href="#" style="float:right">
                                        <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.condition.file.info')}">
                                            <span class="ui active label">${message(code:'default.select.label')}</span>
                                        </span> &hArr;
                                        <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.condition.fileUpload.info')}">
                                            <span class="ui label">${message(code:'default.newFile.label')}</span>
                                        </span>
                                    </a>
                                </label>
                                <g:set var="docctx" value="${task.condition.getProperty(field)}" />
                                %{-- <g:if test="${docctx}">
                                    <g:link controller="docstore" id="${docctx.owner.uuid}">
                                        <i class="icon file"></i>
                                    </g:link>
                                </g:if> --}%
                                <div id="fileUploadWrapper_dropdown" class="fileUploadWrapper field">
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
                                <div id="fileUploadWrapper_upload" class="fileUploadWrapper ui segment" style="margin:8px 0 0 0; display:none;">
                                    %{--<g:form class="ui form" url="${formUrl}" method="post" enctype="multipart/form-data">--}%

                                    <g:if test="${workflow}"> %{-- currentWorkflows --}%
                                        <input type="hidden" name="wfUploadOwner" value="${workflow.subscription.class.name}:${workflow.subscription.id}"/>
                                    </g:if>
                                    <g:else>
                                        <input type="hidden" name="wfUploadOwner" value="${subscription.class.name}:${subscription.id}"/>
                                    </g:else>
                                        <label for="wfUploadTitle" >${message(code: 'template.addDocument.name')}:</label>
                                        <input type="text" id="wfUploadTitle" name="wfUploadTitle" />

                                        <label>${message(code: 'template.addDocument.type')}:</label>
                                        <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE) - [RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT, RDStore.DOC_TYPE_ONIXPL]}"
                                                  class="ui dropdown fluid"
                                                  optionKey="id"
                                                  optionValue="${{ it.getI10n('value') }}"
                                                  name="wfUploadDoctype"
                                                  />

                                        <label for="wfUploadFile-placeholder">${message(code: 'template.addDocument.file')}:</label>

                                        <div class="ui fluid action input">
                                            <input type="text" id="wfUploadFile-placeholder" name="wfUploadFile-placeholder" readonly="readonly" placeholder="${message(code:'template.addDocument.selectFile')}">
                                            <input type="file" id="wfUploadFile" name="wfUploadFile" style="display: none;">
                                            <div id="wfUploadFile-button" class="ui icon button" style="padding-left:30px; padding-right:30px">
                                                <i class="attach icon"></i>
                                            </div>
                                        </div>

                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            $('#fileUploadWrapper_toggle').click( function(e) {
                                                e.preventDefault();
                                                $('.fileUploadWrapper').toggle();
                                                $('#fileUploadWrapper_toggle .label').toggleClass('active');
                                            });
                                            $('#wfUploadFile').on('change', function(e) {
                                                var name = e.target.files[0].name;
                                                $('#wfUploadTitle').val(name);
                                                $('#wfUploadFile-placeholder').val(name);
                                            });
                                            $('#wfUploadFile-button').click( function() {
                                                $('#wfUploadFile').click();
                                            });
                                            $('#wfForm').on('submit', function(e) {
                                                if ( $('#fileUploadWrapper_dropdown').css('display') == 'none' ) {
                                                    $('#fileUploadWrapper_dropdown').remove();
                                                    $(this).attr('enctype', 'multipart/form-data')
                                                }
                                                if ( $('#fileUploadWrapper_upload').css('display') == 'none' ) {
                                                    $('#fileUploadWrapper_upload').remove();
                                                }
                                            });
                                        </laser:script>
                                    %{--</g:form>--}%
                                </div>
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

        <g:if test="${info}">
            <input type="hidden" name="info" value="${info}" />
        </g:if>
        <input type="hidden" name="cmd" value="usage:${prefix}:${task.id}" />
    </g:if>
 </g:form>



