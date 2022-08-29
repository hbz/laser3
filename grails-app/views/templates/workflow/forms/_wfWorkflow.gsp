<%@ page import="de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form url="${formUrl}" method="POST" class="ui form">

    <div class="field required">
        <g:set var="fieldName" value="${prefix}_title" />
        <label for="${fieldName}">${message(code:'default.title.label')}</label>
        <input type="text" name="${fieldName}" id="${fieldName}" value="${workflow?.title}" required="required" />
    </div>

    <div class="field">
        <g:set var="fieldName" value="${prefix}_description" />
        <label for="${fieldName}">${message(code:'default.description.label')}</label>
        <input type="text" name="${fieldName}" id="${fieldName}" value="${workflow?.description}" />
    </div>

    <g:if test="${prefix == WfWorkflowPrototype.KEY}">

        <div class="field">
            <div class="two fields">
                <div class="field eight wide required">
                    <g:set var="fieldName" value="${prefix}_targetType" />
                    <label for="${fieldName}">${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_TARGET_TYPE).getI10n('desc')}</label>
                    <ui:select class="ui dropdown la-not-clearable" id="${fieldName}" name="${fieldName}"
                               required="required"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"
                               from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_TARGET_TYPE )}"
                               value="${workflow?.targetType?.id ?: RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION.id}"
                               optionKey="id"
                               optionValue="value" />
                </div>
                <div class="field eight wide required">
                    <g:set var="fieldName" value="${prefix}_state" />
                    <label for="${fieldName}">${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_STATE).getI10n('desc')}</label>
                    <ui:select class="ui dropdown la-not-clearable" id="${fieldName}" name="${fieldName}"
                               required="required"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"
                               from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATE )}"
                               value="${workflow?.state?.id ?: RDStore.WF_WORKFLOW_STATE_TEST.id}"
                               optionKey="id"
                               optionValue="value" />
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field eight wide required">
                    <g:set var="fieldName" value="${prefix}_targetRole" />
                    <label for="${fieldName}">${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_TARGET_ROLE).getI10n('desc')}</label>
                    <ui:select class="ui dropdown la-not-clearable" id="${fieldName}" name="${fieldName}"
                               required="required"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"
                               from="${[ RDStore.WF_WORKFLOW_TARGET_ROLE_CONSORTIUM ]}"
                               value="${workflow?.targetRole?.id ?: RDStore.WF_WORKFLOW_TARGET_ROLE_CONSORTIUM.id}"
                               optionKey="id"
                               optionValue="value" />
%{--                    from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_TARGET_ROLE )}"--}%
                </div>
                <div class="field eight wide required">
                    <g:set var="fieldName" value="${prefix}_variant" />
                    <label for="${fieldName}">Version</label>
                    <g:set var="defaultVersion" value="${DateUtils.getSDF_yyyy().format(new Date()) + '/' + DateUtils.getSDF_MM().format(new Date())}" />
                    <input type="text" name="${fieldName}" id="${fieldName}" value="${workflow?.variant ?: defaultVersion}" />
                </div>
            </div>
        </div>

        <div class="field">
            <g:set var="fieldName" value="${prefix}_task" />
            <label for="${fieldName}">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}</label>
            <g:select class="ui dropdown" id="${fieldName}" name="${fieldName}"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_taskList}"
                      value="${workflow?.task?.id}"
                      optionKey="id"
                      optionValue="${{ (tpIdTable && tpIdTable[it.id]) ? ('(' + tpIdTable[it.id] + ') ' + it.title) : it.title }}" />
        </div>

        <g:if test="${workflow}">
            <div class="field">
                <div class="field">
                    <label>${message(code:'default.lastUpdated.label')}</label>
                    <p>${DateUtils.getLocalizedSDF_noTime().format(workflow.lastUpdated)}</p>
    %{--                ${message(code:'default.dateCreated.label')}: ${DateUtils.getLocalizedSDF_noTime().format(workflow.dateCreated)}--}%
                </div>
            </div>
        </g:if>
    </g:if>
    <g:if test="${prefix == WfWorkflow.KEY}">

        <g:set var="wfInfo" value="${workflow?.getInfo()}" />

        <div class="field">
            <g:set var="fieldName" value="${prefix}_comment" />
            <label for="${fieldName}">${message(code:'default.comment.label')}</label>
            <input type="text" name="${fieldName}" id="${fieldName}" value="${workflow?.comment}" />
        </div>

        <div class="field required">
            <g:set var="fieldName" value="${prefix}_status" />
            <label for="${fieldName}">${message(code:'default.status.label')}</label>
            <ui:select class="ui dropdown la-not-clearable" id="${fieldName}" name="${fieldName}"
                          required="required"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                          value="${workflow?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        <g:if test="${wfInfo?.target}">
            <div class="field">
                <g:set var="fieldName" value="${prefix}_target" />
                <label for="${fieldName}">${message(code:'default.relation.label')} ${wfInfo.targetTitle}</label>
                <div class="ui la-flexbox">
                    <i class="icon ${wfInfo.targetIcon} la-list-icon"></i>
                    <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">
                        ${wfInfo.targetName}
                    </g:link>
                </div>
            </div>
        </g:if>
            %{--
            <g:select class="ui dropdown disabled" id="${prefix}_subscription" name="${prefix}_subscription"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_subscriptionList}"
                      value="${workflow?.subscription?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.name}}" />
              --}%


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
%{--                             ${workflow.prototype.title} ????? <span class="sc_grey">(${message(code:'default.version.label')} ${workflow.prototype.variant})</span>--}%
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