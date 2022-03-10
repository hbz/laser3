<%@ page import="de.laser.Subscription; de.laser.ApiSource; grails.converters.JSON; de.laser.helper.RDStore; de.laser.Platform;de.laser.titles.BookInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.addEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.addEntitlements.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="iconSubscriptionIsChild"/>
</g:if>
<semui:xEditable owner="${subscription}" field="name"/>
</h1>
<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.addEntitlements.label')}</h2>
<%-- <g:render template="nav"/> --%>
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="message"/>
</g:if>

<g:set var="counter" value="${offset + 1}"/>

<g:render template="/templates/filter/tipp_ieFilter"/>

    <semui:messages data="${flash}"/>

<h3 class="ui dividing header"><g:message code="subscription.details.addEntitlements.header"/></h3>
<semui:msg class="warning" header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.warning" />
<g:form class="ui form" controller="subscription" action="addEntitlements"
        params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscription.id]}"
        method="post" enctype="multipart/form-data">
    <div class="three fields">
        <div class="field">
            <div class="ui fluid action input">
                <input type="text" readonly="readonly"
                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                       style="display: none;">

                <div class="ui icon button">
                    <i class="attach icon"></i>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="ui checkbox toggle">
                <g:checkBox name="preselectValues" value="${preselectValues}"/>
                <label><g:message code="subscription.details.addEntitlements.preselectValues"/></label>
            </div>
            <div class="ui checkbox toggle">
                <g:checkBox name="preselectCoverageDates" value="${preselectCoverageDates}"/>
                <label><g:message code="subscription.details.addEntitlements.preselectCoverageDates"/></label>
            </div>
            <div class="ui checkbox toggle">
                <g:checkBox name="uploadPriceInfo" value="${uploadPriceInfo}"/>
                <label><g:message code="subscription.details.addEntitlements.uploadPriceInfo"/></label>
            </div>
        </div>

        <div class="field">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.preselect')}"
                   class="fluid ui button"/>
        </div>
    </div>
