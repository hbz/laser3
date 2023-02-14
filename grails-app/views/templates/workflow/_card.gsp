<%@ page import="de.laser.workflow.WfChecklist; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<%
    List<WfChecklist> checklists = workflowService.sortByLastUpdated( workflowService.getWorkflows(ownobj, contextService.getOrg()) )
%>

<!-- TODO la-js-hideable @ flyout -->
    <ui:card message="workflow.plural" class="notes la-js-hideable ${css_class}" href="#modalCreateWorkflow" editable="${editable}">
        <g:each in="${checklists}" var="clist">
            <g:set var="clistInfo" value="${clist.getInfo()}" />
            <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="ten wide column la-column-right-lessPadding">
                            <a data-wfid="${clist.id}">${clist.title}</a>
                            <br />
                            ${message(code:'template.notes.updated')}
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${clistInfo.lastUpdated}"/>
                        </div>
                        <div class="right aligned six wide column la-column-left-lessPadding">
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                    data-confirm-term-how="delete"
                                    controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}"]}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </div>
                    </div>
                </div>
        </g:each>
    </ui:card>

    <div id="wfFlyout" class="ui eight wide flyout" style="padding:50px 0;overflow:scroll"></div>

<style>
    #container-workflows { width: 100%; }  %{-- TMP --}%
</style>

<laser:script file="${this.getGroovyPageFileName()}">

    $('a[data-wfId]').on ('click', function(e) {
        var key = '${WfChecklist.KEY}:' + $(this).attr ('data-wfId');

        if (! $('body').hasClass ('la-decksaver-active')) {
            $('#wfFlyout').flyout ({
                onHide: function (e) {
                    $('.ui.page.dimmer').dimmer ('show');
                    document.location = document.location;
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