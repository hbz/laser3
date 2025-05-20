<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.workflow.WfChecklist; de.laser.storage.RDStore" %>
<laser:serviceInjection />

%{--# editable: ${editable}--}%
%{--# workflowService.hasWRITE(): ${workflowService.hasWRITE()}--}%

    <ui:card message="workflow.open.plural" class="workflows" href="#modalCreateWorkflow" editable="${workflowService.hasWRITE()}">
        <g:each in="${checklists.findAll{ it.getInfo().status != RDStore.WF_WORKFLOW_STATUS_DONE }}" var="clist">
            <g:set var="clistInfo" value="${clist.getInfo()}" />
            <div class="ui small feed content">
                    <div class="ui grid summary">
                        <div class="ten wide column la-column-right-lessPadding">
                            <a data-wfid="${clist.id}">${clist.title}</a>
                            <br />
                            ${message(code:'template.notes.updated')}
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${clistInfo.lastUpdated}"/>
                        </div>
                        <div class="right aligned six wide column la-column-left-lessPadding">
                            <g:if test="${workflowService.hasWRITE()}">
                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                        data-confirm-term-how="delete"
                                        controller="${clistInfo.targetController}" action="${actionName}" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}"]}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                        </div>
                    </div>
                </div>
        </g:each>
    </ui:card>

    <div id="wfFlyout" class="ui very wide flyout" style="padding:50px 0;overflow:scroll"></div>


<laser:script file="${this.getGroovyPageFileName()}">

    $('a[data-wfId]').on ('click', function(e) {
        var key = '${WfChecklist.KEY}:' + $(this).attr ('data-wfId');

        if (! $('body').hasClass ('la-decksaver-active')) {
            $('#wfFlyout').flyout ({
                onHide: function (e) {
                    $('#globalPageDimmer').dimmer ('show');
                    document.location = document.location.origin + document.location.pathname;
                }
            });
        }

        $.ajax ({
            url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
            data: {
                key: key
            }
        }).done (function (response) {
            $('#wfFlyout').html (response).flyout ('show');
            r2d2.initDynamicUiStuff ('#wfFlyout');
            r2d2.initDynamicXEditableStuff ('#wfFlyout');
        })
    });

</laser:script>