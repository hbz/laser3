<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart message="menu.admin.announcements" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb message="menu.admin.announcements" class="active"/>
    </ui:breadcrumbs>

    <g:if test="${currentAnnouncement}">
        <ui:h1HeaderWithIcon message="announcement.update.label" type="admin"/>
    </g:if>
    <g:else>
        <ui:h1HeaderWithIcon message="announcement.create.label" type="admin"/>
    </g:else>

    <ui:messages data="${flash}" />

    <g:if test="${mailDisabled}">
        <ui:msg class="warning" header="${message(code:'default.hint.label')}" message="system.config.mail.disabled" />
    </g:if>
    <g:else>
        <ui:msg class="info" header="${message(code:'default.hint.label')}" text="${message(code:'announcement.recipient.count.info', args:[numberOfCurrentRecipients])}" />
    </g:else>

        <ui:form controller="admin" action="createSystemAnnouncement">
            <input type="hidden" name="saId" value="${currentAnnouncement?.id}">
            <div class="field">
                <label for="saTitle">${message(code:'announcement.subject.label')}</label>
                <input type="text" id="saTitle" name="saTitle" value="${currentAnnouncement?.title}" />
            </div>
            <div class="field">
                <label for="saContent">${message(code:'default.content.label')}</label>
                <textarea id="saContent" name="saContent">${currentAnnouncement?.content}</textarea>
            </div>
            <div class="ui field">
                <label for="saPreview">${message(code:'announcement.preview.label')}</label>
                <textarea id="saPreview" name="saPreview" readonly="readonly">${currentAnnouncement?.getCleanTitle()}

${currentAnnouncement?.getCleanContent()}
                </textarea>
            </div>

            <laser:script file="${this.getGroovyPageFileName()}">
                JSPC.app.updateSysAnnPreview = function() {
                    $('#saPreview').text(
                        $('form #saTitle').val().replace(/<.*?>/gm,"") + '\n\n' +
                        $('form #saContent').val().replace(/<.*?>/gm,"")
                    )
                }
                $('form #saTitle').on('change', function(){
                    JSPC.app.updateSysAnnPreview()
                })
                $('form #saContent').on('change', function(){
                    JSPC.app.updateSysAnnPreview()
                })
            </laser:script>

            <div class="field">
                <g:if test="${currentAnnouncement}">
                    <g:link controller="admin" action="systemAnnouncements" role="button" class="${Btn.SIMPLE}">${message(code:'default.button.reset.label')}</g:link>
                    <input type="submit" class="${Btn.SIMPLE}" value="${message(code:'default.button.save_changes')}"/>
                </g:if>
                <g:else>
                    <input type="submit" class="${Btn.SIMPLE}" value="${message(code:'announcement.create.button.label')}" />
                </g:else>
            </div>
        </ui:form>

    <br />
    <h2 class="ui header la-clear-before">${message(code:'announcement.previous.label')}</h2>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
            <tr>
                <th class="ten wide">Ankündigung</th>
                <th class="four wide center aligned">Info</th>
                <th class="two wide center aligned"><i class="${Icon.SYM.OPTIONS}"></i></th>
            </tr>
        </thead>
        <tbody>

        <g:each in="${announcements}" var="sa">
            <tr>
                <td style="vertical-align: top">

                    <g:if test="${sa.isPublished}">
                        <div class="ui green label"><i class="${Icon.ANNOUNCEMENT}"></i>${message(code:'announcement.published')}</div>
                    </g:if>

                    <div class="ui header">
                        <% print sa.title; /* avoid auto encodeAsHTML() */ %>
                    </div>
                    <div class="content">
                        <% print sa.content; /* avoid auto encodeAsHTML() */ %>
                    </div>

                    <g:if test="${sa.lastPublishingDate}">
                        <div class="content">
                            <%
                                def status = [ validUserIds : [], failedUserIds: [] ]
                                if (sa.status) { status = grails.converters.JSON.parse(sa.status) }
                            %>

                            <span class="la-popup-tooltip" data-position="top left" data-content="Zuletzt veröffentlicht">
                                <i class="${Icon.ANNOUNCEMENT} la-list-icon"></i>
                                <g:formatDate date="${sa.lastPublishingDate}" format="${message(code:'default.date.format.noZ')}"/>
                            </span>

                            <g:if test="${status['failedUserIds']}">
                                <span class="ui grey text"><icon:arrow/>${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                <span class="ui red text"><icon:arrow/>${status['failedUserIds'].size()} ${message(code:'announcement.sendError.label')}</span>
                            </g:if>
                            <g:else>
                                <g:if test="${status['validUserIds']}">
                                    <span class="ui green text"><icon:arrow/>${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                </g:if>
                                <g:else>
                                    <span class="ui grey text"><icon:arrow/>${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                </g:else>
                            </g:else>
                        </div>
                    </g:if>
                </td>
                <td>
                    <span class="la-popup-tooltip" data-position="top left" data-content="${message(code:'default.lastUpdated.label')}">
                        <i class="icon pencil alternate la-list-icon"></i>
                        <g:formatDate date="${sa.lastUpdated}" format="${message(code:'default.date.format.notime')}"/>
                    </span>

                    <br />

                    <span class="la-popup-tooltip" data-position="top left" data-content="Erstellungsdatum">
                        <i class="icon plus circle la-list-icon"></i>
                        <g:formatDate date="${sa.dateCreated}" format="${message(code:'default.date.format.notime')}"/>
                        <br />
                        ${message(code:'default.from')} <g:link controller="user" action="show" id="${sa.user?.id}">${(sa.user?.displayName)?:sa.user}</g:link>
                    </span>
                </td>
                <td>
                    <g:if test="${sa.isPublished}">
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'undo']"
                                role="button" class="${Btn.SIMPLE}"
                                onclick="return confirm('${message(code:'announcement.undo.confirm')}')">
                            ${message(code:'default.publish_undo.label')}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'delete']"
                                role="button" class="${Btn.MODERN.NEGATIVE}"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.DELETE}"></i>
                        </g:link>

                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'edit']"
                                role="button" class="${Btn.MODERN.SIMPLE}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </g:link>

                        <br />
                        <br />

                        <g:if test="${mailDisabled}">
                            <button class="${Btn.SIMPLE}" disabled="disabled">${message(code:'default.publish.label')}</button>
                        </g:if>
                        <g:else>
                            <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'publish']"
                                    role="button" class="${Btn.SIMPLE}"
                                    onclick="return confirm('${message(code:'announcement.publish.confirm')}')">
                                ${message(code:'default.publish.label')}
                            </g:link>
                        </g:else>
                    </g:else>
                </td>
            <tr>
        </g:each>

        </tbody>
    </table>

    <style>
        span.la-popup-tooltip:hover { cursor:help }
        table .content { margin-top: 2em }
    </style>
<laser:htmlEnd />
