<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.auth.UserRole;de.laser.Org;de.laser.auth.Role;de.laser.utils.DateUtils;" %>
<laser:htmlStart message="user.edit.label" />

        <laser:render template="/user/global/breadcrumb" model="${[ params:params ]}"/>

        <ui:controlButtons>
            <laser:render template="/user/global/actions" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon message="user.edit.label" type="user" />
        <h2 class="ui header la-noMargin-top">${user.username}</h2>

    <ui:messages data="${flash}" />

    <div class="ui two column grid la-clear-before">

        <div class="column wide eight">
            <div class="la-inline-lists">

                <div class="ui card">
                    <div class="ui content">
                        <h2 class="ui dividing header">${message(code: 'profile.user')}</h2>

                        <div class="ui form">
                            <div class="ui field">
                                <label for="username">${message(code:'user.username.label')}</label>
                                <input id="username" type="text" readonly="readonly" value="${user.username}">
                            </div>

                            <div class="ui field">
                                <label>${message(code:'user.displayName.label')}</label>
                                <g:if test="${editable}">
                                    <span id="displayEdit"
                                          class="xEditableValue"
                                          data-type="text"
                                          data-pk="${user.class.name}:${user.id}"
                                          data-name="display"
                                          data-url='<g:createLink controller="ajax" action="editableSetValue"/>'
                                          data-original-title="${user.display}">${user.display}
                                    </span>
                                </g:if>
                                <g:else>
                                    ${user.display}
                                </g:else>
                            </div>

                            <div class="ui field">
                                <label>${message(code:'user.email')}</label>
                                <ui:xEditable owner="${user}" field="email" validation="email,notEmpty"/>
                            </div>

                            <g:if test="${editable}">
                                <div class="ui field">
                                    <label>${message(code:'user.enabled.label')}</label>
                                    <cc:boogle owner="${user}" field="enabled" />
                                </div>
                            </g:if>

                        </div><!-- .form -->
                    </div><!-- .content -->
                </div><!-- .card -->

                <g:if test="${editable}">
                    <div class="ui card">
                        <div class="ui content">
                           <h2 class="ui dividing header">${message(code: 'mail.sendMail.label')}</h2>

                            <div class="ui form">
                                <g:form controller="user" action="newPassword" params="${[id: user.id]}">
                                    <div class="ui two fields">
                                        <div class="ui field">
                                            <label>${message(code:'user.password.label')}</label>
                                            <input type="submit" class="${Btn.SIMPLE} orange" value="${message(code:'user.newPassword.text')}">
                                        </div>
                                    </div>
                                </g:form>

                                <g:form controller="user" action="sendUsername" params="${[id: user.id]}">
                                    <div class="ui two fields">
                                        <div class="ui field">
                                            <label>${message(code:'user.username.label')}</label>
                                            <input type="submit" class="${Btn.SIMPLE} orange" value="${message(code:'menu.user.forgottenUsername.send')}">
                                        </div>
                                    </div>
                                </g:form>
                            </div><!-- .form -->
                        </div><!-- .content -->
                    </div><!-- .card -->
                </g:if>

                <g:if test="${contextService.getUser().isYoda() || contextService.getUser().isAdmin()}">
                    <div class="ui card">
                        <div class="ui content">
                            <ui:cardLabelAdminOnly />

                            <div class="ui form">
                                <div class="ui field">
                                    <label>
                                        ${message(code:'user.accountLocked.label')}
                                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.loginBlocked.label')}: ${message(code:'springSecurity.errors.login.locked')}">
                                            <i class="${Icon.TOOLTIP.HELP}"></i>
                                        </span>
                                    </label>
                                    <cc:boogle owner="${user}" field="accountLocked" overwriteEditable="true"/>
                                </div>
