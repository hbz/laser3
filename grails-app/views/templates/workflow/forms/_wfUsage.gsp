<%@ page import="de.laser.Doc; de.laser.Org; de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.Subscription; de.laser.workflow.WorkflowHelper; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.workflow.*;" %>

<g:form id="wfForm" url="${formUrl}" method="POST" class="ui form">

    %{-- WORKFLOW --}%
    %{-- WORKFLOW --}%

    <g:if test="${prefix == WfWorkflow.KEY}">
        <g:set var="prefixOverride" value="${WfWorkflow.KEY}" />
        <g:set var="wfInfo" value="${workflow.getInfo()}" />

        <div class="field">
%{--            <p><strong>${workflow.title}</strong></p>--}%
            <g:if test="${workflow.description}">
                <p>${workflow.description}</p>
            </g:if>
        </div>
        <div class="field">
            <label>${message(code:'default.relation.label')}</label>
            <div class="ui la-flexbox">
                <i class="icon ${wfInfo.targetIcon} la-list-icon"></i>
                <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">
                    ${wfInfo.targetName}
                </g:link>
            </div>
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

        %{-- <g:if test="${! targetObject}"> currentWorkflows --}%

        <div class="field">
            <label>Offene Aufgaben</label>

            <div class="ui segment vertically divided grid" style="box-shadow:none">
                <g:set var="tasks" value="${workflow.getSequence()}" />
                <% int openTasksCount = 0 %>

                <g:each in="${tasks}" var="task" status="ti">
                    <g:if test="${task.status == RDStore.WF_TASK_STATUS_OPEN}">
                        <% openTasksCount++ %>
                        <div class="row">
                            <div class="one wide column wf-centered">
                                <span class="sc_darkgrey">
                                    <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                </span>
                            </div>
                            <div class="fifteen wide column">
                                <div class="content">
                                    <div class="header">
                                        <strong>${task.title}</strong>
                                    </div>
                                    <div class="description" style="margin:1em 0 0 0">
                                        ${task.description}
                                    </div>
                                    <g:if test="${task.comment}">
                                        <div style="margin: 1em 1em 0 1em; padding-left: 1em; border-left: 5px solid #E0E0E0; font-style: italic;">
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
                                                <uiWorkflow:taskConditionField condition="${task.condition}" field="${field}" />
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
                            <g:if test="${child.status == RDStore.WF_TASK_STATUS_OPEN}">
                                <% openTasksCount++ %>
                                <div class="row">
                                    <div class="one wide column wf-centered">
                                        <span class="sc_darkgrey">
                                            <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                        </span>
                                    </div>
                                    <div class="fifteen wide column">
                                        <div class="content">
                                            <div class="header">
                                                <strong>${child.title}</strong>
                                            </div>
                                            <div class="description" style="margin:1em 0 0 0">
                                                ${child.description}
                                            </div>
                                            <g:if test="${child.comment}">
                                                <div style="margin: 1em 1em 0 1em; padding-left: 1em; border-left: 5px solid #E0E0E0; font-style: italic;">
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
                                                        <uiWorkflow:taskConditionField condition="${child.condition}" field="${field}" />
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

                <g:if test="${openTasksCount == 0}">
                    <div class="row">
                        <div class="sixteen wide column">
                            <g:if test="${tasks}">
                                <i class="icon check"></i> Es sind keine offenen Aufgaben mehr vorhanden.
                            </g:if>
                            <g:else>
                                Es sind keine Aufgaben vorhanden.
                            </g:else>
                        </div>
                    </div>
                </g:if>
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

    <g:elseif test="${prefix == WfTask.KEY}">
        <g:set var="prefixOverride" value="${WfTask.KEY}" />
        <g:set var="wfInfo" value="${task.getWorkflow()?.getInfo()}" />

        <div class="field">
%{--            <p><strong>${task.title}</strong></p>--}%
            <g:if test="${task.description}">
                <p>${task.description}</p>
            </g:if>
            <p>
                <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)} sc_darkgrey"></i>
                ${task.priority.getI10n('value')}
            </p>
