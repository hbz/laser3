<%@ page import="de.laser.storage.RDStore; de.laser.workflow.*; de.laser.WorkflowService" %>

<%
    // TODO

    String targetText = '?'
    String targetController = '?'
    String targetType_plural = '?'
    
    if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION) {
        targetText = target.name
        targetController = 'org'
        targetType_plural = message(code:'org.institution.plural')
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER) {
        targetText = target.name
        targetController = 'org'
        targetType_plural = message(code:'default.provider.label')
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE) {
        targetText = target.reference
        targetController = 'license'
        targetType_plural = message(code:'license.plural')
    }
    else if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION) {
        targetText = target.name
        targetController = 'subscription'
        targetType_plural = message(code:'subscription.plural')
    }
%>

<g:set var="wfPrototypeList" value="${WfWorkflowPrototype.findAllByStateAndTargetType( RDStore.WF_WORKFLOW_STATE_ACTIVE, cmd ).findAll{ !it.hasCircularReferences() }}" />

<g:if test="${wfPrototypeList}">
    <ui:modal id="modalInstantiateWorkflow" text="Workflow für '${targetText}' erstellen">

        <g:form controller="${targetController}" action="workflows" id="${target.id}" method="POST" class="ui form">
            <div class="field">
                <label for="workflowId">${message(code:'workflow.label')}</label>

                <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                                                 name="workflowId" id="workflowId"
                                                 noSelection="${message(code:'default.select.choose.label')}"
                                                 from="${wfPrototypeList}"
                                                 optionKey="id"
                                                 optionValue="title"
                                                 optionExpl="${{ it.description + ' (Version: ' + it.variant + ')'}}" />

            </div>
            <input type="hidden" name="cmd" value="instantiate:${WfWorkflowPrototype.KEY}" />
            <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
        </g:form>

    </ui:modal>
</g:if>
<g:else>
    <ui:modal id="modalInstantiateWorkflow" text="Workflow für '${targetText}' erstellen" hideSubmitButton="true">
        <p>
            ${message(code:'workflow.info.noActivePrototypes', args: [targetType_plural])}
        </p>
        %{--<ui:msg class="info" text="${message(code:'workflow.info.noActivePrototypes', args: [targetType_plural])}" noClose="true" />--}%
    </ui:modal>
</g:else>