</g:form>
<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</laser:script>
<g:form action="processAddEntitlements" class="ui form">
    <input type="hidden" name="id" value="${subscription.id}"/>
    <g:hiddenField name="preselectCoverageDates" value="${preselectCoverageDates}"/>
    <g:hiddenField name="uploadPriceInfo" value="${uploadPriceInfo}"/>

    <div class="three fields">
        <div class="field"></div>

        <div class="field">
            <g:if test="${!blockSubmit}">
                <input type="submit"
                       value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
                       class="fluid ui button"/>
            </g:if>
            <g:else>
                <div data-tooltip="${message(code: 'subscription.details.addEntitlements.thread.running')}">
                    <input type="submit" disabled="disabled"
                           value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
                           class="fluid ui button"/>
                </div>
            </g:else>
        </div>
        <div class="field"></div>
    </div>
    <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
        <thead>
        <tr>
            <th rowspan="3" style="vertical-align:middle;">
                <%
                    String allChecked = ""
                    checked.each { e ->
                        if(e != "checked")
                            allChecked = ""
                    }
                %>
                <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>
            </th>
            <th rowspan="3"><g:message code="sidewide.number"/></th>
            <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortname" title="${message(code: 'title.label')}"/>
            <th><g:message code="tipp.coverage"/></th>
            <th><g:message code="tipp.access"/></th>
            <g:if test="${uploadPriceInfo}">
                <th><g:message code="tipp.price.listPrice"/></th>
                <th><g:message code="tipp.price.localPrice"/></th>
                <th><g:message code="tipp.price.startDate"/></th>
            </g:if>
            <th><g:message code="default.actions.label"/></th>
        </tr>
        <tr>

            <th colspan="1" rowspan="2"></th>

            <th>${message(code: 'default.from')}</th>
            <th>${message(code: 'default.from')}</th>
            <th colspan="7" rowspan="2"></th>
        </tr>
        <tr>
            <th>${message(code: 'default.to')}</th>
            <th>${message(code: 'default.to')}</th>
        </tr>
        </thead>

        <tbody>
        <g:each in="${tipps}" var="tipp">
            <tr data-index="${tipp.gokbId}">
                <td><input type="checkbox" name="bulkflag" class="bulkcheck" ${checked ? checked[tipp.gokbId] : ''}></td>
            <td>${counter++}</td>

            <td>
                <!-- START TEMPLATE -->
                <g:render template="/templates/title_short"
                          model="${[ie: null, tipp: tipp,
                                    showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                <!-- END TEMPLATE -->
            </td>

            <td>
                <g:if test="${(tipp.titleType == 'Book')}">
                    <%-- TODO contact Ingrid! ---> done as of subtask of ERMS-1490 --%>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.dateFirstInPrint')}"></i>
                   %{-- <semui:datepicker class="ieOverwrite" placeholder="${message(code: 'tipp.dateFirstInPrint')}" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstInPrint : tipp.title?.dateFirstInPrint}"/>
                    <%--${tipp.title.dateFirstInPrint}--%>--}%
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstInPrint}"/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
                    %{--<semui:datepicker class="ieOverwrite" placeholder="${message(code: 'tipp.dateFirstOnline')}" name="ieAccessEnd" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstOnline : tipp.title?.dateFirstOnline}"/>
                    <%--${tipp.title.dateFirstOnline}--%>--}%
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstOnline}"/>
                </g:if>
                <g:else>
                    <%-- The check if preselectCoverageStatements is set is done server-side; this is implicitely done when checking if the issueEntitlementOverwrite map has the coverage statement list.
                        In order to define and initialise the list, it is mandatory that the foresaid flag is set to true. Compare with SubscriptionController.addEntitlements()
                     --%>
                    <g:set var="coverageStatements" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.coverages : tipp.coverages}"/>
                    <g:each in="${coverageStatements}" var="covStmt" status="key">
                        <!-- von -->
                        <semui:datepicker class="ieOverwrite coverage" name="startDate${key}" value="${covStmt.startDate}" placeholder="${message(code:'tipp.startDate')}"/>
                        <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.startDate}"/>--%><br />
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.startVolume')}"></i>
                        <input data-coverage="true" name="startVolume${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startVolume}" placeholder="${message(code: 'tipp.startVolume')}">
                        <%--${tipp?.startVolume}--%><br />
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.startIssue')}"></i>
                        <input data-coverage="true" name="startIssue${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startIssue}" placeholder="${message(code: 'tipp.startIssue')}">
                        <%--${tipp?.startIssue}--%>
                        <semui:dateDevider/>
                        <!-- bis -->
                        <semui:datepicker class="ieOverwrite coverage" name="endDate${key}" value="${covStmt.endDate}" placeholder="${message(code:'tipp.endDate')}"/>
                        <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.endDate}"/><br />--%>
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.endVolume')}"></i>
                        <input data-coverage="true" name="endVolume${key}" type="text" class="ui input ieOverwrite" value="${covStmt.endVolume}" placeholder="${message(code: 'tipp.endVolume')}">
                        <%--${tipp?.endVolume}--%><br />
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.endIssue')}"></i>
                        <input data-coverage="true" name="endIssue${key}" type="text" class="ui input ieOverwrite" value="${covStmt.endIssue}" placeholder="${message(code: 'tipp.endIssue')}">
                        <%--${tipp?.endIssue}--%><br />
                        <%--${tipp.coverageDepth}--%>
                        <input data-coverage="true" class="ieOverwrite" name="coverageDepth${key}" type="text" placeholder="${message(code:'tipp.coverageDepth')}" value="${covStmt.coverageDepth}"><br />
                        <%--${tipp.embargo}--%>
                        <input data-coverage="true" class="ieOverwrite" name="embargo${key}" type="text" placeholder="${message(code:'tipp.embargo')}" value="${covStmt.embargo}"><br />
                        <%--${tipp.coverageNote}--%>
                        <input data-coverage="true" class="ieOverwrite" name="coverageNote${key}" type="text" placeholder="${message(code:'tipp.coverageNote')}" value="${covStmt.coverageNote}"><br />
                    </g:each>
                </g:else>
            </td>
                <td>
                    <!-- von -->
                    <semui:datepicker class="ieOverwrite" name="accessStartDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessStartDate : tipp.accessStartDate}" placeholder="${message(code:'tipp.accessStartDate')}"/>
                    <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessStartDate}"/>--%>
                    <semui:dateDevider/>
                    <!-- bis -->
                    <semui:datepicker class="ieOverwrite" name="accessEndDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessEndDate : tipp.accessEndDate}" placeholder="${message(code:'tipp.accessEndDate')}"/>
                    <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessEndDate}"/>--%>
                </td>
            <g:if test="${uploadPriceInfo}">
                <td>
                    <g:formatNumber number="${issueEntitlementOverwrite[tipp.gokbId]?.listPrice}" type="currency" currencySymbol="${issueEntitlementOverwrite[tipp.gokbId]?.listCurrency}" currencyCode="${issueEntitlementOverwrite[tipp.gokbId]?.listCurrency}"/>
                </td>
                <td>
                    <g:formatNumber number="${issueEntitlementOverwrite[tipp.gokbId]?.localPrice}" type="currency" currencySymbol="${issueEntitlementOverwrite[tipp.gokbId]?.localCurrency}" currencyCode="${issueEntitlementOverwrite[tipp.gokbId]?.localCurrency}"/>
                </td>
                <td>
                    <semui:datepicker class="ieOverwrite" name="priceDate" value="${issueEntitlementOverwrite[tipp.gokbId]?.priceDate}" placeholder="${message(code:'tipp.price.startDate')}"/>
                </td>
            </g:if>
            <td>
                <g:if test="${!blockSubmit}">
                    <g:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay" action="processAddEntitlements"
                            params="${[id: subscription.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}"
                            data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                        <i class="plus icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <div data-tooltip="${message(code: 'subscription.details.addEntitlements.thread.running')}">
                        <g:link class="ui icon disabled button la-popup-tooltip la-delay" action="processAddEntitlements"
                                params="${[id: subscription.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}">
                            <i class="plus icon"></i>
                        </g:link>
                    </div>
                </g:else>
            </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="paginateButtons" style="text-align:center">
        <g:if test="${!blockSubmit}">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
                   class="ui button"/>
        </g:if>
        <g:else>
            <div data-tooltip="${message(code: 'subscription.details.addEntitlements.thread.running')}">
                <input type="submit" disabled="disabled"
                       value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
                       class="ui button"/>
            </div>
        </g:else>
    </div>


    <g:if test="${tipps}">
        <%
            params.remove("kbartPreselect")
        %>
        <semui:paginate controller="subscription"
                        action="addEntitlements"
                        params="${params + [pagination: true]}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}"
                        max="${max}"
                        total="${num_tipp_rows}"/>
    </g:if>

