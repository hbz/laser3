<%@ page import="de.laser.PersonRole" %>
<ui:modal id="${cssId}" text="Neuen ${modalPrsLinkRole.getI10n("value")} hinzufügen" hideSubmitButton="true">

    <p>${message(code:'myinst.addressBook.visibleOnly')}</p>

    <div class="field">
        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th>Person</th>
                <th>Funktion</th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${orgRole.org.getPublicPersons()}" var="p">
                    <g:if test="${true}">
                        <tr>
                            <td>
                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                    <i class="address card icon"></i>
                                </span>
                                ${p}
                            </td>
                            <td>
                                <g:each in="${PersonRole.findByPrsAndOrg(p, orgRole.org)}" var="prsFunc">
                                    ${prsFunc.functionType?.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="center aligned">
                                <g:form class="ui form" url="[controller:'ajax', action:'addPrsRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent}"/>
                                    <input type="hidden" name="person" value="${p.class.name}:${p.id}" />
                                    <input type="hidden" name="role" value="${role}"/>
                                    <input type="hidden" name="org" value="${orgRole.org.class.name}:${orgRole.org.id}" />

                                    <input type="submit" class="ui positive button" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>
                    </g:if>
                </g:each>

                <g:each in="${modalVisiblePersons}" var="p">
                    <g:if test="${PersonRole.findByPrsAndOrg(p, orgRole.org)}">
                        <tr>
                            <td>
                                <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                    <i class="address card outline icon"></i>
                                </span>
                                ${p}
                            </td>
                            <td>
                                <g:each in="${PersonRole.findByPrsAndOrg(p, orgRole.org)}" var="prsFunc">
                                    ${prsFunc.functionType?.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="center aligned">
                                <g:form class="ui form" url="[controller:'ajax', action:'addPrsRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent}"/>
                                    <input type="hidden" name="person" value="${p.class.name}:${p.id}" />
                                    <input type="hidden" name="role" value="${role}"/>
                                    <input type="hidden" name="org" value="${orgRole.org.class.name}:${orgRole.org.id}" />

                                    <input type="submit" class="ui positive button" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>
                    </g:if>
                </g:each>

            </tbody>
        </table>

    </div>

</ui:modal>