<%@ page import="de.laser.system.SystemMessageCondition; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.system.SystemMessage" %>

<ui:modal id="${msg ? 'modalEditSystemMessage' : 'modalCreateSystemMessage'}" message="${msg ? 'admin.systemMessage.edit' : 'admin.systemMessage.create'}"
        msgSave="${msg ? message(code:'default.button.save_changes') : message(code:'default.button.create.label')}">
    <g:form class="ui form" url="[controller: 'admin', action: 'systemMessages']" method="post">
        <g:if test="${msg}">
            <input type="hidden" name="cmd" value="edit" />
            <input type="hidden" name="id" value="${msg.id}" />
        </g:if>
        <g:else>
            <input type="hidden" name="cmd" value="create" />
        </g:else>
        <fieldset>
            <div class="field">
                <label for="modal_content_de">${message(code: 'default.content.label')} (${message(code: 'default.german.label')})</label>
                <textarea id="modal_content_de" name="content_de">${msg?.content_de}</textarea>
            </div>
            <div class="field">
                <label for="modal_content_en">${message(code: 'default.content.label')} (${message(code: 'default.english.label')})</label>
                <textarea id="modal_content_en" name="content_en">${msg?.content_en}</textarea>
            </div>

            <div class="field">
                <label for="modal_preview_de">${message(code: 'default.preview.label')} (${message(code: 'default.german.label')})</label>
                <div id="modal_preview_de" class="ui scrolling segment" style="max-height:220px; margin:0; box-shadow:none">
                    <ui:renderContentAsMarkdown>${msg?.content_de}</ui:renderContentAsMarkdown>
                </div>
            </div>
            <div class="field">
                <label for="modal_preview_en">${message(code: 'default.preview.label')} (${message(code: 'default.english.label')})</label>
                <div id="modal_preview_en" class="ui scrolling segment" style="max-height:220px; margin:0; box-shadow:none">
                    <ui:renderContentAsMarkdown>${msg?.content_en}</ui:renderContentAsMarkdown>
                </div>
            </div>
            <div class="two fields">
                <div class="field">
                    <label for="type">${message(code: 'default.type.label')}</label>
                    <g:select from="${[[SystemMessage.TYPE_GLOBAL, g.message(code: 'systemMessage.TYPE_GLOBAL')],
                                       [SystemMessage.TYPE_DASHBOARD, g.message(code: 'systemMessage.TYPE_DASHBOARD')],
                                       [SystemMessage.TYPE_STARTPAGE, g.message(code: 'systemMessage.TYPE_STARTPAGE')]
                        ]}"
                              optionKey="${{it[0]}}"
                              optionValue="${{it[1]}}"
                              id="modal_type"
                              name="type"
                              value="${msg?.type}"
                              class="ui fluid search dropdown la-not-clearable"/>
                </div>
                <div class="field">
                    <label for="type">${message(code: 'default.condition')}</label>
                    <g:select from="${SystemMessageCondition.CONFIG.collect{ [it.key, it.key + ' - ' + it.description + ' (' + it.systemMessageType + ')'] }}"
                              optionKey="${{it[0]}}"
                              optionValue="${{it[1]}}"
                              id="modal_condition"
                              name="condition"
                              value="${msg?.condition}"
                              noSelection="${['' : 'Keine Bedingung verwenden']}"
                              class="ui fluid search dropdown"/>
                </div>
            </div>
        </fieldset>
    </g:form>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.modal textarea[id^=modal_content_]').on('change', function() {
        JSPC.app.systemMessages.updatePreview(this)
    });
</laser:script>
