<%@ page import="de.laser.ui.Icon" %>
<div class="ui segment">
    <h3 class="ui header">
        <g:if test="${controllerName == "subscription"}">
            ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${keyPairs.size()}"/>
        </g:if><g:else>
            ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${keyPairs.size()}/${num_sub_rows}"/>
        </g:else>
    </h3>
    <ui:tabs>
        <g:each in="${platforms}" var="platform">
            <ui:tabsItem controller="subscription" action="membersSubscriptionsManagement" tab="${platform.id.toString()}" subTab="tabPlat" params="${params + [tab: 'customerIdentifiers', tabPlat: platform.id]}" text="${platform.name}"/>
        </g:each>
    </ui:tabs>
    <div class="ui bottom attached tab active segment">
        <table class="ui la-js-responsive-table la-table table">
            <thead>
            <tr>
                <th class="three wide">${message(code: 'consortium.member')}</th>
                <th class="four wide">${message(code: 'platform.label')}</th>
                <th class="three wide">${message(code: 'org.customerIdentifier')}</th>
                <th class="three wide">${message(code: 'org.requestorKey')}</th>
                <th class="two wide">${message(code: 'default.note.label')}</th>
                <th class="one wide">${message(code: 'default.actions')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${keyPairs}" var="pair" status="rowno">
                <g:set var="overwriteEditable_ci" value="${editable}" />
                <%
                    //ERMS-5495 in conflict with ERMS-5647! overwriteEditable_ci is now set to editable; raise subject of kanban!
                    /*
                    boolean overwriteEditable_ci = contextService.getUser().isAdmin() ||
                            userService.hasFormalAffiliation(contextService.getUser(), pair.owner, 'INST_EDITOR') ||
                            userService.hasFormalAffiliation(contextService.getUser(), pair.customer, 'INST_EDITOR')
                     */
                %>
                <tr>
                    <td><g:link controller="organisation" action="show" id="${pair.customer.id}">${pair.customer.sortname ?: pair.customer.name}</g:link></td>
                    <td><g:link controller="platform" action="show" id="${pair.platform.id}">${pair.platform.name}</g:link></td>
                    <td><ui:xEditable owner="${pair}" field="value" overwriteEditable="${overwriteEditable_ci}" /></td>
                    <td><ui:xEditable owner="${pair}" field="requestorKey" overwriteEditable="${overwriteEditable_ci}" /></td>
                    <td><ui:xEditable owner="${pair}" field="note" overwriteEditable="${overwriteEditable_ci}" /></td>
                    <td>
                        <g:if test="${overwriteEditable_ci}">
                            <g:link controller="subscription"
                                    action="unsetCustomerIdentifier"
                                    id="${subscription.id}"
                                    params="${[deleteCI: pair.id]}"
                                    class="ui button icon red la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unset.customeridentifier", args: ["" + pair.getProvider() + " : " + (pair.platform?:'') + " " + (pair.value?:'')])}"
                                    data-confirm-term-how="unset"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.ERASE}"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>

