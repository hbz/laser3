<%@ page import="de.laser.addressbook.PersonRole; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.OrgRole; de.laser.wekb.ProviderRole; de.laser.wekb.VendorRole" %>
<ui:modal id="${cssId}" text="Neuen ${modalPrsLinkRole.getI10n("value")} hinzufügen" hideSubmitButton="true">
    <%
        def ownObj
        if(relation instanceof OrgRole)
            ownObj = relation.org
        else if(relation instanceof ProviderRole)
            ownObj = relation.provider
        else if(relation instanceof VendorRole)
            ownObj = relation.vendor
    %>
    <p>${message(code:'myinst.addressBook.visibleOnly')}</p>

    <div class="field">
        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th>Person</th>
                <th>Funktion</th>
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${ownObj.getPublicPersons()}" var="p">
                    <g:if test="${ownObj.gokbId == null}">
                        <tr>
                            <td>
                                <span class="la-popup-tooltip" data-content="${message(code:'address.public')}" data-position="top right">
                                    <i class="${Icon.ACP_PUBLIC}"></i>
                                </span>
                                ${p}
                            </td>
                            <td>
                                <g:each in="${PersonRole.getAllRolesByOwner(p, ownObj)}" var="prsFunc">
                                    ${prsFunc.functionType.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="x">
                                <g:form class="ui form" url="[controller:'addressbook', action:'createPersonRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent}"/>
                                    <input type="hidden" name="person" value="${p.id}" />
                                    <input type="hidden" name="role" value="${role.id}"/>
                                    <input type="hidden" name="ownObj" value="${genericOIDService.getOID(ownObj)}" />

                                    <input type="submit" class="${Btn.POSITIVE}" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>
                    </g:if>
                </g:each>

                <g:each in="${modalVisiblePersons}" var="p">
                    <g:if test="${PersonRole.getAllRolesByOwner(p, ownObj)}">
                        <tr>
                            <td>
                                <span class="la-popup-tooltip" data-content="${message(code:'address.private')}" data-position="top right">
                                    <i class="${Icon.ACP_PRIVATE}"></i>
                                </span>
                                ${p}
                            </td>
                            <td>
                                <g:each in="${PersonRole.getAllRolesByOwner(p, ownObj)}" var="prsFunc">
                                    ${prsFunc.functionType.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="x">
                                <g:form class="ui form" url="[controller:'addressbook', action:'createPersonRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent}"/>
                                    <input type="hidden" name="person" value="${p.id}" />
                                    <input type="hidden" name="role" value="${role.id}"/>
                                    <input type="hidden" name="ownObj" value="${genericOIDService.getOID(ownObj)}" />

                                    <input type="submit" class="${Btn.POSITIVE}" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>
                    </g:if>
                </g:each>

            </tbody>
        </table>

    </div>

</ui:modal>