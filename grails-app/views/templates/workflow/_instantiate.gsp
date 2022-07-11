<%@ page import="de.laser.storage.RDStore; de.laser.workflow.*; de.laser.WorkflowService" %>

<ui:modal id="modalInstantiateWorkflow" text="Workflow für '${subscription.name}' erstellen">

    <g:form controller="subscription" action="workflows" id="${subscription.id}" method="POST" class="ui form">
        <div class="field">
            <label for="workflowId">${message(code:'workflow.label')}</label>

            <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                    name="workflowId" id="workflowId"
                    noSelection="${message(code:'default.select.choose.label')}"
                    from="${WfWorkflowPrototype.findAllByState( RDStore.WF_WORKFLOW_STATE_ACTIVE )}"
                    optionKey="id"
                    optionValue="title"
                    optionExpl="description" />
        </div>

        <input type="hidden" name="cmd" value="instantiate:${WfWorkflowPrototype.KEY}" />
        <input type="hidden" name="subId" value="${subscription.id}" />
    </g:form>

</ui:modal>