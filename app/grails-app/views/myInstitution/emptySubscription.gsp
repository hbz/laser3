<laser:serviceInjection />

<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Combo;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.emptySubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.emptySubscription.label" class="active" />
        </semui:breadcrumbs>

        <g:render template="actions" />
        <br>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'myinst.emptySubscription.label')}</h1>

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
                    <laser:select name="status" from="${fakeList}" optionKey="id" optionValue="value"
                                  noSelection="${['' : '']}"
                                  value="${['':'']}"
                                  class="ui select dropdown"/>
                </div>

                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                    <%
                        List subscriptionTypes = RefdataCategory.getAllRefdataValues('Subscription Type')
                        subscriptionTypes-=RDStore.SUBSCRIPTION_TYPE_LOCAL
                    %>
                    <div class="field">
                        <label>${message(code:'myinst.emptySubscription.create_as')}</label>
                        <laser:select id="asOrgType" name="type" from="${subscriptionTypes}" value="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}" optionKey="id" optionValue="value" class="ui select dropdown" />
                    </div>
                </g:if>
                <g:elseif test="${accessService.checkPerm('ORG_INST_COLLECTIVE')}">
                    <input type="hidden" id="asOrgType" name="type" value="${RDStore.SUBSCRIPTION_TYPE_LOCAL.id}" />
                </g:elseif>

                <br/>
                <div id="dynHiddenValues"></div>

                <%--<g:if test="${accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")}">
                    <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                </g:if>--%>
                <input id="submitterFallback" type="submit" class="ui button js-click-control" value="${message(code:'default.button.create.label', default:'Create')}" />
            </g:form>
        </semui:form>

    <hr>

    <%-- deactivated because of ERMS-1732
        <g:if test="${consortialView || departmentalView}">

            <g:if test="${! members}">
                <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
                    <hr />

                    <div class="ui info message">
                        <div class="header">
                            <g:if test="${consortialView}">
                                <g:message code="myinst.noMembers.cons.header"/>
                            </g:if>
                            <g:elseif test="${departmentalView}">
                                <g:message code="myinst.noMembers.dept.header"/>
                            </g:elseif>
                        </div>
                        <p>
                            <g:if test="${consortialView}">
                                <g:message code="myinst.noMembers.cons.body" args="${["${link(action:'addMembers'){message(code:'myinst.noMembers.link')}}"]}"/>
                            </g:if>
                            <g:elseif test="${departmentalView}">
                                <g:message code="myinst.noMembers.dept.body" args="${["${link(action:'addMembers'){message(code:'myinst.noMembers.link')}}"]}"/>
                            </g:elseif>
                        </p>
                    </div>
                </g:if>
            </g:if>
            <g:else>
                <div class="cons-options">
                    <semui:filter>
                        <g:formRemote name="x" url="[action:'ajaxEmptySubscription']" update="orgListTable" class="ui form">
                            <g:render template="/templates/filter/orgFilter"
                                      model="[
                                              tmplConfigShow: [['name']],
                                              tmplConfigFormFilter: true,
                                              useNewLayouter: true
                                      ]" />
                        </g:formRemote>
                    </semui:filter>

                    <div id="orgListTable">
                        <g:render template="/templates/filter/orgFilterTable" model="[orgList: members, tmplShowCheckbox: true, tmplConfigShow: ['sortname', 'name']]" />
                    </div>

                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" checked="checked" readonly="readonly">
                        <label>
                            <g:if test="${consortialView}">${message(code:'myinst.separate_subs')}</g:if>
                            <g:elseif test="${departmentalView}">${message(code:'myinst.separate_subs_dept')}</g:elseif>
                        </label>
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
                    var selVal = $(this).val();
                    if (['${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}','${RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE.id}'].indexOf(selVal) > -1) {
                        $('.cons-options').show()
                    }
                    else {
                        $('.cons-options').hide()
                    }
                })
            </r:script>

        </g:if>
     --%>
        <r:script language="JavaScript">
            function formatDate(input) {
                if(input.match(/^\d{2}[\.\/-]\d{2}[\.\/-]\d{2,4}$/)) {
                    var inArr = input.split(/[\.\/-]/g);
                    return inArr[2]+"-"+inArr[1]+"-"+inArr[0];
                }
                else {
                    console.log(input);
                    return input;
                }
            }
             $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
                if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
                    var startDate = Date.parse(formatDate($("#valid_from").val()));
                    var endDate = Date.parse(formatDate($("#valid_to").val()));
                    console.log(formatDate($("#valid_from").val())+' '+formatDate($("#valid_to").val()));
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
