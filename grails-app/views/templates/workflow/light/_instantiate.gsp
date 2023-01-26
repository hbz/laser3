<%@ page import="de.laser.workflow.light.*; de.laser.storage.RDStore; de.laser.workflow.*; de.laser.WorkflowService" %>

<%
    // TODO

    Set<WfChecklist> wfTemplateList = WfChecklist.getAllTemplatesByOwnerAndObjType(contextService.getOrg(), target)

    String targetText = '?'
    String targetController = '?'
    String targetType_plural = '?'

    if (cmd == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION) {
        targetText = target.name
        targetController = 'org'
        targetType_plural = message(code:'org.institution.plural')
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

<ui:modal id="modalInstantiateWorkflowLight" text="Workflow für '${targetText}' erstellen">

    <g:form controller="${targetController}" action="workflows" id="${target.id}" method="POST" class="ui form">
        <div class="ui label red" style="float:right">Feature in Entwicklung</div><br />

        <div class="ui secondary menu">
            <a class="active item" data-tab="wfl-menu-new">Neue Checkliste anlegen</a>
            <a class="item" data-tab="wfl-menu-instantiate">Vorlage kopieren</a>
        </div>

        <div class="ui active tab" data-tab="wfl-menu-new" style="padding-top:1em;">
            <div class="field required">
                <g:set var="fieldName" value="${WfChecklist.KEY}_title" />
                <label for="${fieldName}">${message(code:'default.title.label')}</label>
                <input type="text" name="${fieldName}" id="${fieldName}" value="${wfcl?.title}" required="required" />
            </div>

            <div class="field">
                <g:set var="fieldName" value="${WfChecklist.KEY}_description" />
                <label for="${fieldName}">${message(code:'default.description.label')}</label>
                <input type="text" name="${fieldName}" id="${fieldName}" value="${wfcl?.description}" />
            </div>

            <div class="field">
                <g:set var="fieldName" value="${WfChecklist.KEY}_numberOfPoints" />
                <label for="${fieldName}">Anzahl der Einträge (kann später geändert werden)</label>
                <input type="text" name="${fieldName}" id="${fieldName}" value="3" />
            </div>

            <input type="hidden" name="cmd" value="create:${WfChecklist.KEY}" />
            <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
        </div>
        <div class="ui tab" data-tab="wfl-menu-instantiate" style="padding-top:1em;">
            <g:if test="${wfTemplateList}">
                <div class="field">
                    <label for="workflowId">${message(code:'workflow.template')}</label>

                    <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                                                     name="workflowId" id="workflowId"
                                                     noSelection="${message(code:'default.select.choose.label')}"
                                                     from="${wfTemplateList}"
                                                     optionKey="id"
                                                     optionValue="title"
                                                     optionExpl="${{ it.description }}" />

                </div>

                <input type="hidden" name="cmd" value="instantiate:${WfChecklist.KEY}" />
                <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
            </g:if>
            <g:else>
                <p>Derzeit sind keine Vorlagen vorhanden.</p>
            </g:else>
        </div>

    </g:form>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#modalInstantiateWorkflowLight .menu .item').tab()
</laser:script>