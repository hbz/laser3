<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils" %>
<laser:htmlStart message="menu.yoda.userRoleMatrix" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.userRoleMatrix" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.userRoleMatrix" type="yoda" />

<div class="ui fluid card">
    <div class="content">

<div class="ui accordion">
    <g:each in="${matrix}" var="role">
        <g:if test="${role.value.size() > 0}">
            <div class="title">
                <strong><i class="dropdown icon"></i> ${role.key} ( ${role.value.size()} Nutzer )</strong>
            </div>
            <div class="content">
                <table class="ui celled la-js-responsive-table la-table compact table">
                    <tbody>
                        <g:each in="${role.value.toSorted{a,b -> a.username <=> b.username}}" var="user">
                            <tr>
                                <td>${user.username}</td>
                                <td>${user.display}</td>
                                <td>${user.email}</td>
                                <td>${user.lastLogin ? DateUtils.getLocalizedSDF_noTime().format(user.lastLogin):''}</td>
                                <td>
                                    <ui:booleanIcon value="${user.enabled}"/>
                                    <g:if test="${user.enabled}">
                                        ${message(code:'user.enabled.label')}
                                    </g:if>
                                    <g:else>
                                        ${message(code:'user.accountDisabled.label')}
                                    </g:else>
                                </td>
%{--                                <td>--}%
%{--                                    <label>${message(code:'user.accountExpired.label')}</label>--}%
%{--                                    <ui:booleanIcon value="${user.accountExpired}"/>--}%

%{--                                    <label>${message(code:'user.accountLocked.label')}</label>--}%
%{--                                    <ui:booleanIcon value="${user.accountLocked}"/>--}%

%{--                                    <label>${message(code:'user.passwordExpired.label')}</label>--}%
%{--                                    <ui:booleanIcon value="${user.passwordExpired}"/>--}%
%{--                                </td>--}%
                                <td class="x">
                                    <g:link controller="user" action="edit" id="${user.id}" class="${Btn.ICON.SIMPLE}">
                                        <i class="${Icon.CMD.EDIT}"></i>
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>
    </g:each>
</div>

    </div>
</div>

<laser:htmlEnd />