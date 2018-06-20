<semui:modal id="${cssId}" text="Neuen ${modalPrsLinkRole.getI10n("value")} hinzufügen" hideSubmitButton="true">

    <p>${message(code:'myinst.addressBook.visibleOnly', default:'Some persons are visible to you due your addressbook')}</p>

    <div class="field">

        <table id="prs_role_tab" class="ui celled la-table la-table-small table">
            <thead>
            <tr>
                <th>Person</th>
                <th>Funktion</th>
                <th>${message(code:'default.actions')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${orgRole.org.getPublicPersons()}" var="p">
                    <g:if test="${true}">
                        <tr>
                            <td>
                                <i class="address card icon"></i> ${p}
                            </td>
                            <td>
                                <g:each in="${com.k_int.kbplus.PersonRole.findByPrsAndOrg(p, orgRole.org)}" var="prsFunc">
                                    ${prsFunc.functionType?.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="x">
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
                    <g:if test="${com.k_int.kbplus.PersonRole.findByPrsAndOrg(p, orgRole.org)}">
                        <tr>
                            <td>
                                <i class="address card outline icon"></i> ${p}
                            </td>
                            <td>
                                <g:each in="${com.k_int.kbplus.PersonRole.findByPrsAndOrg(p, orgRole.org)}" var="prsFunc">
                                    ${prsFunc.functionType?.getI10n("value")}
                                </g:each>
                            </td>
                            <td class="x">
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

</semui:modal>