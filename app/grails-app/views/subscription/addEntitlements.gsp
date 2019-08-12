<%@ page import="grails.converters.JSON; de.laser.helper.RDStore; com.k_int.kbplus.Subscription; com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.label', default: 'Subscription')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.addEntitlements.label', default: 'Add Entitlements')}"/>
</semui:breadcrumbs>
<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>
<h1 class="ui left aligned icon header"><semui:headerIcon/>

<g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name"
               class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
</h1>

<g:render template="nav"/>

<g:set var="counter" value="${offset + 1}"/>
${message(code: 'subscription.details.availableTitles', default: 'Available Titles')} ( ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (tipps?.size())), num_tipp_rows])} )

<semui:filter>
    <g:form class="ui form" action="addEntitlements" params="${params}" method="get">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">

        <div class="fields two">
            <div class="field">
                <label for="filter">${message(code: 'subscription.compare.filter.title', default: 'Filters - Title')}</label>
                <input id="filter" name="filter" value="${params.filter}"/>
            </div>

            <div class="field">
                <label for="pkgfilter">${message(code: 'subscription.details.from_pkg', default: 'From Package')}</label>
                <select id="pkgfilter" name="pkgfilter">
                    <option value="">${message(code: 'subscription.details.from_pkg.all', default: 'All')}</option>
                    <g:each in="${subscriptionInstance.packages}" var="sp">
                        <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
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
                       value="${message(code: 'default.button.filter.label', default: 'Filter')}">
            </div>
        </div>

    </g:form>
</semui:filter>
<g:if test="${flash.error}">
    <semui:messages data="${flash}"/>
