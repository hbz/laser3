<g:each in="${roleLinks}" var="role">
    <g:if test="${role.org}">
        <dl>
            <dt><label class="control-label">${role?.roleType?.getI10n("value")}</label></dt>
            <dd>
                <g:link controller="Organisations" action="show" id="${role.org.id}">${role?.org?.name}</g:link>

                <g:if test="${editmode}">
                    <br />
                    (<g:link controller="ajax" action="delOrgRole" id="${role.id}"
                            onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})">
                        <i class="unlinkify icon red"></i>
                        ${message(code:'default.button.unlink.label')}
                    </g:link>)
                </g:if>
            </dd>
        </dl>
    </g:if>
</g:each>

<g:if test="${editmode}">
    <dl>
        <dt></dt>
        <dd>
            <a class="ui button" data-semui="modal" href="#osel_add_modal" >${message(code:'license.addOrgLink')}</a>
        </dd>
    </dl>
</g:if>
