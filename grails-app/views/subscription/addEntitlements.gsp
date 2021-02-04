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
<semui:xEditable owner="${subscription}" field="name"/>
</h1>
<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.addEntitlements.label')}</h2>
<%-- <g:render template="nav"/> --%>

<g:set var="counter" value="${offset + 1}"/>
${message(code: 'subscription.details.availableTitles')} ( ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (tipps?.size())), num_tipp_rows])} )

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form class="ui form" action="addEntitlements" params="${params}" method="get">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">

        <div class="fields two">
            <div class="field">
                <label for="filter">${message(code: 'subscription.compare.filter.title')}</label>
                <input id="filter" name="filter" value="${params.filter}"/>
            </div>

            <div class="field">
                <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                <select id="pkgfilter" name="pkgfilter">
                    <option value="">${message(code: 'subscription.details.from_pkg.all')}</option>
                    <g:if test="${params.packageLinkPreselect}">
                        <option value="${params.packageLinkPreselect}" selected=selected>${params.preselectedName}</option>
                    </g:if>
                    <%--<g:elseif test="${!subscription.packages.find { sp -> sp.pkg.gokbId == params.pkgFilter}}">
                        <option value="${params.pkgFilter}" selected=selected>${params.preselectedName}</option>
                    </g:elseif>--%>
                    <g:each in="${subscription.packages}" var="sp">
                        <option value="${sp.pkg.gokbId}" ${sp.pkg.gokbId == params.pkgfilter ? 'selected=selected' : ''}>${sp.pkg.name}</option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="three fields">
            <div class="field">
                <semui:datepicker label="default.startsBefore.label" id="startsBefore" name="startsBefore"
                                  value="${params.startsBefore}"/>
            </div>

            <div class="field">
                <semui:datepicker label="default.endsAfter.label" id="endsAfter" name="endsAfter"
                                  value="${params.endsAfter}"/>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>
        </div>

    </g:form>