%{--            <g:if test="${task.getParent()}"> <!-- TODO -->--}%
%{--                <p><i class="icon paperclip"></i> Ist eine Unteraufgabe von: ${task.getParent().title}</p>--}%
%{--            </g:if>--}%
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
%{--                <label>${message(code:'workflow.condition.label')}</label>--}%
                <label>${task.condition.title}</label>

                <div class="ui segment" style="background-color:#f9fafb; margin-top:0; box-shadow:none;">
                    <div class="field">
%{--                        <p><strong>${task.condition.title}</strong></p>--}%
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
                                    <div id="fileUploadWrapper_toggle_${field}" class="ui small buttons" style="float:right; margin-right:10px">
                                        <span data-position="top right" class="ui left attached button active la-popup-tooltip la-delay" data-content="${message(code:'workflow.condition.file.info')}">
                                            <i class="icon file"></i> &nbsp;
                                        </span>
                                        <span data-position="top right" class="ui right attached button la-popup-tooltip la-delay" data-content="${message(code:'workflow.condition.fileUpload.info')}">
                                            &nbsp; <i class="icon paperclip"></i>
                                        </span>
                                    </div>
                                </label>
                                <g:set var="docctx" value="${task.condition.getProperty(field)}" />
                                %{-- <g:if test="${docctx}">
                                    <g:link controller="docstore" id="${docctx.owner.uuid}">
                                        <i class="icon file"></i>
                                    </g:link>
                                </g:if> --}%
                                <div id="fileUploadWrapper_dropdown_${field}" class="ui segment" style="box-shadow:none">
                                    <div class="field">
                                        <g:if test="${workflow}"> %{-- currentWorkflows --}%
                                            <g:set var="targetDocuments" value="${wfInfo.target.documents.findAll{ it.status != RDStore.DOC_CTX_STATUS_DELETED && it.owner.contentType == Doc.CONTENT_TYPE_FILE }}" />
                                            <g:select class="ui dropdown" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                                      from="${targetDocuments}"
                                                      value="${task.condition.getProperty(field)?.id}"
                                                      optionKey="${{it.id}}"
                                                      optionValue="${{ (it.owner?.title ? it.owner.title : it.owner?.filename ? it.owner.filename : message(code:'template.documents.missing')) + ' (' + it.owner?.type?.getI10n("value") + ')' }}" />
                                        </g:if>
                                        <g:else>
                                            <g:set var="targetDocuments" value="${targetObject.documents.findAll{ it.status != RDStore.DOC_CTX_STATUS_DELETED && it.owner.contentType == Doc.CONTENT_TYPE_FILE }}" />
                                            <g:select class="ui dropdown" id="${prefixOverride}_${field}" name="${prefixOverride}_${field}"
                                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                                      from="${targetDocuments}"
                                                      value="${task.condition.getProperty(field)?.id}"
                                                      optionKey="${{it.id}}"
                                                      optionValue="${{ (it.owner?.title ? it.owner.title : it.owner?.filename ? it.owner.filename : message(code:'template.documents.missing')) + ' (' + it.owner?.type?.getI10n("value") + ')' }}" />

                                        </g:else>
                                    </div>
                                </div>
                                <div id="fileUploadWrapper_upload_${field}" class="ui segment" style="box-shadow:none;display:none">
                                    %{--<g:form class="ui form" url="${formUrl}" method="post" enctype="multipart/form-data">--}%

                                    <g:if test="${workflow}"> %{-- currentWorkflows --}%
                                        <input type="hidden" name="wfUploadOwner_${field}" value="${wfInfo.target.class.name}:${wfInfo.target.id}"/>
                                    </g:if>
                                    <g:else>
                                        <input type="hidden" name="wfUploadOwner_${field}" value="${targetObject.class.name}:${targetObject.id}"/>
                                    </g:else>
                                        <label for="wfUploadTitle_${field}" >${message(code: 'template.addDocument.name')}:</label>
                                        <input type="text" id="wfUploadTitle_${field}" name="wfUploadTitle_${field}" />

                                        <label>${message(code: 'template.addDocument.type')}:</label>
                                        <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE) - [RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT, RDStore.DOC_TYPE_ONIXPL]}"
                                                  class="ui dropdown fluid"
                                                  optionKey="id"
                                                  optionValue="${{ it.getI10n('value') }}"
                                                  name="wfUploadDoctype_${field}"
                                                  />

                                        <g:if test="${(workflow && wfInfo.target instanceof Org) || (targetObject && targetObject instanceof Org)}">
                                            <label>${message(code:'template.addDocument.shareConf')}</label>
                                            <g:select from="${[RDStore.SHARE_CONF_UPLOADER_ORG, RDStore.SHARE_CONF_UPLOADER_AND_TARGET, RDStore.SHARE_CONF_ALL]}"
                                                      class="ui dropdown fluid la-not-clearable"
                                                      optionKey="id"
                                                      optionValue="${{ it.getI10n('value') }}"
                                                      name="wfUploadShareConf_${field}"
                                            />
                                        </g:if>

                                        <label for="wfUploadFile_placeholder_${field}">${message(code: 'template.addDocument.file')}:</label>
                                        <div class="ui fluid action input">
                                            <input type="text" id="wfUploadFile_placeholder_${field}" name="wfUploadFile_placeholder_${field}" readonly="readonly" placeholder="${message(code:'template.addDocument.selectFile')}">
                                            <input type="file" id="wfUploadFile_file_${field}" name="wfUploadFile_file_${field}" style="display: none;">
                                            <div id="wfUploadFile_button_${field}" class="ui icon button" style="padding-left:30px; padding-right:30px">
                                                <i class="attach icon"></i>
                                            </div>
                                        </div>

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

            <laser:script file="${this.getGroovyPageFileName()}">
                $('[id^=fileUploadWrapper_toggle]').click( function(e) {
                    e.preventDefault();
                    var id = $(this).attr('id').split('_')[2];
                    $('#fileUploadWrapper_upload_' + id + ', #fileUploadWrapper_dropdown_' + id).toggle();
                    $('#fileUploadWrapper_toggle_' + id+ ' .button').toggleClass('active');
                });
                $('[id^=wfUploadFile]').on('change', function(e) {
                    var id = $(this).attr('id').split('_')[2];
                    var name = e.target.files[0].name;
                    $('#wfUploadTitle_' + id).val(name);
                    $('#wfUploadFile_placeholder_' + id).val(name);
                });
                $('[id^=wfUploadFile_button]').click( function() {
                    var id = $(this).attr('id').split('_')[2];
                    $('#wfUploadFile_file_' + id).click();
                });
                $('#wfForm').on('submit', function(e) {
                    if ( $('#fileUploadWrapper_dropdown_file1').css('display') == 'none' ) {
                        $('#fileUploadWrapper_dropdown_file1').remove();
                        $(this).attr('enctype', 'multipart/form-data')
                    }
                    if ( $('#fileUploadWrapper_upload_file1').css('display') == 'none' ) {
                        $('#fileUploadWrapper_upload_file1').remove();
                    }
                    if ( $('#fileUploadWrapper_dropdown_file2').css('display') == 'none' ) {
                        $('#fileUploadWrapper_dropdown_file2').remove();
                        $(this).attr('enctype', 'multipart/form-data')
                    }
                    if ( $('#fileUploadWrapper_upload_file2').css('display') == 'none' ) {
                        $('#fileUploadWrapper_upload_file2').remove();
                    }
                });
            </laser:script>
        </g:if>

        <g:if test="${info}">
            <input type="hidden" name="info" value="${info}" />
        </g:if>
        <input type="hidden" name="cmd" value="usage:${prefix}:${task.id}" />
    </g:elseif>
 </g:form>



