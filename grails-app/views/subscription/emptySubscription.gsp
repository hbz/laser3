<laser:serviceInjection />

<%@ page import="de.laser.RefdataCategory;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Combo;de.laser.RefdataValue;de.laser.Org" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'myinst.emptySubscription.label')}</title>
        </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <semui:crumb message="myinst.emptySubscription.label" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'myinst.emptySubscription.label')}</h1>

        <semui:messages data="${flash}"/>

        <semui:form>
            <g:form action="processEmptySubscription" method="post" class="ui form newSubscription">
                <div class="field required">
                    <label>${message(code:'myinst.emptySubscription.name')} <g:message code="messageRequiredField" /></label>
                    <input type="text" name="newEmptySubName" placeholder=""/>
                 </div>

                <div class="two fields">
                    <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from" value="${defaultStartYear}" />

                    <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" value="${defaultEndYear}" />
                </div>

                <div class="field required">
                    <label>${message(code:'default.status.label')} <g:message code="messageRequiredField" /></label>
                    <laser:select name="status" from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" optionKey="id" optionValue="value"
                                  noSelection="${['' : '']}"
                                  value="${['':'']}"
                                  class="ui select dropdown"/>
                </div>

                <g:if test="${(institution.globalUID == Org.findByName('LAS:eR Backoffice').globalUID)}">
                    <%
                        List subscriptionTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE)
                        subscriptionTypes-=RDStore.SUBSCRIPTION_TYPE_LOCAL
                    %>
                    <div class="field">
                        <label>${message(code:'myinst.emptySubscription.create_as')}</label>
                        <laser:select id="asOrgType" name="type" from="${subscriptionTypes}" value="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}" optionKey="id" optionValue="value" class="ui select dropdown" />
                    </div>
                </g:if>
                <g:elseif test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                    <input type="hidden" id="asOrgType" name="type" value="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id}" />
                </g:elseif>
                <g:elseif test="${accessService.checkPerm('ORG_INST')}">
                    <input type="hidden" id="asOrgType" name="type" value="${RDStore.SUBSCRIPTION_TYPE_LOCAL.id}" />
                </g:elseif>

                <br />
                <div id="dynHiddenValues"></div>

                <%--<g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <input class="hidden" type="checkbox" name="generateSlavedSubs" value="Y" checked="checked" readonly="readonly">
                </g:if>--%>
                <input id="submitterFallback" type="submit" class="ui button js-click-control" value="${message(code:'default.button.create.label')}" />
                <input type="button" class="ui button js-click-control" onclick="JSPC.helper.goBack();" value="${message(code:'default.button.cancel.label')}" />
            </g:form>
        </semui:form>

    <hr />
        <laser:script file="${this.getGroovyPageFileName()}">
             $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
                if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
                    var startDate = Date.parse(JSPC.helper.formatDate($("#valid_from").val()));
                    var endDate = Date.parse(JSPC.helper.formatDate($("#valid_to").val()));
                    return (startDate < endDate);
                }
                else return true;
             };
                    $('.newSubscription')
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
        </laser:script>
    </body>
</html>
