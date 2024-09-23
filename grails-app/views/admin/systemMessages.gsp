<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.system.SystemMessage; de.laser.jobs.HeartbeatJob" %>

<laser:htmlStart message="menu.admin.systemMessage" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.systemMessage" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.systemMessage" type="admin"/>

<div class="la-float-right">
    <input type="submit" class="${Btn.SIMPLE}" value="${message(code: 'admin.systemMessage.create')}" data-ui="modal" data-href="#modalCreateSystemMessage" />
</div>

<br />
<br />

<ui:msg class="info" hideClose="true">
    <icon:pointingHand /> ${message(code: 'admin.systemMessage.info.TMP', args: [HeartbeatJob.HEARTBEAT_IN_SECONDS])}
</ui:msg>

<ui:messages data="${flash}" />

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code: 'default.content.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'default.activated.label')}</th>
            <th>${message(code: 'default.lastUpdated.label')}</th>
            <th class="la-action-info">${message(code:'default.actions.label')}</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${systemMessages}" var="msg" status="mi">
        <tr style="vertical-align: top">
            <td>
                <div class="ui top attached segment">
                    <span class="ui mini top right attached label">DE</span>
                    <ui:xEditable owner="${msg}" field="content_de" type="textarea"/>
                </div>
                <div class="ui attached segment">
                    <span class="ui mini top right attached label">EN</span>
                    <ui:xEditable owner="${msg}" field="content_en" type="textarea"/>
                </div>
                <div class="ui top attached segment">
%{--                    <span class="ui top attached label">${message(code: 'default.preview.label')}</span>--}%
                    <div id="preview_de_${mi}">
                        <ui:renderContentAsMarkdown>${msg.content_de}</ui:renderContentAsMarkdown>
                    </div>
                </div>
                <div class="ui attached segment">
                    <div id="preview_en_${mi}">
                        <ui:renderContentAsMarkdown>${msg.content_en}</ui:renderContentAsMarkdown>
                    </div>
                </div>
            </td>
            <td>
                <g:if test="${SystemMessage.TYPE_ATTENTION == msg.type}">
                    <span class="ui label yellow">Systemmeldung</span>
                </g:if>
                <g:if test="${SystemMessage.TYPE_STARTPAGE_NEWS == msg.type}">
                    <span class="ui label blue">Startseite</span>
                </g:if>
            </td>
            <td>
                <ui:xEditableBoolean owner="${msg}" field="isActive"/>
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
            </td>
        </tr>
        </g:each>
    </tbody>
</table>

<ui:modal id="modalCreateSystemMessage" message="admin.systemMessage.create">
    <g:form class="ui form" url="[controller: 'admin', action: 'systemMessages', params: [create: true]]" method="post">

        <fieldset>
            <div class="field">
                <label for="content_de">${message(code: 'default.content.label')} (${message(code: 'default.german.label')})</label>
                <textarea name="content_de" id="content_de"></textarea>
            </div>

            <div class="field">
                <label for="content_en">${message(code: 'default.content.label')} (${message(code: 'default.english.label')})</label>
                <textarea name="content_en" id="content_en"></textarea>
            </div>

            <div class="field">
                <label for="type">${message(code: 'default.type.label')}</label>
                <g:select from="${[[SystemMessage.TYPE_ATTENTION, 'Systemmeldung'], [SystemMessage.TYPE_STARTPAGE_NEWS, 'Startseite']]}"
                          optionKey="${{it[0]}}"
                          optionValue="${{it[1]}}"
                          name="type"
                          class="ui fluid search dropdown"/>
            </div>
        </fieldset>
    </g:form>
</ui:modal>

<laser:htmlEnd />
