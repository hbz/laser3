<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.FormService; de.laser.Subscription;" %>
<laser:serviceInjection/>
<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <laser:render template="/templates/documents/table"
                      model="${[instance: subscription, context: 'documents', redirect: actionName, owntp: 'subscription']}"/>
        </div>
    </g:if>

    <h2 class="ui header">${message(code: 'subscriptionsManagement.document.info.newDocument')}</h2>

    <div class="ui segments">
    <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'documents']" method="post"
            class="ui segment form newDocument" enctype="multipart/form-data">
        <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
        <g:hiddenField id="allMembers" name="allMembers" value="false"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="field required">
            <label for="upload_title">${message(code: 'template.addDocument.name')}:</label>
            <input type="text" id="upload_title" name="upload_title" value=""/>
        </div>

        <div class="field required">
            <label for="upload_file_placeholder">${message(code: 'template.addDocument.file')}:</label>

            <div class="ui fluid action input">
                <input type="text" id="upload_file_placeholder" readonly="readonly" placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" id="upload_file" name="upload_file" style="display: none;">

                <div class="${Btn.ICON.SIMPLE}" style="padding-left:30px; padding-right:30px">
                    <i class="${Icon.CMD.ATTACHMENT}"></i>
                </div>
            </div>
        </div>

        <div class="field">
            <label for="doctype">${message(code: 'template.addDocument.type')}:</label>
            <%
                List notAvailable = [RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT, RDStore.DOC_TYPE_ONIXPL]
                List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE) - notAvailable
            %>
            <g:select from="${documentTypes}"
                      class="ui dropdown clearable fluid"
                      optionKey="value"
                      optionValue="${{ it.getI10n('value') }}"
                      name="doctype"
                      id="doctype"
                      value=""/>
        </div>

        <g:if test="${showConsortiaFunctions && controllerName != "subscription"}">
            <div class="field">
                <label for="setSharing">${message(code: 'template.addDocument.setSharing')}</label>
                <g:checkBox name="setSharing" class="ui checkbox"/>
            </div>
        </g:if>

        <input type="hidden" id="selectedSubscriptionIds" name="selectedSubscriptionIds" value="" />

        <div class="ui error message"></div>

        <button class="${Btn.SIMPLE}" ${!editable ? 'disabled="disabled"' : ''} type="submit" name="processOption"
                value="newDoc">${message(code: 'default.button.create.label')}</button>
    </g:form>

    <div class="ui segment">
        <h3 class="ui header">
            <g:if test="${controllerName == "subscription"}">
                ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${filteredSubscriptions.size()}"/>
            </g:if><g:else>
                ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${num_sub_rows}"/>
            </g:else>
        </h3>
        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <g:if test="${editable}">
                    <th class="center aligned">
                        <g:checkBox name="membersListToggler" id="membersListToggler" checked="${managementService.checkTogglerState(subIDs, "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}")}"/>
                    </th>
                </g:if>

                <th>${message(code: 'sidewide.number')}</th>
                <g:if test="${controllerName == "subscription"}">
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'subscriptionDetails.members.members')}</th>
                    <g:if test="${params.showMembersSubWithMultiYear}">
                        <th>${message(code: 'subscription.referenceYear.label.shy')}</th>
                    </g:if>
                </g:if>
                <g:if test="${controllerName == "myInstitution"}">
                    <th>${message(code: 'default.subscription.label')}</th>
                </g:if>
                <th>${message(code: 'default.documents.label')}</th>
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriberRespConsortia() : zeile.orgs}"/>
                <tr>
                    <g:if test="${editable}">
                        <td>
                            <%-- This whole construct is necessary for that the form validation works!!! --%>
                            <div class="field">
                                <div class="ui checkbox">
                                    <g:checkBox class="selectedSubs" id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}" checked="${selectionCache.containsKey('selectedSubs_'+sub.id)}"/>
                                </div>
                            </div>
                        </td>
                    </g:if>
                    <td>${(offset ?: 0) + i + 1}</td>
                    <g:if test="${controllerName == "subscription"}">
                        <td>
                            ${subscr.sortname}
                        </td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>
                            <ui:customerTypeOnlyProIcon org="${subscr}" />
                        </td>
                        <g:if test="${params.showMembersSubWithMultiYear}">
                            <td>${sub.referenceYear}</td>
                        </g:if>
                    </g:if>
                    <g:elseif test="${controllerName == "myInstitution"}">
                        <td>
                            ${sub.name} <br/>
                            <span class="${sub.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL ? 'sc_blue' : ''}"> (${sub.type.getI10n('value')}) </span>
                        </td>
                    </g:elseif>
                    <td>
                        <laser:render template="/templates/documents/table"
                                  model="${[instance: sub, context: 'documents', redirect: actionName, owntp: 'subscription']}"/>
                    </td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}"
                                class="${Btn.MODERN.SIMPLE}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    </div><!-- .segments -->
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:else>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true);
            $("#allMembers").val(true);
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false);
            $("#allMembers").val(false);
        }
        JSPC.app.setSelectedSubscriptionIds();
        $.ajax({
            method: "post",
            url: "<g:createLink controller="ajaxJson" action="updatePaginationCache" />",
            data: {
                allIds: [${subIDs.join(',')}],
                cacheKeyReferer: "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}"
            }
        }).done(function(result){
            console.log("updated cache for all subscriptions: "+result.state);
        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });

    $(".selectedSubs").change(function() {
        let selId = $(this).attr("id");
        $.ajax({
            method: "post",
            url: "<g:createLink controller="ajaxJson" action="updatePaginationCache" />",
            data: {
                selId: selId,
                cacheKeyReferer: "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}"
            }
        }).done(function(result){
            console.log("updated cache for "+selId+": "+result.state);
        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });

    $("input[name=selectedSubs]").checkbox({
        onChange: function() {
            $('#membersListToggler').prop('checked', false);
        }
    });

    $("tr[class!=disabled] input[name=selectedSubs]").on ('change', function () {
        JSPC.app.setSelectedSubscriptionIds();
    });

    JSPC.app.setSelectedSubscriptionIds = function() {
        $('#selectedSubscriptionIds').val(
            $("tr[class!=disabled] input[name=selectedSubs]:checked").map (function () {
                return this.value;
            }).get()
        );
        $('.newDocument').form('validate form');
    };

    $('#upload_file').change( function () {
        setTimeout (function () { $('.newDocument').form('validate form'); }, 100); // fixed event handling
    });

     $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });

    $('.newDocument').form({
        on: 'blur',
        inline: false,
        fields: {
            upload_title: {
                identifier: 'upload_title',
                rules: [
                    {
                        type: 'empty',
                        prompt: '${message(code: 'template.addDocument.name')} ${message(code: "validation.needsToBeFilledOut")}'
                    }
                ]
            },
            upload_file_placeholder: {
                identifier: 'upload_file_placeholder',
                rules: [
                    {
                        type: 'empty',
                        prompt: '${message(code: 'template.addDocument.file')} ${message(code: "validation.needsToBeFilledOut")}'
                    }
                ]
            },
            selectedSubscriptionIds: {
                identifier: 'selectedSubscriptionIds',
                rules: [
                    {
                        type: 'empty',
                        prompt: '<g:message code="subscriptionsManagement.noSelectedSubscriptions.table"/>'
                    }
                ]
            }
        }
    });

    <g:if test="${selectionCache.keySet().size() > 0}">
        JSPC.app.setSelectedSubscriptionIds();
    </g:if>
</laser:script>