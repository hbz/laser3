<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.workflow.WorkflowHelper; de.laser.workflow.WfCheckpoint; de.laser.workflow.WfChecklist; de.laser.WorkflowService; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:if test="${clist}">
    <%
        boolean checkedEditable = workflowService.hasUserPerm_edit()
    %>

    <g:set var="clistInfo" value="${clist.getInfo()}" />

    <div class="ui header center aligned">
        ${clist.title}

        <g:if test="${checkedEditable}">
            <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
                <div class="ui success message" style="margin-top:1em;text-align:left;font-size:14px;font-weight:normal;">
                    ${message(code: 'workflow.edit.ok')}
                </div>
            </g:if>
            <g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
                <div class="ui error message" style="margin-top:1em;text-align:left;font-size:14px;font-weight:normal;">
                    ${message(code: 'workflow.edit.error')}
                </div>
            </g:elseif>
            <g:else>
                <div class="ui message info" style="margin-top:1em;text-align:left;font-size:14px;font-weight:normal;">
                    <div class="content">
                        <i class="hand point right outline icon"></i>
                        Ihre Änderungen werden direkt/ohne explizites Speichern wirksam.
                        <br />
                        <i class="hand point right outline icon"></i>
                        Bei einer Kopie werden Informationen aus den Feldern
                        <strong>Kommentar</strong>, <strong>Vorlage</strong>, <strong>Aufgabe erledigt</strong> und <strong>Datumsangabe</strong>
                        nicht übernommen.
                    </div>
                </div>
            </g:else>
        </g:if>
    </div>

    <div class="content">

            <div class="ui vertically divided grid">

                <div class="row">
                    <div class="two wide column wf-centered">
                        <uiWorkflow:statusIcon checklist="${clist}" size="big" />
                    </div>
                    <div class="ten wide column">

                        <div class="la-inline-lists">
                            <dl>
                                <dt>${message(code: 'workflow.label')}</dt>
                                <dd>
                                    <div class="ui header">
                                        <ui:xEditable overwriteEditable="${checkedEditable}" owner="${clist}" field="title" type="text" />
                                    </div>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'default.description.label')}</dt>
                                <dd>
                                    <ui:xEditable overwriteEditable="${checkedEditable}" owner="${clist}" field="description" type="textarea" />
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'default.comment.label')}</dt>
                                <dd>
                                    <ui:xEditable overwriteEditable="${checkedEditable}" owner="${clist}" field="comment" type="textarea" />
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'workflow.template')}</dt>
                                <dd>
                                    <ui:xEditableBoolean overwriteEditable="${checkedEditable}" owner="${clist}" field="template" />
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'default.relation.label')}</dt>
                                <dd>
                                    <i class="icon ${clistInfo.targetIcon} la-list-icon"></i>
                                    <g:link controller="${clistInfo.targetController}" action="show" params="${[id: clistInfo.target.id]}">
                                        ${clistInfo.targetName}
                                    </g:link>
                                </dd>
                            </dl>

                            <dl>
                                <dt>${message(code:'default.lastUpdated.label')}</dt>
                                <dd>
                                    <span class="${DateUtils.isDateToday(clist.lastUpdated) ? '' : 'sc_darkgrey'}">
                                        ${DateUtils.getLocalizedSDF_noTime().format(clist.lastUpdated)}
                                    </span>
                                </dd>
                            </dl>

                            <dl>
                                <dt>${message(code:'default.dateCreated.label')}</dt>
                                <dd>
                                    <span class="${DateUtils.isDateToday(clist.dateCreated) ? '' : 'sc_darkgrey'}">
                                        ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                                    </span>
                                </dd>
                            </dl>
                        </div><!-- .la-inline-lists -->

                    </div>

                    <g:set var="wfKey" value="${clistInfo.target.class.name}:${clistInfo.target.id}:${WfChecklist.KEY}:${clist.id}" />

                    <div class="four wide column wf-centered">
                    </div>
                </div><!-- .row -->


                <g:set var="cpoints" value="${clist.getSequence()}" />
                <g:each in="${cpoints}" var="cpoint" status="ti">

                    <div class="row">
                        <div class="two wide column wf-centered">
                            <i class="${WorkflowHelper.getCssIconAndColorByStatus(cpoint.done ? RDStore.WF_TASK_STATUS_DONE : RDStore.WF_TASK_STATUS_OPEN)} large"></i>
                        </div>
                        <div class="ten wide column">

                            <div class="la-inline-lists">
                                <dl>
                                    <dt>${message(code: 'workflow.task.label')}</dt>
                                    <dd>
                                        <div class="ui header">
                                            <ui:xEditable overwriteEditable="${checkedEditable}" owner="${cpoint}" field="title" type="text" />
                                        </div>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'default.description.label')}</dt>
                                    <dd>
                                        <ui:xEditable overwriteEditable="${checkedEditable}" owner="${cpoint}" field="description" type="textarea" />
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'default.comment.label')}</dt>
                                    <dd>
                                        <ui:xEditable overwriteEditable="${checkedEditable}" owner="${cpoint}" field="comment" type="textarea" />
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'workflow.checkpoint.done')}</dt>
                                    <dd>
                                        <ui:xEditableBoolean overwriteEditable="${checkedEditable}" owner="${cpoint}" field="done" />
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'workflow.checkpoint.date')}</dt>
                                    <dd>
                                        <ui:xEditable overwriteEditable="${checkedEditable}" owner="${cpoint}" field="date" type="date" />
                                    </dd>
                                </dl>

                                <dl>
                                    <dt>${message(code:'default.lastUpdated.label')}</dt>
                                    <dd>
                                        <span class="${DateUtils.isDateToday(cpoint.lastUpdated) ? '' : 'sc_darkgrey'}">
                                            ${DateUtils.getLocalizedSDF_noTime().format(cpoint.lastUpdated)}
                                        </span>
                                    </dd>
                                </dl>
                            </div><!-- .la-inline-lists -->

                        </div>

                        <g:set var="tKey" value="${clistInfo.target.class.name}:${clistInfo.target.id}:${WfCheckpoint.KEY}:${cpoint.id}" />%{-- todo --}%

                        <div class="four wide column wf-centered">
                            <g:if test="${checkedEditable}"><!-- TODO: workflows-permissions -->
                                <g:if test="${ti == 1 && cpoints.size() == 2}">%{-- override layout --}%
                                    <div class="${Btn.ICON.SIMPLE} compact la-hidden"><ui:placeholder /></div>
                                    <div class="${Btn.MODERN.SIMPLE} compact"
                                         data-cmd="moveUp:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="${Icon.CMD.MOVE_UP}"></i>
                                    </div>
                                </g:if>
                                <g:else>
                                    <g:if test="${ti > 0}">
                                        <div class="${Btn.MODERN.SIMPLE} compact"
                                             data-cmd="moveUp:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="${Icon.CMD.MOVE_UP}"></i>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div class="${Btn.ICON.SIMPLE} compact la-hidden"><ui:placeholder /></div>
                                    </g:else>
                                    <g:if test="${ti < cpoints.size()-1}">
                                        <div class="${Btn.MODERN.SIMPLE} compact"
                                             data-cmd="moveDown:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="${Icon.CMD.MOVE_DOWN}"></i>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div class="${Btn.ICON.SIMPLE} compact la-hidden"><ui:placeholder /></div>
                                    </g:else>
                                </g:else>
                            </g:if>

                            <g:if test="${checkedEditable}"><!-- TODO: workflows-permissions -->
