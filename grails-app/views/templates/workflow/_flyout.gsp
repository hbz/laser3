<%@ page import="de.laser.UserSetting; de.laser.workflow.WorkflowHelper; de.laser.workflow.WfCheckpoint; de.laser.workflow.WfChecklist; de.laser.WorkflowOldService; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:if test="${clist}">
    <%
        boolean checkedEditable = workflowService.hasUserPerm_edit()

        if (referer) {
            if (referer.contains('license/show') || referer.contains('lic/show') || referer.contains('subscription/show') || referer.contains('sub/show')) {
                checkedEditable = checkedEditable && (contextService.getUser().getSetting(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES).getValue() == RDStore.YN_YES)
            }
        }
    %>

    <g:set var="clistInfo" value="${clist.getInfo()}" />

    <div class="ui header center aligned">
        ${clist.title}
    </div>

    <div class="content">

            <div class="ui vertically divided grid">

                <div class="row">
                    <div class="two wide column"></div>
                    <div class="twelve wide column">
                        Sie bearbeiten den Workflow <strong>${clist.title}</strong>.
                        Ihre Änderungen werden direkt (ohne explizites Speichern) wirksam.
                    </div>
                    <div class="two wide column"></div>
                </div>

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
                                <dt>${message(code:'default.lastUpdated.label')} / ${message(code:'default.dateCreated.label')}</dt>
                                <dd>
                                    <span class="${DateUtils.isDateToday(clist.lastUpdated) ? '' : 'sc_darkgrey'}">
                                        ${DateUtils.getLocalizedSDF_noTime().format(clist.lastUpdated)}
                                    </span> /
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
                            <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(cpoint.done ? RDStore.WF_TASK_STATUS_DONE : RDStore.WF_TASK_STATUS_OPEN)}"></i>
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
                                <g:if test="${ti > 0}">
                                    <div class="ui icon blue button compact la-modern-button"
                                         data-cmd="moveUp:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="icon arrow up"></i>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                </g:else>
                                <g:if test="${ti < cpoints.size()-1}">
                                    <div class="ui icon blue button compact la-modern-button"
                                         data-cmd="moveDown:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="icon arrow down"></i>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                </g:else>
                            </g:if>

                            <g:if test="${checkedEditable}"><!-- TODO: workflows-permissions -->
%{--                                <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"--}%
%{--                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.checkpoint", args: [cpoint.title])}"--}%
%{--                                        data-confirm-term-how="delete"--}%
%{--                                        controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}"--}%
%{--                                        params="${[cmd:"delete:${WfCheckpoint.KEY}:${cpoint.id}", info:"${wfKey}"]}"--}%
%{--                                        role="button"--}%
%{--                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">--}%
%{--                                    <i class="trash alternate outline icon"></i>--}%
%{--                                </g:link>--}%
                                <div class="ui icon negative button la-modern-button"
%{--                                <div class="ui icon negative button la-modern-button js-open-confirm-modal"--}%
%{--                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.checkpoint", args: [cpoint.title])}"--}%
%{--                                     data-confirm-term-how="delete"--}%
%{--                                     role="button" aria-label="${message(code: 'ariaLabel.delete.universal')}"--}%
                                     data-cmd="delete:${WfCheckpoint.KEY}:${cpoint.id}" data-key="${WfChecklist.KEY}:${clist.id}"><i class="trash alternate outline icon"></i>
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
                                    <input type="submit" class="ui button green" name="save" value="Neue Aufgabe hinzufügen">
                                </div>
                            </g:form>
                        </div>

                        <div class="four wide column wf-centered">
                            <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                            <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>

                            <div class="ui icon blue button compact la-modern-button" id="cpFormToggle"><i class="icon plus"></i></div>
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
        $('#cpFormToggle').on ('click', function () {
            $(this).toggleClass ('la-modern-button')
            $('#cpForm').toggle()
        })
        $('#wfFlyout .content .icon.button[data-cmd]').on ('click', function(e) {
            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
                data: {
                    cmd: $(this).attr ('data-cmd'),
                    key: $(this).attr ('data-key'),
                }
            }).done (function (response) {
                $('#wfFlyout').html (response);
                r2d2.initDynamicUiStuff ('#wfFlyout');
                r2d2.initDynamicXEditableStuff ('#wfFlyout');
            })
        })
        $('#cpForm input[type=submit]').on ('click', function(e) {
            e.preventDefault()
            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
                data: $('#cpForm').serialize()
            }).done (function (response) {
                $('#wfFlyout').html (response);
                r2d2.initDynamicUiStuff ('#wfFlyout');
                r2d2.initDynamicXEditableStuff ('#wfFlyout');
            })
        })
    </laser:script>
</g:if>

%{--<laser:script file="${this.getGroovyPageFileName()}">--}%
%{--    docs.init('.workflow-details');--}%
%{--</laser:script>--}%