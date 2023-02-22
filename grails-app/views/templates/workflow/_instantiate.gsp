<%@ page import="de.laser.Subscription; de.laser.workflow.*; de.laser.storage.RDStore; de.laser.workflow.*; de.laser.WorkflowOldService" %>

<%
    // TODO

    Set<WfChecklist> wfTemplateList = WfChecklist.getAllTemplatesByOwnerAndObjType(contextService.getOrg(), target)

    String targetText = '?'
    String targetController = '?'

    if (target instanceof de.laser.Org) { // TODO
        targetText = target.name
        targetController = 'org'
    }
    else if (target instanceof de.laser.License) {
        targetText = target.reference
        targetController = 'license'
    }
    else if (target instanceof de.laser.Subscription) {
        targetText = target.name
        targetController = 'subscription'
    }
%>

<ui:modal id="modalCreateWorkflow" text="Workflow für '${targetText}' erstellen">

    <g:if test="${true}"> %{-- DEV-BLOCKER --}%

    <g:form controller="${controllerName}" action="${actionName}" id="${target.id}" method="POST" class="ui form">
        <div class="ui grid">
            <div class="four wide column">
                <div id="modalTabMenu" class="ui pointing secondary vertical la-tab-with-js menu">
                    <a data-tab="newWorkflow" class="item">Workflow neu anlegen</a>
                    <a data-tab="copyWorkflow" class="item">Vorlage kopieren</a>
                </div>
            </div>

            <div class="twelve wide column">
                <div data-tab="newWorkflow" class="ui tab active" style="padding: 0 1em;">

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

                    <div class="field required">
                        <g:set var="fieldName" value="${WfChecklist.KEY}_numberOfPoints" />
                        <label for="${fieldName}">Anzahl der Einträge (kann später geändert werden)</label>
                        <input type="text" name="${fieldName}" id="${fieldName}" value="3" required="required" />
                    </div>

                    <input type="hidden" name="cmd" value="create:${WfChecklist.KEY}" />
                    <input type="hidden" name="target" value="${target.class.name}:${target.id}" />

                </div>
                <div data-tab="copyWorkflow" class="ui tab" style="padding: 0 1em;">
                    <g:if test="${wfTemplateList}">

                        <div class="field">
                            <g:set var="fieldName" value="${WfChecklist.KEY}_title" />
                            <label for="${fieldName}">Neuer ${message(code:'default.title.label')}</label>
                            <input type="text" name="${fieldName}" id="${fieldName}" />
                        </div>

                        <div class="field">
                            <g:set var="fieldName" value="${WfChecklist.KEY}_description" />
                            <label for="${fieldName}">Neue ${message(code:'default.description.label')}</label>
                            <input type="text" name="${fieldName}" id="${fieldName}" />
                        </div>

                        <div class="field required">
                            <label for="sourceId">${message(code:'workflow.template')}</label>

                            <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                                                             name="sourceId" id="sourceId"
                                                             from="${wfTemplateList}"
                                                             value="${wfTemplateList.first().id}"
                                                             optionKey="id"
                                                             optionValue="title"
                                                             optionExpl="${{ it.description + ' (' + it.getSequence().size() + ' Aufgaben)'}}" />
                        </div>

                        <input type="hidden" name="cmd" value="instantiate:${WfChecklist.KEY}" />
                        <input type="hidden" name="target" value="${target.class.name}:${target.id}" />
                    </g:if>
                    <g:else>
                        <p>Es wurden (noch) keine Vorlagen gefunden.</p>
                    </g:else>
                </div>
            </div>
        </div>
    </g:form>
    </g:if>
    <g:else>
        <p>Derzeit sind keine Workflows verfügar.</p>
    </g:else>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#modalTabMenu .item').tab({ onVisible : function () {
        $('#modalCreateWorkflow .tab[data-tab=newWorkflow] input').addClass ('disabled').attr ('disabled', 'disabled')
        $('#modalCreateWorkflow .tab[data-tab=copyWorkflow] input').addClass ('disabled').attr ('disabled', 'disabled')
        $(this).find ('input').removeClass ('disabled').removeAttr ('disabled')
    } })

    JSPC.callbacks.modal.show.modalCreateWorkflow = function () {
        $('#modalTabMenu .item').tab('change tab', 'copyWorkflow').tab('change tab', 'newWorkflow')
    }
</laser:script>