%{--                                <div class="${Btn.MODERN.NEGATIVE}"--}%
%{--                                     data-cmd="delete:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="${Icon.CMD.DELETE}"></i>--}%
%{--                                </div>--}%
                                <div class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.checkpoint", args: [cpoint.title])}"
                                        data-confirm-term-how="delete"
                                        data-callback="workflowFlyoutCmd"
                                        data-cmd="delete:${WfCheckpoint.KEY}:${cpoint.id}"
                                        data-key="${WfChecklist.KEY}:${clist.id}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </div>
                            </g:if>

                        </div>
                    </div><!-- .row -->

                </g:each>

                <!-- -->

                <g:if test="${checkedEditable}"><!-- TODO: workflows-permissions -->

                    <div class="row">
                        <div class="two wide column"></div>

                        <div class="ten wide column">
                            <g:form name="cpForm" controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" method="POST" class="ui form" style="display:none">

                                <div style="margin-top:2em;">
                                    <div class="field required">
                                        <g:set var="fieldName" value="${WfCheckpoint.KEY}_title" />
                                        <label for="${fieldName}">${message(code:'default.title.label')}</label>
                                        <input type="text" name="${fieldName}" id="${fieldName}" required="required" />
                                    </div>

                                    <div class="field">
                                        <g:set var="fieldName" value="${WfCheckpoint.KEY}_description" />
                                        <label for="${fieldName}">${message(code:'default.description.label')}</label>
                                        <input type="text" name="${fieldName}" id="${fieldName}" />
                                    </div>

                                    <input type="hidden" name="${WfCheckpoint.KEY}_checklist" value="${clist.id}" />

                                    <input type="hidden" name="cmd" value="create:${WfCheckpoint.KEY}" />
                                    <input type="hidden" name="key" value="${WfChecklist.KEY}:${clist.id}" />
                                    <input type="hidden" name="target" value="${clistInfo.target.class.name}:${clistInfo.target.id}" />
                                </div>
                                <div class="field">
                                    <input type="submit" class="${Btn.POSITIVE}" name="save" value="Neue Aufgabe hinzufügen">
                                </div>
                            </g:form>
                        </div>

                        <div class="four wide column wf-centered">
                            <div class="${Btn.ICON.SIMPLE} compact la-hidden"><ui:placeholder /></div>
                            <div class="${Btn.ICON.SIMPLE} compact la-hidden"><ui:placeholder /></div>

                            <div class="${Btn.MODERN.SIMPLE} compact" id="cpFormToggle"><i class="${Icon.CMD.ADD}"></i></div>
                        </div>
                    </div><!-- .row -->

                </g:if>

            </div><!-- .grid -->
    </div>

    <style>
        .ui.grid .row               { padding-top: 0.35rem; padding-bottom: 0.35rem; }
        .ui.grid .row:first-of-type { padding-top: 1.2rem; }
        .ui.grid .row:last-of-type  { padding-bottom: 1.2rem; }

        .la-inline-lists dl dt { padding: 0.35rem 0 0.35rem 0;}
        .la-inline-lists dl dd { padding: 0.35rem 0 0.35rem 1rem;}
    </style>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.workflowFlyoutCmd = function ($trigger) {
            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
                data: {
                    cmd: $trigger.attr ('data-cmd'),
                    key: $trigger.attr ('data-key'),
                }
            }).done (function (response) {
                $('#wfFlyout').html (response)
                r2d2.initDynamicUiStuff ('#wfFlyout')
                r2d2.initDynamicXEditableStuff ('#wfFlyout')
            })
        }

        $('#wfFlyout .content .icon.button[data-cmd]:not([data-callback])').on ('click', function(e) {
            JSPC.app.workflowFlyoutCmd($(this))
        })

        $('#cpFormToggle').on ('click', function () {
            $(this).toggleClass ('la-modern-button')
            $('#cpForm').toggle()
            $('#cpForm #WF_CHECKPOINT_title')[0].focus()
        })

        $('#cpForm input[type=submit]').on ('click', function(e) {
            e.preventDefault()
            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
                data: $('#cpForm').serialize()
            }).done (function (response) {
                $('#wfFlyout').html (response)
                r2d2.initDynamicUiStuff ('#wfFlyout')
                r2d2.initDynamicXEditableStuff ('#wfFlyout')
            })
        })
    </laser:script>
</g:if>

%{--<laser:script file="${this.getGroovyPageFileName()}">--}%
%{--    docs.init('.workflow-details');--}%
%{--</laser:script>--}%