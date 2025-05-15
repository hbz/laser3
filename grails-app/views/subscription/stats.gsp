<%@ page import="de.laser.FormService; de.laser.storage.PropertyStore; de.laser.properties.SubscriptionProperty; de.laser.ui.Btn; de.laser.ui.Icon; java.text.SimpleDateFormat; grails.converters.JSON; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.wekb.Platform; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report; de.laser.interfaces.CalculatedType; de.laser.base.AbstractReport; de.laser.finance.CostItem; de.laser.base.AbstractReport; de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.stats.label" />
    <laser:javascript src="echarts.js"/>
        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
            <laser:render template="iconSubscriptionIsChild"/>
            ${subscription.name}
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="stats" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <laser:render template="nav" />

        <ui:objectStatus object="${subscription}" />
        <laser:render template="message" />
        <ui:messages data="${flash}" />

        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <g:each in="${platformInstanceRecords.values()}" var="platform">
                <div class="ui segment">
                    <laser:render template="/platform/platformStatsDetails" model="[wekbServerUnavailable: wekbServerUnavailable, platformInstanceRecord: platform]"/>
                    <g:set var="statsInfo" value="${SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp where sp.owner = :subscription and sp.type = :statsAccess', [statsAccess: PropertyStore.SUB_PROP_STATS_ACCESS, subscription: subscription])}"/>
                    <g:if test="${statsInfo}">
                        <ui:msg showIcon="true" class="warning" noClose="true" header="${message(code: 'default.stats.info.header')}">
                            ${statsInfo[0]}<br>
                            <g:message code="default.stats.noCounterSupport"/>
                        </ui:msg>
                    </g:if>
                    <ui:msg showIcon="true" class="info" noClose="true" header="${message(code: 'default.stats.contact.header')}">
                        <a href="#" class="infoFlyout-trigger" data-template="contactStats" data-platform="${platform.id}"><g:message code="default.stats.contact.link"/></a>
                    </ui:msg>
                    <laser:render template="/info/flyoutWrapper"/>
                    <g:if test="${platform.statisticsFormat.contains('COUNTER')}">
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
                        <%
                            Map<String, Object> platformSushiConfig = exportService.prepareSushiCall(platform, 'stats')
                        %>
                        <table class="ui celled table">
                            <tr>
                                <th><g:message code="default.number"/></th>
                                <th><g:message code="default.institution"/></th>
                                <th>Customer ID</th>
                                <th>Requestor ID/API-Key</th>
                                <th><g:message code="default.usage.sushiCallCheck.header"/></th>
                                <th class="center aligned">
                                    <ui:optionsIcon />
                                </th>
                            </tr>
                            <g:each in="${Subscription.executeQuery('select new map(sub.id as memberSubId, org.sortname as memberName, org.id as memberId, ci as customerIdentifier) from CustomerIdentifier ci, OrgRole oo join oo.org org join oo.sub sub where ci.customer = org and sub.instanceOf = :parent and oo.roleType in (:subscrRoles) and ci.platform.gokbId = :platform order by ci.customer.sortname asc', [parent: subscription, platform: platform.uuid, subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}" var="row" status="i">
                                <tr>
                                    <td>${i+1}</td>
                                    <td>
                                        <g:link controller="organisation" action="show" id="${row.memberId}">${row.memberName}</g:link>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${row.customerIdentifier}" field="value"/>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${row.customerIdentifier}" field="requestorKey"/>
                                    </td>
                                    <td id="${genericOIDService.getHtmlOID(row.customerIdentifier)}" class="counterApiConnectionCheck" data-platform="${platform.uuid}" data-customerId="${row.customerIdentifier.value}" data-requestorId="${row.customerIdentifier.requestorKey}">

                                    </td>
                                    <td>
                                        <g:link class="${Btn.ICON.SIMPLE}" action="stats" id="${row.memberSubId}" role="button" aria-label="${message(code: 'default.usage.consortiaTableHeader')}"><i class="${Icon.STATS}"></i></g:link>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <g:render template="/templates/stats/stats"/>
        </g:else>
    <laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
         $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
         var name = e.target.files[0].name;
         $('input:text', $(e.target).parent()).val(name);
    });

    $(".counterApiConnectionCheck").each(function(i) {
        let cell = $(this);
        let data = {
            platform: cell.attr("data-platform"),
            customerId: cell.attr("data-customerId"),
            requestorId: cell.attr("data-requestorId")
        };
        $.ajax({
            url: "<g:createLink controller="ajaxJson" action="checkCounterAPIConnection"/>",
            data: data
        }).done(function(response) {
            if(response.error === true) {
               cell.html('<span class="la-popup-tooltip" data-content="'+response.message+'"><i class="circular inverted icon red times"></i></span>');
               r2d2.initDynamicUiStuff('#'+cell.attr('id'));
            }
        });
    });
    </laser:script>

<laser:htmlEnd />
