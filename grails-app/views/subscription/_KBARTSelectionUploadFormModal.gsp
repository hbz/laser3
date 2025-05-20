<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.AuditConfig" %>
<ui:modal id="KBARTUploadForm" message="${headerToken}" msgSave="${message(code: 'subscription.details.addEntitlements.preselect')}">
    <%-- double-check needed because menu is not being refreshed after xEditable change on sub/show --%>
    <g:if test="${!(subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE || AuditConfig.getConfig(subscription.instanceOf, 'holdingSelection'))}">
        <ui:msg showIcon="true" class="info" message="${headerToken}.manual" hideClose="true"/>

        <g:form class="ui form" method="post" enctype="multipart/form-data">
            <g:hiddenField name="id" value="${subscription.id}"/>
            <g:if test="${withPick}">
                <g:hiddenField name="withPick" value="${true}"/>
            </g:if>
            <g:if test="${withIDOnly}">
                <g:hiddenField name="withIDOnly" value="${true}"/>
            </g:if>
            <g:hiddenField name="progressCacheKey" value="${progressCacheKey}"/>
            <g:if test="${surveyConfig}">
                <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
                <g:hiddenField name="tab" value="${tab}"/>
            </g:if>
            <div class="field">
                <div class="ui action input">
                    <input type="text" readonly="readonly"
                           placeholder="${message(code: 'template.addDocument.selectFile')}">
                    <input type="file" id="kbartPreselect" name="kbartPreselect" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                           style="display: none;">
                    <div class="${Btn.ICON.SIMPLE}">
                        <i class="${Icon.CMD.ATTACHMENT}"></i>
                    </div>
                </div>
            </div>
            <g:if test="${referer == 'addEntitlements'}">
                <%-- meaningless since the control of the title distribution via inheritance!
                cases:
                a) title of consortium = title of members: either holding entire or holding partial with inheritance activated
                b) title of consortium != title of members: process title enrichment at _each_ member individually
                    <g:if test="${institution.isCustomerType_Consortium()}">
                        <div class="field">
                            <div class="ui right floated checkbox toggle">
                                <g:checkBox name="withChildrenKBART"/>
                                <label><g:message code="subscription.details.addEntitlements.withChildren"/></label>
                            </div>
                        </div>
                    </g:if>
                --%>
                <div class="ui two fields">
                    <g:if test="${subscription.ieGroups}">
                        <div class="field">
                            <label for="issueEntitlementGroupKBART">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.add')}:</label>
                            <select name="issueEntitlementGroupKBARTID" id="issueEntitlementGroupKBART"
                                    class="ui search dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>
                                <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                                    <option value="${titleGroup.id}">
                                        ${titleGroup.name} (${titleGroup.countCurrentTitles()})
                                    </option>
                                </g:each>
                            </select>
                        </div>
                    </g:if>
                    <div class="field">
                        <label for="issueEntitlementGroupNewKBART">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}:</label>
                        <input type="text" id="issueEntitlementGroupNewKBART" name="issueEntitlementGroupNewKBART" value="">
                    </div>
                </div>
            </g:if>
        </g:form>
        <div class="ui teal progress" id="uploadProcessLoadingIndicator">
            <div class="bar">
                <div class="progress"></div>
            </div>
            <div class="label"></div>
        </div>
        <div id="processResultWrapper"></div>
        <laser:script file="${this.getGroovyPageFileName()}">
            $('.action .icon.button').click(function () {
                $(this).parent('.action').find('input:file').click();
            });

            $('input:file', '.ui.action.input').on('change', function (e) {
                var name = e.target.files[0].name;
                $('input:text', $(e.target).parent()).val(name);
            });
            $('#uploadProcessLoadingIndicator').hide();

            $('#KBARTUploadForm form').submit(function(e) {
                e.preventDefault();
                $('input:submit').prop('disabled', true);
                $('#uploadProcessLoadingIndicator').progress();
                $('#uploadProcessLoadingIndicator').progress('set percent', 0);
                $('#uploadProcessLoadingIndicator').show();
                //let kbart = $('#kbartPreselect').prop('files')[0];
                let formData = new FormData(this);
                //formData.append('kbartFile', kbart);
                $.ajax({
                    url: '<g:createLink controller="subscription" action="${surveyConfig ? 'tippSelectForSurvey' : 'selectEntitlementsWithKBART'}"/>',
                    cache: false,
                    contentType: false,
                    processData: false,
                    data: formData,
                    type: 'post'
                }).done(function(response) {
                    $('#uploadProcessLoadingIndicator').hide();
                    $('#processResultWrapper').html(response);
                    $('input:submit').prop('disabled', false);
                });
                checkProgress();
            });

            function checkProgress() {
                let percentage = 0;
                setTimeout(function() {
                    $.ajax({
                        <%-- progressCacheKey is set in sub/_actions.gsp resp everywhere where the link to the renderer of this template is being defined! --%>
                        url: "<g:createLink controller="ajaxJson" action="checkProgress" params="[cachePath: progressCacheKey]"/>"
                    }).done(function(response){
                        percentage = response.percent;
                        $('#uploadProcessLoadingIndicator div.label').text(response.label);
                        if(percentage !== null)
                            $('#uploadProcessLoadingIndicator').progress('set percent', percentage);
                        if($('#uploadProcessLoadingIndicator').progress('is complete'))
                            $('#uploadProcessLoadingIndicator').hide();
                        else
                            checkProgress();
                    }).fail(function(resp, status){
                        //TODO
                    });
                }, 500);
            }
        </laser:script>
    </g:if>
    <g:else>
        <ui:msg class="error" showIcon="error" header="${message(code:"default.error")}" message="subscription.details.addEntitlements.holdingEntire" />
    </g:else>
</ui:modal>