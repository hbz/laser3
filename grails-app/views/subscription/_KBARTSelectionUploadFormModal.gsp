<ui:modal id="KBARTUploadForm" message="subscription.details.addEntitlements.header" msgSave="${message(code: 'subscription.details.addEntitlements.preselect')}">
    <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.warning" />

    <g:form class="ui form" method="post" enctype="multipart/form-data">
        <g:if test="${actionName == 'addEntitlements'}">
            <g:hiddenField name="id" value="${subscription.id}"/>
        </g:if>
        <g:if test="${actionName == 'renewEntitlementsWithSurvey'}">
            <g:hiddenField name="id" value="${subscriberSub.id}"/>
            <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
            <g:hiddenField name="tab" value="${params.tab}"/>
        </g:if>
        <div class="field">
            <div class="ui action input">
                <input type="text" readonly="readonly"
                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                       style="display: none;">
                <div class="ui icon button">
                    <i class="attach icon"></i>
                </div>
            </div>
        </div>
        <g:if test="${actionName == 'addEntitlements'}">
            <g:if test="${institution.isCustomerType_Consortium()}">
                <div class="field">
                    <div class="ui right floated checkbox toggle">
                        <g:checkBox name="withChildrenKBART"/>
                        <label><g:message code="subscription.details.addEntitlements.withChildren"/></label>
                    </div>
                </div>
            </g:if>
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
    <div class="localLoadingIndicator" hidden="hidden">
        <div class="ui inline medium text loader active">Aktualisiere Daten ..</div>
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

        $('#KBARTUploadForm form').submit(function(e) {
            e.preventDefault();
            $('.localLoadingIndicator').show();
            //let kbart = $('#kbartPreselect').prop('files')[0];
            let formData = new FormData(this);
            //formData.append('kbartFile', kbart);
            $.ajax({
                url: '<g:createLink controller="${controllerName}" action="${actionName == 'addEntitlements' ? 'selectEntitlementsWithKBART' : actionName}"/>',
                cache: false,
                contentType: false,
                processData: false,
                data: formData,
                type: 'post',
                success: function(response) {
                    $('.localLoadingIndicator').hide();
                    $('#processResultWrapper').html(response);
                }
            });
        });
    </laser:script>
</ui:modal>