</semui:filter>

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
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
                   class="fluid ui button"/>
        </div>
        <div class="field"></div>
    </div>
    <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
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
            <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortName" title="${message(code: 'title.label')}"/>
            <th><g:message code="tipp.coverage"/></th>
            <th><g:message code="tipp.access"/></th>
            <g:if test="${uploadPriceInfo}">
                <th><g:message code="tipp.listPrice"/></th>
                <th><g:message code="tipp.localPrice"/></th>
                <th><g:message code="tipp.priceStartDate"/></th>
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
            <semui:listIcon type="${tipp.titleType}"/>
            <strong><g:link controller="tipp" action="show"
                            id="${tipp.id}">${tipp.name}</g:link></strong>

            <g:if test="${tipp.titleType.contains('Book') && tipp.volume}">
                (${message(code: 'title.volume.label')} ${tipp.volume})
            </g:if>

            <g:if test="${tipp.titleType.contains('Book') && (tipp.firstAuthor || tipp.firstEditor)}">
                <br /><strong>${tipp.getEbookFirstAutorOrFirstEditor()}</strong>
            </g:if>

            <br />
                <g:if test="${tipp?.id}">
                    <div class="la-title">${message(code: 'default.details.label')}</div>
                    <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'laser')}"
                            href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                            target="_blank"
                            controller="tipp" action="show"
                            id="${tipp?.id}">
                        <i class="book icon"></i>
                    </g:link>
                </g:if>
                <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                        var="gokbAPI">
                    <g:if test="${tipp?.gokbId}">
                        <a role="button"
                           class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                           data-content="${message(code: 'gokb')}"
                           href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}"
                           target="_blank"><i class="la-gokb  icon"></i>
                        </a>
                    </g:if>
                </g:each>
            <br />

            <g:if test="${tipp.titleType.contains('Book')}">
                <g:if test="${tipp.editionStatement}">
                <div class="item"><strong>${message(code: 'title.editionStatement.label')}:</strong> ${tipp.editionStatement}
                </div>
                </g:if>
                <g:if test="${tipp.summaryOfContent}">
                <div class="item">
                     ${tipp.summaryOfContent}
                </div>
                </g:if>
            </g:if>

                <g:if test="${tipp.seriesName}">
                    <div class="item">
                        <i class="grey icon list la-popup-tooltip la-delay" data-content="${message(code: 'title.seriesName.label')}"></i>
                        <div class="content">
                            ${tipp.seriesName}
                        </div>
                    </div>
                </g:if>

                <g:if test="${tipp.subjectReference}">
                    <div class="item">
                        <i class="grey icon comment alternate la-popup-tooltip la-delay" data-content="${message(code: 'title.subjectReference.label')}"></i>
                        <div class="content">
                            ${tipp.subjectReference}
                        </div>
                    </div>
                </g:if>

            <g:if test="${tipp.hostPlatformURL}">
                <semui:linkIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
            </g:if>
            <br />
            <g:each in="${tipp.ids?.sort { it.ns.ns }}" var="id">
                <span class="ui small blue image label">
                    ${id.ns.ns}: <div class="detail">${id.value}</div>
                </span>
            </g:each>


            <div class="ui list">
                <div class="item" title="${tipp.availabilityStatusExplanation}">
                    <strong>${message(code: 'default.access.label')}:</strong> ${tipp.availabilityStatus?.getI10n('value')}
                </div>

            </div>

            <div class="item">
                <strong>${message(code: 'default.status.label')}:</strong>
                <%--<semui:xEditableRefData owner="${tipp}" field="status" config="${de.laser.helper.RDConstants.TIPP_STATUS}"/>--%>
                ${tipp.status.getI10n('value')}
            </div>

            <div class="item"><strong>${message(code: 'package.label')}:</strong>

                <div class="la-flexbox">
                    <i class="icon gift scale la-list-icon"></i>
                    <g:link controller="package" action="show" id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                </div>
            </div>

            <div class="item"><strong>${message(code: 'tipp.platform')}:</strong>
                <g:if test="${tipp?.platform.name}">
                    ${tipp?.platform.name}
                </g:if>
                <g:else>${message(code: 'default.unknown')}</g:else>
                <g:if test="${tipp?.platform.name}">
                    <g:link class="ui icon mini  button la-url-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'tipp.tooltip.changePlattform')}"
                            controller="platform" action="show" id="${tipp?.platform.id}">
                        <i class="pencil alternate icon"></i>
                    </g:link>
                </g:if>
                <g:if test="${tipp?.platform?.primaryUrl}">
                    <a role="button" class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                       href="${tipp?.platform?.primaryUrl?.contains('http') ? tipp?.platform?.primaryUrl : 'http://' + tipp?.platform?.primaryUrl}"
                       target="_blank"><i class="share square icon"></i></a>
                </g:if>
            </div>
        </td>

            <td>
                <g:if test="${tipp.titleType.contains('Book')}">
                    <%-- TODO contact Ingrid! ---> done as of subtask of ERMS-1490 --%>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                   %{-- <semui:datepicker class="ieOverwrite" placeholder="${message(code: 'title.dateFirstInPrint.label')}" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstInPrint : tipp.title?.dateFirstInPrint}"/>
                    <%--${tipp.title.dateFirstInPrint}--%>--}%
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstInPrint}"/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                    %{--<semui:datepicker class="ieOverwrite" placeholder="${message(code: 'title.dateFirstOnline.label')}" name="ieAccessEnd" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstOnline : tipp.title?.dateFirstOnline}"/>
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
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                        <input data-coverage="true" name="startVolume${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startVolume}" placeholder="${message(code: 'tipp.volume')}">
                        <%--${tipp?.startVolume}--%><br />
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                        <input data-coverage="true" name="startIssue${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startIssue}" placeholder="${message(code: 'tipp.issue')}">
                        <%--${tipp?.startIssue}--%>
                        <semui:dateDevider/>
                        <!-- bis -->
                        <semui:datepicker class="ieOverwrite coverage" name="endDate${key}" value="${covStmt.endDate}" placeholder="${message(code:'tipp.endDate')}"/>
                        <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.endDate}"/><br />--%>
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                        <input data-coverage="true" name="endVolume${key}" type="text" class="ui input ieOverwrite" value="${covStmt.endVolume}" placeholder="${message(code: 'tipp.volume')}">
                        <%--${tipp?.endVolume}--%><br />
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                        <input data-coverage="true" name="endIssue${key}" type="text" class="ui input ieOverwrite" value="${covStmt.endIssue}" placeholder="${message(code: 'tipp.issue')}">
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
                    <semui:datepicker class="ieOverwrite" name="priceDate" value="${issueEntitlementOverwrite[tipp.gokbId]?.priceDate}" placeholder="${message(code:'tipp.priceStartDate')}"/>
                </td>
            </g:if>
            <td>
                <g:link class="ui icon positive button la-popup-tooltip la-delay" action="processAddEntitlements"
                        params="${[id: subscription.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}"
                        data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                    <i class="plus icon"></i>
                </g:link>
            </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="paginateButtons" style="text-align:center">
        <input type="submit"
               value="${message(code: 'subscription.details.addEntitlements.add_selected')}"
               class="ui button"/>
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
