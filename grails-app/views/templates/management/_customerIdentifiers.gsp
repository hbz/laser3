<%@ page import="de.laser.FormService; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:serviceInjection/>

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
        <g:each in="${platforms}" var="platform">
            <g:form action="uploadRequestorIDs" params="${[id: params.id, platform: platform.id]}" controller="subscription" method="post" enctype="multipart/form-data" class="ui form">
                <div class="ui message">
                    <div class="header">${message(code: 'default.usage.addRequestorIDs.info', args: [platform.name])}</div>

                    <br>
                    ${message(code: 'default.usage.addRequestorIDs.text')}

                    <br>
                    <g:link class="item" controller="public" action="manual" id="fileImport" target="_blank">${message(code: 'help.technicalHelp.fileImport')}</g:link>
                    <br>

                    <g:link controller="subscription" action="templateForRequestorIDUpload" params="[id: params.id, platform: platform.id]">
                        <p>${message(code:'myinst.financeImport.template')}</p>
                    </g:link>

                    <div class="ui action input">
                        <input type="text" readonly="readonly"
                               placeholder="${message(code: 'template.addDocument.selectFile')}">
                        <input type="file" name="requestorIDFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                               style="display: none;">
                        <div class="${Btn.ICON.SIMPLE}">
                            <i class="${Icon.CMD.ATTACHMENT}"></i>
                        </div>
                    </div>
                </div><!-- .message -->
                <div class="field la-field-right-aligned">
                    <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.button.add.label')}"/>
                </div>
                <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
            </g:form>
        </g:each>
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
                            userService.hasFormalAffiliation(pair.owner, 'INST_EDITOR') ||
                            userService.hasFormalAffiliation(pair.customer, 'INST_EDITOR')
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
                                    class="${Btn.MODERN.NEGATIVE_CONFIRM}"
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
<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
         $(this).parent('.action').find('input:file').click();
     });

     $('input:file', '.ui.action.input').on('change', function (e) {
         var name = e.target.files[0].name;
         $('input:text', $(e.target).parent()).val(name);
     });
</laser:script>

