
<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>Person</th>
            <g:if test="${tmplShowFunction}">
                <th>Verantwortlichkeit</th>
            </g:if>
            <th>Organisation</th>
            <th class="la-action-info">${message(code:'default.actions.label')}</th>
        </tr>
    </thead>
    <g:each in="${visiblePrsLinks}" var="pr">
        <tr>
            <g:if test="${pr.org}">
                <td>
                    <g:if test="${! pr.prs.isPublic}"><i class="address book outline icon"></i> </g:if>
                    ${pr.prs}
                </td>
                <g:if test="${tmplShowFunction}">
                    <td>
                        <g:if test="${pr.functionType}">
                            ${pr.functionType.getI10n("value")}
                        </g:if>
                        <g:if test="${pr.responsibilityType}">
                            ${pr.responsibilityType.getI10n("value")}
                        </g:if>
                    </td>
                </g:if>
                <td>
                    <g:link controller="organisation" action="show" id="${pr.org.id}">${pr.org.name}</g:link>
                </td>
                <td>
                    <g:if test="${editable}">
                        <g:link controller="ajax" action="delPrsRole" id="${pr.id}" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                role="button"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.contact.organisation")}"
                                data-confirm-term-how="unlink"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash alternate outline icon"></i></g:link>
                    </g:if>
                </td>
            </g:if>
            <g:else>
                <td colspan="3">${message(code:'template.orgLinks.error', args:[pr.id])}</td>
            </g:else>
        </tr>
    </g:each>
</table>

<g:if test="${editable}">
    <input class="ui button"
           value="${message(code: 'default.add.label', args: [message(code: 'person.label')])}"
           data-semui="modal"
           data-href="#prsLinksModal" />
</g:if>

