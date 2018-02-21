<dl>
    <dt><g:message code="license.responsibilites" default="Responsibilites" /></dt>
    <dd>

        <g:each in="${visiblePrsLinks}" var="pr">
            <g:if test="${pr.org}">
                <div>
                    <g:if test="${pr.prs.isPublic?.value == "No"}"><i class="address book outline icon"></i> </g:if>
                    <g:link controller="person" action="show" id="${pr.prs.id}">${pr.prs}</g:link>

                    <g:if test="${true || tmplShowFunction}">

                        <g:if test="${pr.functionType}">
                            ,
                            ${pr.functionType.getI10n("value")}
                        </g:if>
                        <g:if test="${pr.responsibilityType}">
                            ,
                            ${pr.responsibilityType.getI10n("value")}
                        </g:if>

                    </g:if>

                    <br />
                    <g:link controller="Organisations" action="show" id="${pr.org.id}">${pr.org.name}</g:link>

                    <g:if test="${editable}">
                        <br />
                        (<g:link controller="ajax" action="delPrsRole" id="${pr.id}"
                                onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})">
                                <i class="unlinkify icon red"></i> ${message(code:'default.button.unlink.label')}
                        </g:link>)
                    </g:if>
                </div>
            </g:if>
        </g:each>

    </dd>
</dl>

<g:if test="${editable}">
    <dl>
        <dt></dt>
        <dd>
            <input class="ui button"
                   value="${message(code: 'default.add.label', args: [message(code: 'person.label', default: 'Person')])}"
                   data-semui="modal"
                   href="#prsLinksModal" />
        </dd>
    </dl>
</g:if>