</g:if>
<h3 class="ui dividing header"><g:message code="subscription.details.addEntitlements.header"/></h3>
<semui:msg class="warning" header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.warning" />
<g:form class="ui form" controller="subscription" action="addEntitlements"
        params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscriptionInstance.id]}"
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
<r:script>
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</r:script>
<g:form action="processAddEntitlements" class="ui form">
    <input type="hidden" name="id" value="${subscriptionInstance.id}"/>
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
    <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
        <thead>
        <tr>
            <th rowspan="3" style="vertical-align:middle;">
                <%
                    String allChecked = "checked"
                    checked.each { e ->
                        if(e != "checked")
                            allChecked = ""
                    }
                %>
                <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}
                                                onClick="javascript:selectAll();"/></g:if>
            </th>
            <th rowspan="3"><g:message code="sidewide.number"/></th>
            <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle" title="${message(code: 'title.label', default: 'Title')}"/>
            <th><g:message code="tipp.coverage"/></th>
            <th><g:message code="tipp.access"/></th>
            <th><g:message code="tipp.coverageDepth"/></th>
            <th><g:message code="tipp.embargo"/></th>
            <th><g:message code="tipp.coverageNote"/></th>
            <g:if test="${uploadPriceInfo}">
                <th><g:message code="tipp.listPrice"/></th>
                <th><g:message code="tipp.localPrice"/></th>
                <th><g:message code="tipp.priceDate"/></th>
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
                <td><input type="checkbox" name="bulkflag" class="bulkcheck" ${checked[tipp.gokbId]}></td>
            <td>${counter++}</td>

            <td>
            <semui:listIcon type="${tipp.title?.type?.value}"/>
            <strong><g:link controller="title" action="show"
                            id="${tipp.title.id}">${tipp.title.title}</g:link></strong>

            <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && tipp?.title?.volume}">
                (${message(code: 'title.volume.label')} ${tipp?.title?.volume})
            </g:if>

            <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && (tipp?.title?.firstAuthor || tipp?.title?.firstEditor)}">
                <br><b>${tipp?.title?.getEbookFirstAutorOrFirstEditor()}</b>
            </g:if>

            <br>
            <g:link controller="tipp" action="show"
                    id="${tipp.id}">${message(code: 'platform.show.full_tipp', default: 'Full TIPP Details')}</g:link>
            &nbsp;&nbsp;&nbsp;
            <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                    var="gokbAPI">
                <g:if test="${tipp?.gokbId}">
                    <a target="_blank"
                       href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}"><i
                            title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                </g:if>
            </g:each>
            <br>

            <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                <div class="item"><b>${message(code: 'title.editionStatement.label')}:</b> ${tipp?.title?.editionStatement}
                </div>
            </g:if>

            <g:if test="${tipp.hostPlatformURL}">
                <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.tooltip.callUrl')}"
                   href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"
                   target="_blank"><i class="share square icon"></i></a>
            </g:if>

            <g:each in="${tipp?.title?.ids?.sort { it?.identifier?.ns?.ns }}" var="id">
                <g:if test="${id.identifier.ns.ns == 'originEditUrl'}">
                    <%--<span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail"><a
                            href="${id.identifier.value}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                    </div>
                    </span>
                    <span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail"><a
                            href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                    </div>
                    </span>--%>
                </g:if>
                <g:else>
                    <span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                    </span>
                </g:else>
            </g:each>


            <div class="ui list">
                <div class="item" title="${tipp.availabilityStatusExplanation}">
                    <b>${message(code: 'default.access.label', default: 'Access')}:</b> ${tipp.availabilityStatus?.getI10n('value')}
                </div>

            </div>

            <div class="item">
                <b>${message(code: 'default.status.label', default: 'Status')}:</b>
                <%--<semui:xEditableRefData owner="${tipp}" field="status" config="TIPP Status"/>--%>
                ${tipp.status.getI10n('value')}
            </div>

            <div class="item"><b>${message(code: 'tipp.package', default: 'Package')}:</b>

                <div class="la-flexbox">
                    <i class="icon gift scale la-list-icon"></i>
                    <g:link controller="package" action="show" id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                </div>
            </div>

            <div class="item"><b>${message(code: 'tipp.platform', default: 'Platform')}:</b>
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
                    <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                       href="${tipp?.platform?.primaryUrl?.contains('http') ? tipp?.platform?.primaryUrl : 'http://' + tipp?.platform?.primaryUrl}"
                       target="_blank"><i class="share square icon"></i></a>
                </g:if>
            </div>



            </div>
        </td>

            <td>
                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                    <%-- TODO contact Ingrid! ---> done as of subtask of ERMS-1490 --%>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                    <semui:datepicker class="ieOverwrite" placeholder="${message(code: 'title.dateFirstInPrint.label')}" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstInPrint : tipp.title?.dateFirstInPrint}"/>
                    <%--${tipp?.title?.dateFirstInPrint}--%>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                    <semui:datepicker class="ieOverwrite" placeholder="${message(code: 'title.dateFirstOnline.label')}" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstOnline : tipp.title?.dateFirstOnline}"/>
                    <%--${tipp?.title?.dateFirstOnline}--%>
                </g:if>
                <g:else>
                    <!-- von -->
                    <semui:datepicker class="ieOverwrite" name="ieStartDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.startDate : tipp.startDate}" placeholder="${message(code:'tipp.startDate')}"/>
                    <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.startDate}"/>--%><br>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                    <input name="ieStartVolume" type="text" class="ui input ieOverwrite" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.startVolume : tipp.startVolume}" placeholder="${message(code: 'tipp.volume')}">
                    <%--${tipp?.startVolume}--%><br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                    <input name="ieStartIssue" type="text" class="ui input ieOverwrite" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.startIssue : tipp.startIssue}" placeholder="${message(code: 'tipp.issue')}">
                    <%--${tipp?.startIssue}--%>
                    <semui:dateDevider/>
                    <!-- bis -->
                    <semui:datepicker class="ieOverwrite" name="ieEndDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.endDate : tipp.endDate}" placeholder="${message(code:'tipp.endDate')}"/>
                    <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.endDate}"/><br>--%>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                    <input name="ieEndVolume" type="text" class="ui input ieOverwrite" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.endVolume : tipp.endVolume}" placeholder="${message(code: 'tipp.volume')}">
                    <%--${tipp?.endVolume}--%><br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                    <input name="ieEndIssue" type="text" class="ui input ieOverwrite" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.endIssue : tipp.endIssue}" placeholder="${message(code: 'tipp.issue')}">
                    <%--${tipp?.endIssue}--%>
                </g:else>
            </td>
            <td>
                <!-- von -->
                <semui:datepicker class="ieOverwrite" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessStartDate : tipp.accessStartDate}" placeholder="${message(code:'tipp.accessStartDate')}"/>
                <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessStartDate}"/>--%>
                <semui:dateDevider/>
                <!-- bis -->
                <semui:datepicker class="ieOverwrite" name="ieAccessEnd" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessEndDate : tipp.accessEndDate}" placeholder="${message(code:'tipp.accessEndDate')}"/>
                <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessEndDate}"/>--%>
            </td>
            <td>
                <%--${tipp.coverageDepth}--%>
                <input class="ieOverwrite" name="coverageDepth" type="text" placeholder="${message(code:'tipp.coverageDepth')}" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.coverageDepth : tipp.coverageDepth}">
            </td>
            </td>
            <td>
                <%--${tipp.embargo}--%>
                <input class="ieOverwrite" name="embargo" type="text" placeholder="${message(code:'tipp.embargo')}" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.embargo : tipp.embargo}">
            </td>
            <td>
                <%--${tipp.coverageNote}--%>
                <input class="ieOverwrite" name="coverageNote" type="text" placeholder="${message(code:'tipp.coverageNote')}" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.coverageNote : tipp.coverageNote}">
            </td>
            <g:if test="${uploadPriceInfo}">
                <td>
                    <g:formatNumber number="${issueEntitlementOverwrite[tipp.gokbId]?.listPrice}" type="currency" currencySymbol="${issueEntitlementOverwrite[tipp.gokbId]?.listPriceCurrency}" currencyCode="${issueEntitlementOverwrite[tipp.gokbId]?.listPriceCurrency}"/>
                </td>
                <td>
                    <g:formatNumber number="${issueEntitlementOverwrite[tipp.gokbId]?.localPrice}" type="currency" currencySymbol="${issueEntitlementOverwrite[tipp.gokbId]?.localPriceCurrency}" currencyCode="${issueEntitlementOverwrite[tipp.gokbId]?.localPriceCurrency}"/>
                </td>
                <td>
                    <semui:datepicker class="ieOverwrite" name="priceDate" value="${issueEntitlementOverwrite[tipp.gokbId]?.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>
                </td>
            </g:if>
            <td>
                <g:link class="ui icon positive button" action="processAddEntitlements"
                        params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}"
                        data-tooltip="${message(code: 'subscription.details.addEntitlements.add_now')}">
                    <i class="plus icon"></i>
                </g:link>
            </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="paginateButtons" style="text-align:center">
        <input type="submit"
               value="${message(code: 'subscription.details.addEntitlements.add_selected', default: 'Add Selected Entitlements')}"
               class="ui button"/>
    </div>


    <g:if test="${tipps}">
        <%
            params.remove("kbartPreselect")
        %>
        <semui:paginate controller="subscription"
                        action="addEntitlements"
                        params="${params + [pagination: true]}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}"
                        total="${num_tipp_rows}"/>
    </g:if>

