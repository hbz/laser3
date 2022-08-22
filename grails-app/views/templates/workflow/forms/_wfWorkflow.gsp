<%@ page import="de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form url="${formUrl}" method="POST" class="ui form">

    <div class="field required">
        <label for="${prefix}_title">${message(code:'default.title.label')}</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${workflow?.title}" required="required" />
    </div>

    <div class="field">
        <label for="${prefix}_description">${message(code:'default.description.label')}</label>
        <input type="text" name="${prefix}_description" id="${prefix}_description" value="${workflow?.description}" />
    </div>

    <g:if test="${prefix == WfWorkflowPrototype.KEY}">

        <div class="fields">
            <div class="field eleven wide required">
                <label for="${prefix}_type>">State</label>
                <ui:select class="ui dropdown la-not-clearable" id="${prefix}_state" name="${prefix}_state"
                              required="required"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATE )}"
                              value="${workflow?.state?.id}"
                              optionKey="id"
                              optionValue="value" />
            </div>
            <div class="field five wide required">
                <label for="${prefix}_prototypeVersion>">Version</label>
                <input type="text" name="${prefix}_prototypeVersion" id="${prefix}_prototypeVersion" value="${workflow?.prototypeVersion}" />
            </div>
        </div>

        <div class="field">
            <label for="${prefix}_task">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}</label>
            <g:select class="ui dropdown" id="${prefix}_task" name="${prefix}_task"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${workflow?.task?.id}"
                      optionKey="id"
                      optionValue="${{ (tpIdTable && tpIdTable[it.id]) ? ('(' + tpIdTable[it.id] + ') ' + it.title) : it.title }}" />
        </div>

    </g:if>
    <g:if test="${prefix == WfWorkflow.KEY}">

        <div class="field">
            <label for="${prefix}_comment">${message(code:'default.comment.label')}</label>
            <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${workflow?.comment}" />
        </div>

        <div class="field required">
            <label for="${prefix}_status">${message(code:'default.status.label')}</label>
            <ui:select class="ui dropdown la-not-clearable" id="${prefix}_status" name="${prefix}_status"
                          required="required"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                          value="${workflow?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        <div class="field">
            <label for="${prefix}_subscription">${message(code:'subscription.label')}</label>
            <p>
                <g:if test="${workflow?.subscription}">
                    <div class="ui la-flexbox">
                        <i class="icon clipboard la-list-icon"></i>
                        <g:link controller="subscription" action="show" params="${[id: workflow.subscription.id]}">
                            ${workflow.subscription.name}
                        </g:link>
                    </div>
                </g:if>
            </p>
            %{--
            <g:select class="ui dropdown disabled" id="${prefix}_subscription" name="${prefix}_subscription"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_subscriptionList}"
                      value="${workflow?.subscription?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.name}}" />
              --}%
        </div>

        %{--
        <div class="field">
            <label for="${prefix}_task">${message(code: 'workflow.object.' + WfTask.KEY)} &darr;</label>
            <g:select class="ui dropdown disabled" id="${prefix}_task" name="${prefix}_task"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${workflow?.task?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
        --}%

%{--        <div class="field">--}%
%{--            <label for="${prefix}_prototype">${message(code:'default.prototype.label')}</label>--}%
%{--            <p>--}%
%{--                <g:if test="${workflow?.prototype}">--}%
%{--                    <div class="ui la-flexbox">--}%
%{--                        <i class="icon clone outline la-list-icon"></i>--}%
%{--                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + workflow.prototype.id]}">--}%
%{--                             ${workflow.prototype.title} ????? <span class="sc_grey">(${message(code:'default.version.label')} ${workflow.prototype.prototypeVersion})</span>--}%
%{--                        </g:link>--}%
%{--                    </div>--}%
%{--                </g:if>--}%
%{--            </p>--}%
%{--        </div>--}%

    </g:if>

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${workflow.id}" />
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