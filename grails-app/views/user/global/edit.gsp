<%@ page import="de.laser.auth.UserRole;de.laser.Org;de.laser.auth.Role" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : <g:message code="user.edit.label" /></title>
</head>
<body>
        <laser:render template="/user/global/breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <laser:render template="/user/global/actions" />
        </semui:controlButtons>

        <semui:h1HeaderWithIcon message="user.edit.label" />
        <h2 class="ui header la-noMargin-top">${user.username}</h2>

    <semui:messages data="${flash}" />

    <div class="ui two column grid">

        <div class="column wide eight">
            <div class="ui segment form">

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
                    <semui:xEditable owner="${user}" field="email" validation="email"/>
                </div>

                <g:if test="${editable}">

                    <div class="ui field">
                        <label>${message(code:'user.enabled.label')}</label>
                        <semui:xEditableBoolean owner="${user}" field="enabled" />
                    </div>

                    <g:form controller="user" action="newPassword" params="${[id: user.id]}">
                        <div class="ui two fields">
                            <div class="ui field">
                                <label>${message(code:'user.password.label')}</label>
                                <input type="submit" class="ui button orange" value="${message(code:'user.newPassword.text')}">
                            </div>
                        </div>
                    </g:form>

                </g:if>

            </div>
        </div><!-- .column -->


        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <div class="column wide eight">
                <div class="ui segment form">

                    <table class="ui celled la-js-responsive-table la-table compact table">
                        <thead>
                        <tr>
                            <th>${message(code:'default.role.label')}</th>
                            <th class="la-action-info">${message(code:'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${user.roles}" var="rl">
                            <tr>
                                <td>${rl.role.authority}</td>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link controller="ajax" action="removeUserRole" params='${[user:"${user.class.name}:${user.id}",role:"${rl.role.class.name}:${rl.role.id}"]}'
                                                class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                role="button"
                                                data-confirm-tokenMsg="${message(code:'default.button.confirm.delete')}"
                                                data-confirm-term-how="delete"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="trash alternate outline icon"></i>
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
                                    <g:form class="ui form" controller="ajax" action="addToCollection">
                                        <input type="hidden" name="__context" value="${user.class.name}:${user.id}"/>
                                        <input type="hidden" name="__newObjectClass" value="${UserRole.class.name}"/>
                                        <input type="hidden" name="__recip" value="user"/>
                                        <div class="ui field">
                                            <input type="hidden" name="role" id="userRoleSelect"/>
                                            <input type="submit" class="ui button" value="${message(code:'user.role.add')}"/>
                                        </div>
                                    </g:form>
                                </td>
                            </tr>
                            </tfoot>
                        </g:if>
                    </table>

                    <laser:script file="${this.getGroovyPageFileName()}">
                        $("#userRoleSelect").select2({
                          placeholder: "${message(code:'user.role.search.ph')}",
                                minimumInputLength: 0,
                                formatInputTooShort: function () {
                                    return "${message(code:'select2.minChars.note')}";
                                },
                                ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                                  url: "<g:createLink controller='ajaxJson' action='lookup'/>",
                                  dataType: 'json',
                                  data: function (term, page) {
                                      return {
                                          q: term, // search term
                                          page_limit: 10,
                                          baseClass: '${Role.class.name}'
                                      };
                                  },
                                  results: function (data, page) {
                                    return {results: data.values};
                                  }
                                }
                              });
                    </laser:script>

                </div>
            </div><!-- .column -->
        </sec:ifAnyGranted>

        <g:if test="${manipulateAffiliations}">
            <div class="sixteen wide column">
                <div class="la-inline-lists">
                    <laser:render template="/templates/user/membership_table" model="[userInstance: user]" />
                </div>
            </div>
        </g:if>
    </div><!-- grid -->

    <g:if test="${editable}">
        <g:if test="${availableComboDeptOrgs}">
            <g:set var="availableOrgs" value="${availableComboDeptOrgs}" />
            %{-- TODO: overwrite for ROLE_ADMIN ? all available Orgs --}%
        </g:if>
        %{-- not found -- <g:elseif test="${availableComboConsOrgs}">
            <g:set var="orgLabel" value="Teilnehmer" />
            <g:set var="availableOrgs" value="${availableComboConsOrgs}" />
        </g:elseif> --}%

        <g:if test="${ availableOrgs }">
            <g:if test="${controllerName == 'user' || (controllerName in ['myInstitution', 'organisation'] && ! user.hasInstMemberAffiliation(orgInstance))}">
                <div class="ui segment form">

                    <g:form controller="${controllerName}" action="addAffiliation" class="ui form" method="get">

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

                        <div class="two fields">
                            <div class="field">
                                <label for="org">${orgLabel ?: message(code:'org.label')}</label>
                                <g:select name="org" id="org"
                                          from="${availableOrgs}"
                                          optionKey="id"
                                          optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                                          class="ui fluid search dropdown"/>
                            </div>

                            <div class="field">
                                <label for="formalRole">${message(code:'default.role.label')}</label>
                                <g:select name="formalRole" id="formalRole"
                                          from="${Role.findAllByRoleType('user')}"
                                          optionKey="id"
                                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                                          value="${Role.findByAuthority('INST_USER').id}"
                                          class="ui fluid dropdown"/>
                            </div>
                        </div>

                        <div class="field">
                            <button type="submit" class="ui button">${message(code: 'profile.membership.add.button')}</button>
                        </div>
                    </g:form>

                </div>
            </g:if>

        </g:if>
    </g:if>

</body>
</html>
