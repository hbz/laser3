<% /* <g:if test="${editable}"> </g:if> */ %>
<semui:actionsDropdown>
    <semui:actionsDropdownItem controller="packageDetails" action="compare" message="menu.institutions.comp_pkg" />

    <g:if test="${actionName == 'show'}">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_PACKAGE_EDITOR">
            <g:link class="item" controller="announcement" action="index" params='[at:"Package Link: ${pkg_link_str}",as:"RE: Package ${packageInstance.name}"]'>${message(code: 'package.show.announcement')}</g:link>
        </sec:ifAnyGranted>
    </g:if>

</semui:actionsDropdown>