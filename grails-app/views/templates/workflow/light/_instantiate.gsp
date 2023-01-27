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

        <div id="tmpModalFix-A">
%{--        <h4 class="ui dividing header">Neu anlegen</h4>--}%

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
        <g:if test="${ false && wfTemplateList }">
            <div id="tmpModalFix-B">
            <h4 class="ui dividing header">Vorlage kopieren</h4>
            <div class="field">
                <label for="sourceId">${message(code:'workflow.template')}</label>

                <ui:dropdownWithI18nExplanations class="ui dropdown"
                                                 name="sourceId" id="sourceId"
                                                 noSelection="${message(code:'default.select.choose.label')}"
                                                 from="${wfTemplateList}"
                                                 optionKey="id"
                                                 optionValue="title"
                                                 optionExpl="${{ it.description }}" />

            </div>

            <input type="hidden" name="cmd" value="instantiate:${WfChecklist.KEY}" />
            <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
            </div>
        </g:if>

    </g:form>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    // tmp - todo
    // tmp - todo
    // tmp - todo
    $('#modalInstantiateWorkflowLight #tmpModalFix-A input').on ('change', function () {
        var block = false
        $('#modalInstantiateWorkflowLight #tmpModalFix-A input[type!=hidden]').map (function (idx, elem) {
            if ($(elem).val()) { block = true }
        })
        if (block) {
            $('#modalInstantiateWorkflowLight #tmpModalFix-B *').attr ('disabled', 'disabled')
            $('#modalInstantiateWorkflowLight #tmpModalFix-B #sourceId').addClass ('disabled')
        } else {
            $('#modalInstantiateWorkflowLight #tmpModalFix-B *').removeAttr ('disabled')
            $('#modalInstantiateWorkflowLight #tmpModalFix-B #sourceId').removeClass ('disabled')
        }
    })
    $('#modalInstantiateWorkflowLight #sourceId').dropdown ({
        onChange: function (value, text, $choice) {
            if ($choice) {
                $('#modalInstantiateWorkflowLight #tmpModalFix-A *').attr ('disabled', 'disabled')
            } else {
                $('#modalInstantiateWorkflowLight #tmpModalFix-A *').removeAttr ('disabled')
            }
        }
    })
</laser:script>