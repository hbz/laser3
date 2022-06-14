<semui:modal id="prsLinksModal" text="Neue Kontaktperson hinzufügen" hideSubmitButton="true">

    <p>${message(code:'myinst.addressBook.visibleOnly')}</p>

    <div class="field">

        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
            <tr>
                <th>Person</th>
                <th>Organisation</th>
                <th>Verantwortlichkeit</th>
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
        <laser:script file="${this.getGroovyPageFileName()}">
            $('#prsLinksModal form input[type=submit]').click( function(){
                var val = $(this).parents('tr').find('select[name=ignore-org-selector]').val()
                $(this).parents('form').find('input[name=org]').val(val)
            })
        </laser:script>
    </div>

</semui:modal>