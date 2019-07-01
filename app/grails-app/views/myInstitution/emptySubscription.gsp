<laser:serviceInjection />

<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Combo;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.addSubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.addSubscription.label" class="active" />
        </semui:breadcrumbs>

        <g:render template="actions" />

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'myinst.emptySubscription.label')}</h1>

        <semui:messages data="${flash}"/>

        <semui:form>
            <g:form action="processEmptySubscription" controller="myInstitution" method="post" class="ui form newLicence">
                <input type="hidden" name="newEmptySubId" value="${defaultSubIdentifier}"/>

                <p>${message(code:'myinst.emptySubscription.notice')}</p>

                <div class="field required">
                    <label>${message(code:'myinst.emptySubscription.name')}</label>
                    <input type="text" name="newEmptySubName" placeholder=""/>
                 </div>

                <div class="two fields">
                    <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from" value="${defaultStartYear}" />

                    <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" value="${defaultEndYear}" />
                </div>

                <div class="field required">
                    <label>${message(code:'myinst.emptySubscription.status')}</label>
                    <%
                        def fakeList = []
                        fakeList.addAll(RefdataCategory.getAllRefdataValues('Subscription Status'))
                        fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status'))
                    %>
                    <laser:select name="status" from="${fakeList}" optionKey="id" optionValue="value" noSelection="${['':'']}" value="${['':'']}" class="ui select dropdown"/>
                </div>

                <%--<g:if test="${(RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  orgType)}">
                    <div class="field">
                        <label>${message(code:'myinst.emptySubscription.create_as', default:'Create with the role of')}</label>

                        <select id="asOrgType" name="asOrgType" class="ui dropdown">
                            <g:each in="${RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.value in (:wl) and rdv.owner.desc = :ot', [wl:['Consortium', 'Institution'], ot:'OrgRoleType'])}" var="opt">
                                <option value="${opt.id}" data-value="${opt.value}">${opt.getI10n('value')}</option>
                            </g:each>
                        </select>

                    </div>
                </g:if>--%>
                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                    <div class="field">
                        <label>${message(code:'myinst.emptySubscription.create_as')}</label>
                        <laser:select name="type" from="${RefdataCategory.getAllRefdataValues('Subscription Type')}" value="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}" optionKey="id" optionValue="value" class="ui select dropdown" />
                    </div>
                </g:if>

                <br/>
                <div id="dynHiddenValues"></div>

                <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                <input id="submitterFallback" type="submit" class="ui button js-click-control" value="${message(code:'default.button.create.label', default:'Create')}" />
            </g:form>
        </semui:form>

    <hr>

        <g:if test="${(RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  orgType)}">

            <g:if test="${! cons_members}">
                <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
                    <hr />

                    <div class="ui info message">
                        <div class="header">Konsorten verwalten</div>
                        <p>
                            Sie können bei Bedarf über
                            <g:link controller="myInstitution" action="addMembers">diesen Link</g:link>
                            Ihre Konsorten verwalten ..
                        </p>
                    </div>
                </g:if>
            </g:if>
            <g:else>
                <div class="cons-options">
                    <semui:filter>
                        <g:formRemote name="x" url="[controller:'MyInstitution', action:'ajaxEmptySubscription', params:[shortcode:contextService.getOrg()?.shortcode]]" update="orgListTable" class="ui form">
                            <g:render template="/templates/filter/orgFilter"
                                      model="[
                                              tmplConfigShow: [['name']],
                                              tmplConfigFormFilter: true,
                                              useNewLayouter: true
                                      ]" />
                        </g:formRemote>
                    </semui:filter>

                    <div id="orgListTable">
                        <g:render template="/templates/filter/orgFilterTable" model="[orgList: cons_members, tmplShowCheckbox: true, tmplConfigShow: ['sortname', 'name']]" />
                    </div>

                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" checked="checked" readonly="readonly">
                        <label>${message(code:'myinst.emptySubscription.seperate_subs', default:'Generate seperate Subscriptions for all Consortia Members')}</label>
                    </div>

                </div><!-- .cons-options -->
            </g:else>

            <r:script language="JavaScript">
                $('#submitterFallback').click(function(e){
                    e.preventDefault();
                    $('#dynHiddenValues').empty();
                    $('input[name=selectedOrgs]:checked').each(function(index, elem){
                        var newElem = $('<input type="hidden" name="selectedOrgs" value="' + $(elem).attr('value') + '">');
                        $('#dynHiddenValues').append(newElem)
                    });
                    $(this).parents('form').submit()
                });

                $('#asOrgType').change(function() {
                    var selVal = $(this).find('option:selected').attr('data-value');
                    if ('Consortium' == selVal) {
                        $('.cons-options').show()
                    }
                    else {
                        $('.cons-options').hide()
                    }
                })
            </r:script>

        </g:if>
        <r:script language="JavaScript">
            function formatDate(input) {
                var inArr = input.split(/[\.-]/g);
                return inArr[2]+"-"+inArr[1]+"-"+inArr[0];
            }
             $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
                if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
                    var startDate = Date.parse(formatDate($("#valid_from").val()));
                    var endDate = Date.parse(formatDate($("#valid_to").val()));
                    return (startDate < endDate);
                }
                else return true;
             };
                    $('.newLicence')
                            .form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            newEmptySubName: {
                                identifier  : 'newEmptySubName',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            },
                            valid_from: {
                                identifier: 'valid_from',
                                rules: [
                                    {
                                        type: 'endDateNotBeforeStartDate',
                                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                                    }
                                ]
                            },
                            valid_to: {
                                identifier: 'valid_to',
                                rules: [
                                    {
                                        type: 'endDateNotBeforeStartDate',
                                        prompt: '<g:message code="validation.endDateBeforeStartDate"/>'
                                    }
                                ]
                            },
                            status: {
                                identifier  : 'status',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                                    }
                                ]
                            }
                         }
                    });
        </r:script>
    </body>
</html>
