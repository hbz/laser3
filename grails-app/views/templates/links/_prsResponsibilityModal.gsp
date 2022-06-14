<semui:modal id="prsLinksModal" text="Neuen ${modalPrsLinkRole.getI10n("value")} hinzufügen" hideSubmitButton="true">

        <%--<p>${message(code:'myinst.addressBook.visibleOnly')}</p>--%>
    <p>Hier können Sie einen ihrer öffentlichen Kontakte mit diesem Objekt verknüpfen.</p>

    <div class="field">

        <table class="ui celled la-js-responsive-table la-table compact table la-ignore-fixed">
            <thead>
            <tr>
                <th>Person</th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:if test="${modalVisiblePersons}">
                    <g:each in="${modalVisiblePersons}" var="p">

                        <tr>
                            <td>
                                <g:if test="${! p.isPublic}">
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                        <i class="address card outline icon"></i>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                        <i class="address card icon"></i>
                                    </span>
                                </g:else>
                                ${p}
                            </td>
                            <td class="x">
                                <g:form class="ui form" url="[controller:'ajax', action:'addPrsRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent.class.name}:${parent.id}"/>
                                    <input type="hidden" name="person" value="${p.class.name}:${p.id}" />
                                    <input type="hidden" name="role" value="${role.class.name}:${role.id}"/>
                                    <input type="hidden" name="org" value="${org.class.name}:${org.id}"/>
                                    <input type="submit" class="ui positive button" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>

                    </g:each>
                </g:if>
            </tbody>
        </table>
    </div>

</semui:modal>