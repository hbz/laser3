<%@ page import="de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form url="${formUrl}" method="POST" class="ui form">

    <div class="field required">
        <label for="${prefix}_title">${message(code:'default.title.label')}</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${task?.title}" required="required" />
    </div>

    <div class="fields two" style="margin-bottom:0;">
        <div class="field twelve wide">
            <label for="${prefix}_description">${message(code:'default.description.label')}</label>
            <input type="text" name="${prefix}_description" id="${prefix}_description" value="${task?.description}" />
        </div>

        <div class="field four wide required">
            <label for="${prefix}_priority">${message(code:'default.priority.label')}</label>
            <ui:select class="ui dropdown la-not-clearable" id="${prefix}_priority" name="${prefix}_priority"
                          required="required"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_PRIORITY )}"
                          value="${task?.priority?.id ?: RDStore.WF_TASK_PRIORITY_NORMAL.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>
    </div>

    <g:if test="${prefix == WfTaskPrototype.KEY}">

        <div class="field">
            <label for="${prefix}_condition">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}</label>
            <g:select class="ui dropdown" id="${prefix}_condition" name="${prefix}_condition"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_conditionList}"
                      value="${task?.condition?.id}"
                      optionKey="id"
                      optionValue="${{ (cpIdTable && cpIdTable[it.id]) ? ('(' + cpIdTable[it.id] + ') ' + it.title) : it.title }}" />
        </div>

        <div class="field">
            <label for="${prefix}_next">Nachfolger (${message(code: 'default.prototype.label')}) <i class="icon angle right"></i></label>
            <g:select class="ui dropdown" id="${prefix}_next" name="${prefix}_next"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_nextList}"
                      value="${task?.next?.id}"
                      optionKey="id"
                      optionValue="${{ (tpIdTable && tpIdTable[it.id]) ? ('(' + tpIdTable[it.id] + ') ' + it.title) : it.title }}" />
        </div>
                %{--<div class="field">
                    <label for="${prefix}_child">Child &darr;&darr;</label>
                    <g:select class="ui dropdown" id="${prefix}_child" name="${prefix}_child"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_childList}"
                              value="${task?.child?.id}"
                              optionKey="id"
                              optionValue="${{ (tpIdTable && tpIdTable[it.id]) ? ('(' + tpIdTable[it.id] + ') ' + it.title) : it.title }}" />
                </div>--}%
    </g:if>

%{-- <div class="field">
<label for="${prefix}_type">Typ</label>
<ui:select class="ui dropdown" id="${prefix}_type" name="${prefix}_type"
              noSelection="${['' : message(code:'default.select.choose.label')]}"
              from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_TYPE )}"
              value="${task?.type?.id}"
              optionKey="id"
              optionValue="value" />
</div> --}%

    <g:if test="${prefix == WfTask.KEY}">

        <div class="field">
            <label for="${prefix}_comment">${message(code:'default.comment.label')}</label>
            <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${task?.comment}" />
        </div>

        <div class="field required">
            <label for="${prefix}_status">${message(code:'default.status.label')}</label>
            <ui:select class="ui dropdown la-not-clearable" id="${prefix}_status" name="${prefix}_status"
                          required="required"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_TASK_STATUS )}"
                          value="${task?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        %{--
        <div class="field">
            <label for="${prefix}_condition">${message(code: 'workflow.object.' + WfCondition.KEY)}</label>
            <g:select class="ui dropdown disabled" id="${prefix}_condition" name="${prefix}_condition"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_conditionList}"
                      value="${task?.condition?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

        <div class="field">
            <div class="fields two">
                <div class="field">
                    <label for="${prefix}_next">Nachfolger &rarr;</label>
                    <g:select class="ui dropdown disabled" id="${prefix}_next" name="${prefix}_next"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_nextList}"
                              value="${task?.next?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
                <div class="field">
                    <label for="${prefix}_child">Kind &darr;</label>
                    <g:select class="ui dropdown disabled" id="${prefix}_child" name="${prefix}_child"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              from="${dd_childList}"
                              value="${task?.child?.id}"
                              optionKey="id"
                              optionValue="${{'(' + it.id + ') ' + it.title}}" />
                </div>
            </div>
        </div>
        --}%
    </g:if>

    <g:if test="${prefix == WfTaskPrototype.KEY}">

        <g:if test="${task}">
            <div class="field">
                <label for="${prefix}_workflow">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} <i class="icon angle up"></i></label>
                <p>
                    <g:each in="${WfWorkflowPrototype.findAllByTask(task, [sort: 'id'])}" var="workflow">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + workflow.id]}">
                            <i class="icon circle brown"></i>
                            ${workflow.title}
                            <span class="sc_grey">(${message(code:'default.version.label')} ${workflow.variant})</span>
                        </g:link>
                        &nbsp;&nbsp;&nbsp;
                    </g:each>
                </p>
            </div>

            <div class="field">
                <label for="${prefix}_previous">Vorgänger (${message(code: 'default.prototype.label')}) <i class="icon angle left"></i></label>
                <p>
                    <g:each in="${WfTaskPrototype.findAllByNext(task, [sort: 'id'])}" var="previous">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + previous.id]}">
                            <i class="icon circle blue"></i> ${previous.title}
                        </g:link>
                        &nbsp;&nbsp;&nbsp;
                    </g:each>
                </p>
                %{--
                <g:select class="ui dropdown disabled" id="${prefix}_previous" name="${prefix}_previous"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_previousList}"
                          value="${task?.getPrevious()?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />
                --}%
            </div>
        </g:if>

            %{--<div class="field">
                <label for="${prefix}_parent">Parent &uarr;</label>
                <p>
                    <g:if test="${task?.getParent()}">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + task.getParent().id]}">
                            <i class="icon circle blue"></i> ${task.getParent().title}
                        </g:link>
                    </g:if>
                </p>

                <g:select class="ui dropdown disabled" id="${prefix}_parent" name="${prefix}_parent"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${dd_parentList}"
                          value="${task?.getParent()?.id}"
                          optionKey="id"
                          optionValue="${{'(' + it.id + ') ' + it.title}}" />

            </div> --}%

        <g:if test="${task}">
            <div class="field">
                <div class="field">
                    <label>${message(code:'default.lastUpdated.label')}</label>
                    <p>${DateUtils.getLocalizedSDF_noTime().format(task.lastUpdated)}</p>
    %{--                ${message(code:'default.dateCreated.label')}: ${DateUtils.getLocalizedSDF_noTime().format(task.dateCreated)}--}%
                </div>
            </div>
        </g:if>
    </g:if>

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${task.id}" />
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