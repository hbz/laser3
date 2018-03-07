<semui:modal id="prsLinksModal" text="Neue Kontaktperson hinzufügen" hideSubmitButton="true">

    <p>${message(code:'myinst.addressBook.visibleOnly', default:'Some persons are visible to you due your addressbook')}</p>

    <div class="field">

        <table id="prs_role_tab" class="ui celled la-table la-table-small table">
            <thead>
            <tr>
                <th>Person</th>
                <th>Organisation</th>
                <th>Verantwortlichkeit</th>
                <th>${message(code:'title.edit.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:if test="${modalVisiblePersons}">
                    <g:each in="${modalVisiblePersons}" var="p">

                        <tr>
                            <td>
                                <g:if test="${p.isPublic?.value == "No"}">
                                    <i class="address card outline icon"></i>
                                </g:if>
                                <g:else>
                                    <i class="address card icon"></i>
                                </g:else>
                                ${p}
                            </td>
                            <td>
                                <select name="ignore-org-selector">
                                    <% def existingRoleLinkOrgs = [] ; // only distinct !!! %>
                                    <g:each in="${p.roleLinks}" var="prl">
                                        <g:if test="${! (prl.org in existingRoleLinkOrgs)}">
                                            <% existingRoleLinkOrgs << prl.org %>
                                            <option value="${prl.org.class.name}:${prl.org.id}">${prl.org}</option>
                                        </g:if>
                                    </g:each>
                                </select>
                            </td>
                            <td>
                                ${modalPrsLinkRole.getI10n("value")}
                            </td>
                            <td>
                                <g:form class="ui form" url="[controller:'ajax', action:'addPrsRole']" method="post">
                                    <input type="hidden" name="parent" value="${parent}"/>
                                    <input type="hidden" name="person" value="${p.class.name}:${p.id}" />
                                    <input type="hidden" name="role" value="${role}"/>
                                    <input type="hidden" name="org" value=""/>
                                    <input type="submit" class="ui positive button" name="save" value="${message(code:'default.button.link.label')}"/>
                                </g:form>
                            </td>
                        </tr>

                    </g:each>
                </g:if>
            </tbody>
        </table>
        <script>
            $('#prsLinksModal form input[type=submit]').click( function(){
                var val = $(this).parents('tr').find('select[name=ignore-org-selector]').val()
                $(this).parents('form').find('input[name=org]').val(val)
            })
        </script>
    </div>

</semui:modal>