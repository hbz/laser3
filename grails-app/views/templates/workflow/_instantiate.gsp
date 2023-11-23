<%@ page import="de.laser.Subscription; de.laser.workflow.*; de.laser.storage.RDStore;" %>

<%
    Set<WfChecklist> wfTemplateList = WfChecklist.getAllTemplatesByOwnerAndObjType(contextService.getOrg(), target)

    String targetText = '?'

    if (target instanceof de.laser.Org) {
        targetText = target.name
    }
    else if (target instanceof de.laser.License) {
        targetText = target.reference
    }
    else if (target instanceof de.laser.Subscription) {
        targetText = target.name
    }
%>

<ui:modal id="modalCreateWorkflow" text="Workflow für '${targetText}' anlegen">

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
                        <label for="${fieldName}_1">${message(code:'default.title.label')}</label>
                        <input type="text" name="${fieldName}" id="${fieldName}_1" value="${wfcl?.title}" required="required" />
                    </div>

                    <div class="field">
                        <g:set var="fieldName" value="${WfChecklist.KEY}_description" />
                        <label for="${fieldName}_1">${message(code:'default.description.label')}</label>
                        <input type="text" name="${fieldName}" id="${fieldName}_1" value="${wfcl?.description}" />
                    </div>

                    <div class="field">
                        <g:set var="fieldName" value="${WfChecklist.KEY}_numberOfPoints" />
                        <label for="${fieldName}_1">Anzahl der Einträge (kann später geändert werden)</label>
                        <input type="number" name="${fieldName}" id="${fieldName}_1" value="3" min="1" max="10" />
                    </div>

                    <input type="hidden" name="cmd" value="create:${WfChecklist.KEY}" />
                    <input type="hidden" name="target" value="${target.class.name}:${target.id}" />

                </div>
                <div data-tab="copyWorkflow" class="ui tab" style="padding: 0 1em;">
                    <g:if test="${wfTemplateList}">

                        <div class="field">
                            <g:set var="fieldName" value="${WfChecklist.KEY}_title" />
                            <label for="${fieldName}_2">Neuer ${message(code:'default.title.label')}</label>
                            <input type="text" name="${fieldName}" id="${fieldName}_2" placeholder="Von der Vorlage übernehmen" />
                        </div>

                        <div class="field">
                            <g:set var="fieldName" value="${WfChecklist.KEY}_description" />
                            <label for="${fieldName}_2">Neue ${message(code:'default.description.label')}</label>
                            <input type="text" name="${fieldName}" id="${fieldName}_2" placeholder="Von der Vorlage übernehmen" />
                        </div>

                        <div class="field required">
                            <label for="sourceId_2">${message(code:'workflow.template')}</label>

                            <ui:dropdownWithI18nExplanations class="ui dropdown la-not-clearable"
                                                             name="sourceId" id="sourceId_2"
                                                             from="${wfTemplateList}"
                                                             value="${wfTemplateList.first().id}"
                                                             optionKey="id"
                                                             optionValue="title"
                                                             optionExpl="${{ (it.description ?: '') + ' (' + it.getSequence().size() + ' Aufgaben)'}}" />
                        </div>

                        <input type="hidden" name="cmd" value="instantiate:${WfChecklist.KEY}" />
                        <input type="hidden" name="target" value="${target.class.name}:${target.id}" />

                        <div class="ui message info" style="margin-top:1.5em;text-align:left;font-size:14px;font-weight:normal;display:block">
                            <div class="content">
                                <i class="hand point right outline icon"></i>
                                Bei einer Kopie werden Informationen aus den Feldern
                                <strong>Kommentar</strong>, <strong>Vorlage</strong>, <strong>Aufgabe erledigt</strong> und <strong>Datumsangabe</strong>
                                nicht übernommen.
                            </div>
                        </div>

                    </g:if>
                    <g:else>
                        <p>Es wurden (noch) keine Vorlagen gefunden.</p>
                    </g:else>
                </div>
            </div>
        </div>
    </g:form>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#modalTabMenu .item').tab({ onVisible : function () {
        $('#modalCreateWorkflow .tab[data-tab=newWorkflow] input').addClass ('disabled').attr ('disabled', 'disabled')
        $('#modalCreateWorkflow .tab[data-tab=copyWorkflow] input').addClass ('disabled').attr ('disabled', 'disabled')
        $(this).find ('input').removeClass ('disabled').removeAttr ('disabled')
    } })

    JSPC.callbacks.modal.onShow.modalCreateWorkflow = function (trigger) {
        $('#modalTabMenu .item').tab ('change tab', 'copyWorkflow').tab ('change tab', 'newWorkflow')
    }

    JSPC.app.checkRequired = function () {
        $('#modalCreateWorkflow').form({
            inline: true,
            fields: {
                ${WfChecklist.KEY}_title_1: {
                    identifier: '${WfChecklist.KEY}_title_1',
                    rules: [{
                            type: 'empty', prompt: '{name} <g:message code="validation.needsToBeFilledOut" />'
                    }]
                },
            }
        });
    }
    JSPC.app.checkRequired();
</laser:script>