

<semui:modal id="editEntitlementGroupItemModal" message="subscription.details.ieGroups.edit" isEditModal="true">

    <g:form class="ui form" action="editEntitlementGroupItem" params="[ie: ie.id, id: subscription.id]" method="POST">
        <input type="hidden" name="cmd" value="processing"/>

        <div class="ui grid">
                <div class="field" style="width:100%">
                    <label><g:message code="subscription.details.ieGroupsforIE" args="[ie.name]"/></label>

                    <br />
                    <br />
                    <div class="scrollWrapper">

                            <table class="ui table la-js-responsive-table la-table scrollContent" >
                                <tr>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'issueEntitlementGroup.name.label')}</th>
                                <th>${message(code: 'issueEntitlementGroup.description.label')}</th>
                                <th>${message(code: 'issueEntitlementGroup.items.label')}</th>
                                    <th></th>
                                </tr>
                                <tbody>
                                <g:each in="${subscription.ieGroups.sort{it.name}}" var="titleGroup" status="i">
                                    <tr>
                                        <td>${i+1}</td>
                                        <td>
                                            ${titleGroup.name}
                                        </td>
                                        <td>
                                            ${titleGroup.description}
                                        </td>
                                        <td>
                                            ${titleGroup.items.size()}
                                        </td>
                                        <td>
                                            <g:if test="${de.laser.IssueEntitlementGroupItem.findByIeAndIeGroup(ie, titleGroup)}">
                                                <input type="checkbox" checked="checked" name="titleGroup" value="${titleGroup.id}" />
                                            </g:if>
                                            <g:else>
                                                <g:if test="${!de.laser.IssueEntitlementGroupItem.findByIe(ie)}">
                                                <input type="checkbox" name="titleGroup" value="${titleGroup.id}" />
                                                </g:if>
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>

                    </div>
                    <style>
                        .scrollWrapper {
                            overflow-y: scroll;
                            max-height: 400px;
                        }
                        .scrollContent {
                        }
                    </style>
                </div>

        </div><!-- .grid -->


    </g:form>
</semui:modal>