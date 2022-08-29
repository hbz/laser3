<%@ page import="de.laser.storage.RDStore; de.laser.workflow.*; de.laser.WorkflowService" %>

<%
    // TODO

    String targetText = '?'
    String targetController = '?'
    
    if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION) {
        targetText = target.name
        targetController = 'org'
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER) {
        targetText = target.name
        targetController = 'org'
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE) {
        targetText = target.reference
        targetController = 'license'
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION) {
        targetText = target.name
        targetController = 'subscription'
    }
%>

<ui:modal id="modalInstantiateWorkflow" text="Workflow für '${targetText}' erstellen">

    <g:form controller="${targetController}" action="workflows" id="${target.id}" method="POST" class="ui form">
        <div class="field">
            <label for="workflowId">${message(code:'workflow.label')}</label>

            <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                    name="workflowId" id="workflowId"
                    noSelection="${message(code:'default.select.choose.label')}"
                    from="${WfWorkflowPrototype.findAllByStateAndTargetType( RDStore.WF_WORKFLOW_STATE_ACTIVE, cmd )}"
                    optionKey="id"
                    optionValue="title"
                    optionExpl="${{ it.description + ' (Version: ' + it.variant + ')'}}" />
        </div>

        <input type="hidden" name="cmd" value="instantiate:${WfWorkflowPrototype.KEY}" />
        <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
    </g:form>

</ui:modal>