</g:form>

<r:script language="JavaScript">
    <% /*
      $(document).ready(function() {
        $('span.newipe').editable('<g:createLink controller="ajax" action="genericSetValue" />', {
          type      : 'textarea',
          cancel    : '${message(code:'default.button.cancel.label', default:'Cancel')}',
          submit    : '${message(code:'default.button.ok.label', default:'OK')}',
          id        : 'elementid',
          rows      : 3,
          tooltip   : '${message(code:'default.click_to_edit', default:'Click to edit...')}'
        });
      });
    */ %>
    $("simpleHiddenRefdata").editable({
        url: function(params) {
          var hidden_field_id = $(this).data('hidden-id');
          $("#"+hidden_field_id).val(params.value);
          // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
        }
      });

    $(".bulkcheck").change(function() {
        updateSelectionCache($(this).parents("tr").attr("data-index"),$(this).prop('checked'));
    });

    $(".ieOverwrite").change(function() {
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateIssueEntitlementOverwrite" />",
            data: {
                sub: ${subscriptionInstance.id},
                key: $(this).parents("tr").attr("data-index"),
                referer: "${actionName}",
                prop: $(this).attr("name"),
                propValue: $(this).val()
            }
        }).done(function(result){

        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });

    function selectAll() {
      $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
      updateSelectionCache("all",$('#select-all').prop('checked'));
    }

    function updateSelectionCache(index,checked) {
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateChecked" />",
            data: {
                sub: ${subscriptionInstance.id},
                index: index,
                referer: "${actionName}",
                checked: checked
            }
        }).done(function(result){

        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    }
</r:script>

</body>
</html>
