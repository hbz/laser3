<%@ page import="de.laser.ui.Btn; de.laser.auth.User; de.laser.DeletionService" %>

<laser:htmlStart message="profile" serviceInjection="true" />

    <ui:breadcrumbs>
        <ui:crumb message="profile.bc.profile" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="profile" />

    <ui:messages data="${flash}" />

    <g:if test="${delResult}">

        <g:if test="${delResult.status == DeletionService.RESULT_CUSTOM}">
            <ui:msg class="error" header="${message(code: 'deletion.blocked.header')}" message="deletion.custom.msg.user" />
        </g:if>
        <g:if test="${delResult.status == DeletionService.RESULT_BLOCKED}">
            <ui:msg class="error" header="${message(code: 'deletion.blocked.header')}" message="deletion.blocked.msg.user" />
        </g:if>
        <g:if test="${delResult.status == DeletionService.RESULT_ERROR}">
            <ui:msg class="error" header="${message(code: 'deletion.error.header')}" message="deletion.error.msg" />
        </g:if>

        <g:form controller="profile" action="delete" data-confirm-id="deleteProfile_form" params="${[id: user.id, process: true]}">

            <g:link controller="profile" action="index" class="${Btn.SIMPLE}">${message(code: 'default.button.cancel.label')}</g:link>

            <g:if test="${delResult.deletable}">
                <g:if test="${delResult.status == DeletionService.RESULT_SUBSTITUTE_NEEDED}">
                    <g:if test="${substituteList}">
                        <div class="${Btn.NEGATIVE_CONFIRM}" data-confirm-id="deleteProfile"
                             data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.profile")}" data-confirm-term-how="delete">
                                ${message(code:'deletion.user')}
                        </div>

                        <div class="ui segment">
                            ${message(code:'user.delete.moveToNewUser')}
                            &nbsp;
                            <g:select id="userReplacement" name="userReplacement" class="ui dropdown selection la-not-clearable"
                                      from="${substituteList}"
                                      optionKey="id"
                                      optionValue="${{'(' + it.username + ') ' + it.displayName}}" />
                        </div>
                    </g:if>
                    <g:else>
                        <input disabled type="submit" class="${Btn.NEGATIVE}" value="${message(code:'deletion.user')}" />
                        <ui:msg class="error" header="${message(code: 'deletion.blocked.header')}" message="user.delete.substitute.missing" />
                    </g:else>
                </g:if>
                <g:elseif test="${delResult.status != DeletionService.RESULT_ERROR}">
                    <div class="${Btn.NEGATIVE_CONFIRM}" data-confirm-id="deleteProfile"
                         data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.profile")}" data-confirm-term-how="delete">
                            ${message(code:'deletion.user')}
                    </div>
                </g:elseif>
            </g:if>
            <g:else>
                <input disabled type="submit" class="${Btn.NEGATIVE}" value="${message(code:'deletion.user')}" />
            </g:else>

        </g:form>

        <%-- --%>

        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th>Objekte, Referenzen</th>
                <th>${message(code:'default.count.label')}</th>
                <th>Objekt-Ids</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${delResult.info.sort{ a,b -> a[0] <=> b[0] }}" var="info">
                <tr>
                    <td>
                        ${info[0]}
                    </td>
                    <td style="text-align:center">
                        <g:if test="${info.size() > 2 && info[1].size() > 0}">
                            <span class="ui circular label la-popup-tooltip la-delay ${info[2]}"
                                <g:if test="${info[2] == DeletionService.FLAG_WARNING}">
                                    data-content="${message(code:'user.delete.warning')}"
                                </g:if>
                                <g:if test="${info[2] == DeletionService.FLAG_SUBSTITUTE}">
                                    data-content="${message(code:'user.delete.substitute')}"
                                </g:if>
                                <g:if test="${info[2] == DeletionService.FLAG_BLOCKER}">
                                    data-content="${message(code:'user.delete.blocker')}"
                                </g:if>
                            >${info[1].size()}</span>
                        </g:if>
                        <g:else>
                            ${info[1].size()}
                        </g:else>
                    </td>
                    <td>
                        <div style="overflow-y:scroll;scrollbar-color:grey white;max-height:14.25em">
                            ${info[1].collect{ item -> (item.hasProperty('id') && item.id) ? item.id : item}.sort().join(', ')}
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <%-- --%>

        <br />

        <div class="ui list">
            <div class="item">
                <span class="ui circular label yellow">1</span>
                <span class="content">
                    ${message(code:'user.delete.warning')}
                </span>
            </div>
            <div class="item">
                <span class="ui circular label teal">2</span>
                <span class="content">
                    ${message(code:'user.delete.substitute')}
                </span>
            </div>
            <div class="item">
                <span class="ui circular label red">3</span>
                <span class="content">
                    ${message(code:'user.delete.blocker')}
                </span>
            </div>
        </div>

    </g:if>

<laser:htmlEnd />
