<%@ page import="de.laser.ImportService; de.laser.ExportClickMeService; de.laser.FormService; de.laser.ui.Btn; de.laser.ui.Icon" %>
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
            <div class="ui teal progress progressIndicator" hidden="hidden">
                <div class="bar">
                    <div class="progress"></div>
                </div>
                <div class="label"></div>
            </div>
            <div class="counterCheckWrapper"></div>
            <g:form action="uploadRequestorIDs" params="${[id: params.id, platform: platform.id]}" controller="subscription" method="post" enctype="multipart/form-data" class="ui form">
                <div class="ui message">
                    <div class="header">${message(code: 'default.usage.addRequestorIDs.info', args: [platform.name])}</div>

                    <br>
                    ${message(code: 'default.usage.addRequestorIDs.text')}

                    <br>
                    <g:link class="item" controller="public" action="manual" id="fileImport" target="_blank">${message(code: 'help.technicalHelp.fileImport')}</g:link>
                    <br>

                    <g:link class="xls" controller="subscription" action="templateForRequestorIDUpload" params="[id: params.id, platform: platform.id, format: ExportClickMeService.FORMAT.XLS]">
                        <p>${message(code:'myinst.financeImport.template')}</p>
                    </g:link>
                    <g:link class="csv" controller="subscription" action="templateForRequestorIDUpload" params="[id: params.id, platform: platform.id, format: ExportClickMeService.FORMAT.CSV]">
                        <p>${message(code:'myinst.financeImport.template')}</p>
                    </g:link>
                    <div class="ui radio checkbox">
                        <input id="formatXLS" name="format" type="radio" value="${ExportClickMeService.FORMAT.XLS.toString()}" class="hidden formatSelection" checked="checked">
                        <label for="formatXLS"><g:message code="default.import.upload.xls"/></label>
                    </div>
                    <div class="ui radio checkbox">
                        <input id="formatCSV" name="format" type="radio" value="${ExportClickMeService.FORMAT.CSV.toString()}" class="hidden formatSelection">
                        <label for="formatCSV"><g:message code="default.import.upload.csv"/></label>
                    </div>
                    <g:hiddenField name="fileContainsHeader" value="on"/>
                    <br>
                    <div class="ui action input xls">
                        <input type="text" readonly="readonly" class="ui input" placeholder="${message(code: 'myinst.subscriptionImport.fileSelectorXLS')}">

                        <input type="file" name="excelFile" accept=".xls,.xlsx,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                               style="display: none;">
                        <div class="${Btn.ICON.SIMPLE}">
                            <i class="${Icon.CMD.ATTACHMENT}"></i>
                        </div>
                    </div>
                    <div class="ui action input csv">
                        <input type="text" readonly="readonly" class="ui input" placeholder="${message(code: 'myinst.subscriptionImport.fileSelectorCSV')}">

                        <input type="file" name="csvFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                               style="display: none;">
                        <div class="${Btn.ICON.SIMPLE}">
                            <i class="${Icon.CMD.ATTACHMENT}"></i>
                        </div>

                        <select class="ui dropdown" name="separator">
                            <g:each in="${ImportService.CSV_CHARS}" var="setting">
                                <option value="${setting.charKey}"><g:message code="${setting.name}"/></option>
                            </g:each>
                        </select>
                    </div>
                </div><!-- .message -->
                <div class="field la-field-right-aligned">
                    <input type="button" class="counterApiConnectionCheck ${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.usage.sushiCallCheck.trigger')}"/>
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
    $('.csv').hide();
    $('.progressIndicator').hide();

    $('.action .icon.button').click(function () {
         $(this).parent('.action').find('input:file').click();
     });

     $('input:file', '.ui.action.input').on('change', function (e) {
         var name = e.target.files[0].name;
         $('input:text', $(e.target).parent()).val(name);
     });

    $('.formatSelection').on('change', function() {
        if($(this).val() === '${ExportClickMeService.FORMAT.XLS}') {
            $('.xls').show();
            $('.csv').hide();
        }
        else if($(this).val() === '${ExportClickMeService.FORMAT.CSV}') {
            $('.csv').show();
            $('.xls').hide();
        }
    });

    $(".counterApiConnectionCheck").on('click', function() {
        $('.progressIndicator').progress('set percent', 0);
        $('.progressIndicator').show();
        $(".counterCheckWrapper").hide();
        $.ajax({
            url: "<g:createLink controller="ajaxHtml" action="checkCounterAPIConnection" params="[id: subscription.id]"/>"
        }).done(function(response) {
            $(".counterCheckWrapper").html(response).show();
        });
        checkProgress();
    });

    function checkProgress() {
        let percentage = 0;
        setTimeout(function() {
            $.ajax({
                url: "<g:createLink controller="ajaxJson" action="checkProgress" params="[cachePath: '/subscription/checkCounterAPIConnection']"/>"
            }).done(function(response){
                percentage = response.percent;
                $('.progressIndicator div.label').text(response.label);
                if(percentage !== null)
                    $('.progressIndicator').progress('set percent', percentage);
                if($('.progressIndicator').progress('is complete')) {
                    $('.progressIndicator').hide();
                }
                else {
                    checkProgress();
                }
            }).fail(function(resp, status){
                console.log(status+' '+resp);
            });
        }, 500);
    }
</laser:script>

