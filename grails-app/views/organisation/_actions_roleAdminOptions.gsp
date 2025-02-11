<%@ page import="de.laser.ui.Icon;" %>
<laser:serviceInjection/>

<sec:ifAnyGranted roles="ROLE_ADMIN">
    <g:if test="${actionName in ['show']}">
        <div class="divider"></div>
        <g:link action="disableAllUsers" id="${params.id}" class="item js-open-confirm-modal la-popup-tooltip"
                data-confirm-tokenMsg="${message(code: "confirm.dialog.disable.allInstUsers")}" data-confirm-term-how="ok">
            <i class="user lock icon"></i> ${message(code:'org.disableAllUsers.label')}
        </g:link>
        <g:link action="markAsArchive" id="${params.id}" class="item js-open-confirm-modal la-popup-tooltip ${org.isArchived() ? 'disabled' : ''}"
                data-confirm-tokenMsg="${message(code: "confirm.dialog.disable.org")}" data-confirm-term-how="ok">
            <i class="exclamation triangle icon"></i> ${message(code:'org.markAsArchive.label')}
        </g:link>
        <g:link action="delete" id="${params.id}" class="item"><i class="${Icon.CMD.DELETE}"></i>
            ${message(code: 'deletion.institution')}
        </g:link>
    </g:if>
</sec:ifAnyGranted>