</g:form>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.selectAll = function () {
        $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
        JSPC.app.updateSelectionCache("all",$('#select-all').prop('checked'));
    }

    JSPC.app.updateSelectionCache = function (index,checked) {
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateChecked" />",
                data: {
                    sub: ${subscription.id},
                    index: index,
                    <g:if test="${params.pkgfilter}">
                        packages: ${params.pkgfilter},
                    </g:if>
                    referer: "${actionName}",
                    checked: checked
                }
            }).done(function(result){

            }).fail(function(xhr,status,message){
                console.log("error occurred, consult logs!");
            });
    }

    $("#select-all").change(function() {
        JSPC.app.selectAll();
    });

    $(".bulkcheck").change(function() {
        JSPC.app.updateSelectionCache($(this).parents("tr").attr("data-index"),$(this).prop('checked'));
    });

    $(".ieOverwrite td").click(function() {
        $(".ieOverwrite").trigger("change");
    });

    $(".ieOverwrite").change(function() {
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateIssueEntitlementOverwrite" />",
            data: {
                sub: ${subscription.id},
                key: $(this).parents("tr").attr("data-index"),
                referer: "${actionName}",
                coverage: $(this).attr("data-coverage") === "true" || $(this).hasClass("coverage"),
                prop: $(this).attr("name") ? $(this).attr("name") : $(this).find("input").attr("name"),
                propValue: $(this).val() ? $(this).val() : $(this).find("input").val()
            }
        }).done(function(result){

        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });


</laser:script>

</body>
</html>
