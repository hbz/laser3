<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>

<g:form controller="admin" action="manageWorkflows" method="POST" class="ui form">
    <g:if test="${! tmplIsModal}"><div class="ui segment"></g:if>

    <div class="field required">
        <label for="${prefix}_title">${message(code:'default.title.label')}</label>
        <input type="text" name="${prefix}_title" id="${prefix}_title" value="${workflow?.title}" required="required" />
    </div>

    <div class="field">
        <label for="${prefix}_description">${message(code:'default.description.label')}</label>
        <input type="text" name="${prefix}_description" id="${prefix}_description" value="${workflow?.description}" />
    </div>

    <g:if test="${prefix == WfWorkflowPrototype.KEY}">

        <div class="field required">
            <label for="${prefix}_type>">State</label>
            <laser:select class="ui dropdown la-not-clearable" id="${prefix}_state" name="${prefix}_state"
                          required="required"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATE )}"
                          value="${workflow?.state?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        <div class="field">
            <label for="${prefix}_child">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} &darr;</label>
            <g:select class="ui dropdown" id="${prefix}_child" name="${prefix}_child"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_childList}"
                      value="${workflow?.child?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>

    </g:if>
    <g:if test="${prefix == WfWorkflow.KEY}">

        <div class="field required">
            <label for="${prefix}_status">${message(code:'default.status.label')}</label>
            <laser:select class="ui dropdown la-not-clearable" id="${prefix}_status" name="${prefix}_status"
                          required="required"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                          from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_STATUS )}"
                          value="${workflow?.status?.id}"
                          optionKey="id"
                          optionValue="value" />
        </div>

        <div class="field">
            <label for="${prefix}_comment">${message(code:'default.comment.label')}</label>
            <input type="text" name="${prefix}_comment" id="${prefix}_comment" value="${workflow?.comment}" />
        </div>


        <div class="field">
            <label for="${prefix}_subscription">${message(code:'subscription.label')}</label>
            <p>
                <g:if test="${workflow?.subscription}">
                    <g:link controller="subscription" action="show" params="${[id: workflow.subscription.id]}">
                        <i class="ui icon clipboard"></i> ${workflow.subscription.name}
                    </g:link>
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
            <label for="${prefix}_child">${message(code: 'workflow.object.' + WfTask.KEY)} &darr;</label>
            <g:select class="ui dropdown disabled" id="${prefix}_child" name="${prefix}_child"
                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                      from="${dd_childList}"
                      value="${workflow?.child?.id}"
                      optionKey="id"
                      optionValue="${{'(' + it.id + ') ' + it.title}}" />
        </div>
        --}%

        <div class="field">
            <label for="${prefix}_prototype">${message(code:'default.prototype.label')}</label>
            <p>
                <g:if test="${workflow?.prototype}">
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + workflow.prototype.id]}">
                        <i class="ui icon clone outline"></i> ${workflow.prototype.title}
                    </g:link>
                </g:if>
            </p>
        </div>

    </g:if>

    <g:if test="${cmd == 'edit'}">
        <input type="hidden" name="cmd" value="${cmd}:${prefix}:${workflow.id}" />
    </g:if>
    <g:else>
        <input type="hidden" name="cmd" value="${cmd}:${prefix}" />
    </g:else>

    <g:if test="${! tmplIsModal}">
            <div class="field">
                <button type="submit" class="ui button"><% if (prefix == WfWorkflowPrototype.KEY) { print 'Prototyp anlegen' } else { print 'Anlegen' } %></button>
            </div>
        </div>
    </g:if>

</g:form>