<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.system.SystemMessage" %>

<laser:htmlStart message="menu.admin.systemMessages" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.systemMessages" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.systemMessages" type="admin"/>

<div class="la-float-right">
    <a onclick="JSPC.app.systemMessages.edit();" class="${Btn.SIMPLE}" role="button" aria-label="${message(code: 'admin.systemMessage.create')}">
        ${message(code: 'admin.systemMessage.create')}
    </a>
</div>

<br />
<br />

<ui:msg class="info" showIcon="true" hideClose="true">
    ${message(code: 'admin.systemMessage.info')}
    <br />
    <br />
    ${message(code: 'admin.help.markdown')}.
    <br />
    Dabei können folgende Token zur Erzeugung dynamischer Inhalte verwendet werden:
    <br />
    <br />
    <g:each in="${helpService.getTokenMap()}" var="tk">
        &nbsp; {{${tk.key}}} -> ${tk.value} <br />
    </g:each>

</ui:msg>

<ui:messages data="${flash}" />

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code: 'default.content.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'default.activated.label')}</th>
            <th>${message(code: 'default.lastUpdated.label')}</th>
            <th class="center aligned">
                <ui:optionsIcon />
            </th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${systemMessages}" var="msg" status="mi">
        <tr style="vertical-align: top">
            <td>
                <div class="ui top attached segment">
                    <span class="ui mini top right attached label">DE</span>
                    <div>
                        <ui:renderContentAsMarkdown>${msg.content_de}</ui:renderContentAsMarkdown>
                    </div>
                </div>
                <div class="ui attached segment">
                    <span class="ui mini top right attached label">EN</span>
                    <div>
                        <ui:renderContentAsMarkdown>${msg.content_en}</ui:renderContentAsMarkdown>
                    </div>
                </div>
                <g:if test="${msg.condition}">
                    <div class="ui attached segment">
                        <span class="ui mini top right attached label">${message(code: 'default.condition')}</span>
                        <span class="ui text red"><strong>${msg.condition.key}</strong> - ${msg.condition.description} (${msg.condition.systemMessageType})</span>
                    </div>
                </g:if>
            </td>
            <td>
                <g:if test="${SystemMessage.TYPE_GLOBAL == msg.type}">
                    <span class="ui label orange">${message(code: 'systemMessage.TYPE_GLOBAL')}</span>
                </g:if>
                <g:elseif test="${SystemMessage.TYPE_DASHBOARD == msg.type}">
                    <span class="ui label teal">${message(code: 'systemMessage.TYPE_DASHBOARD')}</span>
                </g:elseif>
                <g:elseif test="${SystemMessage.TYPE_STARTPAGE == msg.type}">
                    <span class="ui label blue">${message(code: 'systemMessage.TYPE_STARTPAGE')}</span>
                </g:elseif>
            </td>
            <td>
                <cc:boogle owner="${msg}" field="isActive"/>
            </td>
            <td>
                <g:formatDate date="${msg.lastUpdated}" format="${message(code: 'default.date.format.noZ')}"/>
            </td>
            <td class="x">
                <g:link controller="admin" action="deleteSystemMessage" id="${msg.id}" class="${Btn.MODERN.NEGATIVE}"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="${Icon.CMD.DELETE}"></i>
                </g:link>

                <a onclick="JSPC.app.systemMessages.edit(${msg.id});" class="${Btn.MODERN.SIMPLE}" role="button" aria-label="${message(code: 'ariaLabel.edit.universal')}">
                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                </a>
            </td>
        </tr>
        </g:each>
    </tbody>
</table>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.systemMessages = {

        edit: function (id) {
            $.ajax({
                url: '<g:createLink controller="admin" action="editSystemMessage"/>?id=' + id,
                success: function(result){
                    $('#dynamicModalContainer').empty()
                    $('#modalCreateSystemMessage, #modalEditSystemMessage').remove()

                    $('#dynamicModalContainer').html(result);
                    $('#dynamicModalContainer .ui.modal').modal({
                        autofocus: false,
                        onVisible: function() {
                            r2d2.helper.focusFirstFormElement(this)
                        }
                    }).modal('show')
                }
            });
        },

        updatePreview: function (elem) {
            let id = $(elem).attr('id').replace('_content_', '_preview_')
            let value = $(elem).val()

            $.ajax({
                url: '<g:createLink controller="ajaxHtml" action="renderMarkdown"/>',
                method: 'POST',
                data: {
                    text: value
                },
                success: function(data) {
                    $('.modal #' + id).html(data)
                }
            });
        }
    }
</laser:script>

<laser:htmlEnd />