%{--                                <div class="ui field">--}%
%{--                                    <label>--}%
%{--                                        ${message(code:'user.passwordExpired.label')}--}%
%{--                                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.loginBlocked.label')}: ${message(code:'springSecurity.errors.login.passwordExpired')}">--}%
%{--                                            <i class="${Icon.TOOLTIP.HELP}"></i>--}%
%{--                                        </span>--}%
%{--                                    </label>--}%
%{--                                    <ui:xEditableBoolean owner="${user}" field="passwordExpired" overwriteEditable="true"/>--}%
%{--                                </div>--}%
                                <div class="ui field">
                                    <label>${message(code:'user.invalidLoginAttempts.label')}</label>
                                    <p>${user.invalidLoginAttempts ?: 0}</p>
                                </div>
                                <div class="ui field">
                                    <label>
                                        ${message(code:'user.accountExpired.label')}
                                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.loginBlocked.label')}: ${message(code:'springSecurity.errors.login.expired')}">
                                            <i class="${Icon.TOOLTIP.HELP}"></i>
                                        </span>
                                    </label>
                                    <ui:xEditableBoolean owner="${user}" field="accountExpired" overwriteEditable="false"/>
                                </div>
                                <div class="ui field">
                                    <label>${message(code:'user.inactivityWarning.label')}</label>
                                    <p>${user.inactivityWarning ? DateUtils.getLocalizedSDF_noZ().format(user.inactivityWarning) : '-'}</p>
                                </div>
                                <div class="ui field">
                                    <label>${message(code:'user.lastLogin.label')}</label>
                                    <p>${user.lastLogin ? DateUtils.getLocalizedSDF_noZ().format(user.lastLogin) : '-'}</p>
                                </div>
                                <div class="ui field">
                                    <label>${message(code:'default.lastUpdated.label')}</label>
                                    <p>${DateUtils.getLocalizedSDF_noZ().format(user.lastUpdated)}</p>
                                </div>
                            </div><!-- .form -->
                        </div><!-- .content -->
                    </div><!-- .card -->
                </g:if>

            </div>
        </div><!-- .column -->

        <div class="eight wide column">
            <div class="la-inline-lists">
                <g:if test="${manipulateAffiliations && user.formalOrg}">
                    <laser:render template="/templates/user/membership_table" model="[userInstance: user]" />
                </g:if>

                <g:if test="${editable}">
                    <g:if test="${availableComboDeptOrgs}">
                        <g:set var="availableOrgs" value="${availableComboDeptOrgs}" />
                    %{-- TODO: overwrite for ROLE_ADMIN ? all available Orgs --}%
                    </g:if>

                    <g:if test="${ availableOrgs && ! user.formalOrg}">
                        <g:if test="${controllerName == 'user' || (controllerName in ['myInstitution', 'organisation'] && ! user.isFormal(orgInstance))}">
                            <div class="ui card">
                                <div class="ui content">
                                    <h2 class="ui dividing header">${message(code: 'profile.membership.existing')}</h2>

                                    <ui:msg class="warning" showIcon="true" hideClose="true" text="Dieser Nutzer ist noch keiner Einrichtung zugewiesen." />

                                    <div class="ui form">
                                        <g:form controller="${controllerName}" action="setAffiliation" class="ui form" method="get">

                                            <g:if test="${controllerName == 'myInstitution'}">
                                                <input type="hidden" name="uoid" value="${genericOIDService.getOID(user)}" />
                                            </g:if>
                                            <g:if test="${controllerName == 'organisation'}">
                                                <input type="hidden" name="uoid" value="${genericOIDService.getOID(user)}" />
                                                <input type="hidden" name="id" value="${orgInstance.id}" />
                                            </g:if>
                                            <g:if test="${controllerName == 'user'}">
                                                <input type="hidden" name="id" value="${user.id}" />
                                            </g:if>

                                            <div class="field">
                                                <label for="org">${orgLabel ?: message(code:'org.label')}</label>
                                                <g:select name="org" id="org"
                                                          from="${availableOrgs}"
                                                          optionKey="id"
                                                          optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                                                          value="${params.org ?: orgInstance?.id}"
                                                          class="ui fluid search dropdown la-not-clearable"/>
                                            </div>
                                            <div class="field">
                                                <label for="formalRole">${message(code:'default.role.label')}</label>
                                                <g:select name="formalRole" id="formalRole"
                                                          from="${Role.findAllByRoleType('user')}"
                                                          optionKey="id"
                                                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                                          value="${Role.findByAuthority('INST_USER').id}"
                                                          class="ui fluid dropdown la-not-clearable"/>
                                            </div>

                                            <div class="field">
                                                <button type="submit" class="${Btn.SIMPLE}">${message(code: 'profile.membership.add.button')}</button>
                                            </div>
                                        </g:form>
                                    </div><!-- .form -->
                                </div><!-- .content -->
                            </div><!-- .card -->
                        </g:if>
                    </g:if>
                </g:if>

                <sec:ifAnyGranted roles="ROLE_ADMIN">
                    <div class="ui card">
                        <div class="ui content">
                            <h2 class="ui dividing header">Systemberechtigung</h2>
                            <div class="ui form">

                                <table class="ui celled la-js-responsive-table la-table compact table">
                                    <thead>
                                        <tr>
                                            <th>${message(code:'default.role.label')}</th>
                                            <th class="center aligned">
                                                <ui:optionsIcon />
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each in="${user.roles}" var="rl">
                                            <tr>
                                                <td>${rl.role.authority}</td>
                                                <td class="x">
                                                    <g:if test="${editable}">
                                                        <g:link controller="ajax" action="removeUserRole" params='${[user:"${user.id}",role:"${rl.role.id}"]}'
                                                                class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                role="button"
                                                                data-confirm-tokenMsg="${message(code:'confirm.dialog.unlink.user.role')}"
                                                                data-confirm-term-how="unlink"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="${Icon.CMD.DELETE}"></i>
                                                        </g:link>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                    </tbody>
                                    <g:if test="${editable}">
                                        <tfoot>
                                            <tr>
                                                <td colspan="2">
                                                    <g:form class="ui form" controller="ajax" action="addUserRole">
                                                        <input type="hidden" name="user" value="${user.id}"/>
                                                        <div class="ui fields">
                                                            <div class="field">
                                                                <g:select from="${Role.findAllByRoleType('global')}"
                                                                          class="ui dropdown clearable fluid"
                                                                          name="role"
                                                                          optionKey="${{ it.id }}"
                                                                          optionValue="${{ it.getI10n('authority') }}"
                                                                          noSelection="${['': message(code: 'default.select.choose.label')]}"
                                                                />
                                                            </div>
                                                            <div class="field">
                                                                <input type="submit" class="${Btn.SIMPLE}" value="${message(code:'user.role.add')}"/>
                                                            </div>
                                                        </div>
                                                    </g:form>
                                                </td>
                                            </tr>
                                        </tfoot>
                                    </g:if>
                                </table>

                            </div><!-- .form -->
                        </div><!-- .content -->
                    </div><!-- .card -->
                </sec:ifAnyGranted>
            </div><!-- .column -->
        </div><!-- .column -->

    </div><!-- grid -->

<laser:htmlEnd />
