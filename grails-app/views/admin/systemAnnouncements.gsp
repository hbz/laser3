<laser:htmlStart message="menu.admin.announcements" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb message="menu.admin.announcements" class="active"/>
    </ui:breadcrumbs>

    <g:if test="${currentAnnouncement}">
        <ui:h1HeaderWithIcon message="announcement.update.label" />
    </g:if>
    <g:else>
        <ui:h1HeaderWithIcon message="announcement.create.label" />
    </g:else>

    <ui:messages data="${flash}" />

    <ui:form>
        <g:form action="createSystemAnnouncement" class="ui form">
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
                    <g:link controller="admin" action="systemAnnouncements" role="button" class="ui button">${message(code:'default.button.reset.label')}</g:link>
                    <input type="submit" class="ui button" value="${message(code:'default.button.save_changes')}"/>
                </g:if>
                <g:else>
                    <input type="submit" class="ui button" value="${message(code:'announcement.create.button.label')}" />
                </g:else>
            </div>
        </g:form>
    </ui:form>

    <br />
    <h2 class="ui header la-clear-before">${message(code:'announcement.previous.label')}</h2>


    <g:if test="${mailDisabled}">
        <ui:msg class="warning" header="${message(code:'default.hint.label')}" text="${message(code:'system.config.mail.disabled')}" />
    </g:if>
    <g:else>
        <ui:msg class="info" header="${message(code:'default.hint.label')}" text="${message(code:'announcement.recipient.count.info', args:[numberOfCurrentRecipients])}" />
    </g:else>

    <div>
        <g:each in="${announcements}" var="sa">
            <div class="ui segment">
                <h3 class="ui header"><% print sa.title; /* avoid auto encodeAsHTML() */ %></h3>
                <g:if test="${sa.isPublished}">
                    <div class="ui green ribbon label"><i class="ui icon exclamation circle"></i>${message(code:'announcement.published')}</div>
                </g:if>

                <div class="ui divider"></div>
                <div class="content">
                    <% print sa.content; /* avoid auto encodeAsHTML() */ %>
                </div>
                <div class="ui divider"></div>

                <style>
                    table.xyz td { padding: 0 2em 0 0 }
                </style>
                <table class="ui xyz">
                    <g:if test="${sa.lastPublishingDate}">

                        <%
                            def status = [
                                    validUserIds : [],
                                    failedUserIds: []
                            ]

                            if (sa.status) {
                                status = grails.converters.JSON.parse(sa.status)
                            }
                        %>

                        <tr>
                            <td>Zuletzt veröffentlicht</td>
                            <td><g:formatDate date="${sa.lastPublishingDate}" format="${message(code:'default.date.format.noZ')}"/></td>
                            <td>
                                <g:if test="${status['failedUserIds']}">
                                    <span class="ui label">${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                    <span class="ui red label">${status['failedUserIds'].size()} ${message(code:'announcement.sendError.label')}</span>
                                </g:if>
                                <g:else>
                                    <g:if test="${status['validUserIds']}">
                                        <span class="ui green label">${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                    </g:if>
                                    <g:else>
                                        <span class="ui label">${status['validUserIds'].size()} ${message(code:'announcement.recipient.label')}</span>
                                    </g:else>
                                </g:else>
                            </td>
                        </tr>
                    </g:if>

                    <tr>
                        <td>${message(code:'default.lastUpdated.label')}</td>
                        <td><g:formatDate date="${sa.lastUpdated}" format="${message(code:'default.date.format.noZ')}"/></td>
                        <td></td>
                    </tr>

                    <tr>
                        <td>${message(code:'default.dateCreated.label')}</td>
                        <td><g:formatDate date="${sa.dateCreated}" format="${message(code:'default.date.format.noZ')}"/></td>
                        <td></td>
                    </tr>

                    <tr>
                        <td>${message(code:'default.from')}</td>
                        <td><g:link controller="user" action="show" id="${sa.user?.id}">${(sa.user?.displayName)?:sa.user}</g:link></td>
                        <td></td>
                    </tr>

                </table><!-- .grid -->

                <div>
                    <g:if test="${sa.isPublished}">
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'undo']" role="button"
                                class="ui button" onclick="return confirm('${message(code:'announcement.undo.confirm')}')">${message(code:'default.publish_undo.label')}</g:link>
                    </g:if>
                    <g:else>
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'delete']" role="button" class="ui negative icon button la-modern-button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i aria-hidden="true" class="trash alternate outline icon"></i></g:link>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'edit']" role="button" class="ui icon button la-modern-button"><i aria-hidden="true" class="edit icon"></i></g:link>

                        <g:if test="${mailDisabled}">
                            <button class="ui button" disabled="disabled">${message(code:'default.publish.label')}</button>
                        </g:if>
                        <g:else>
                            <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'publish']" role="button"
                                    class="ui button" onclick="return confirm('${message(code:'announcement.publish.confirm')}')">${message(code:'default.publish.label')}</g:link>

                        </g:else>
                     </g:else>
                </div>
            </div>
        </g:each>
    </div>

<laser:htmlEnd />
