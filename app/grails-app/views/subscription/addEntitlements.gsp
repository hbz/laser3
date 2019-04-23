<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform" %>
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

<g:render template="nav" contextPath="."/>

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
<%
    List zdbIds = []
    List onlineIds = []
    List printIds = []
    if (identifiers) {
        zdbIds = identifiers.zdbIds
        onlineIds = identifiers.onlineIds
        printIds = identifiers.printIds
    }
%>
<g:if test="${flash.error}">
    <semui:messages data="${flash}"/>
</g:if>
<g:form class="ui form" controller="subscription" action="addEntitlements"
        params="${[identifiers: identifiers, sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscriptionInstance.id]}"
        method="post" enctype="multipart/form-data">
    <div class="two fields">
        <div class="field">
            <div class="ui fluid action input">
                <input type="text" readonly="readonly"
                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                       style="display: none;">

                <div class="ui icon button" style="padding-left:30px; padding-right:30px">
                    <i class="attach icon"></i>
                </div>
            </div>
        </div>

        <div class="field">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.preselect', default: 'Preselect Entitlements per KBART-File')}"
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
    <input type="hidden" name="siid" value="${subscriptionInstance.id}"/>

    <div class="two fields">
        <div class="field"></div>

        <div class="field">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.add_selected', default: 'Add Selected Entitlements')}"
                   class="fluid ui button"/>
        </div>
    </div>
    <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
        <thead>
        <tr>
            <th rowspan="3" style="vertical-align:middle;">
                <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall"
                                                onClick="javascript:selectAll();"/></g:if>
            </th>
            <th rowspan="3">${message(code: 'sidewide.number')}</th>
            <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle"
                              title="${message(code: 'title.label', default: 'Title')}"/>
            <th class="two wide">${message(code: 'tipp.coverage')}</th>
            <th class="two wide">${message(code: 'tipp.access')}</th>
            <th class="two wide">${message(code: 'tipp.coverageDepth', default: 'Coverage Depth')}</th>
            <th class="two wide">${message(code: 'tipp.embargo', default: 'Embargo')}</th>
            <th class="two wide">${message(code: 'tipp.coverageNote', default: 'Coverage Note')}</th>
            <th class="two wide">${message(code: 'default.actions.label')}</th>
        </tr>
        <tr>

            <th colspan="1" rowspan="2"></th>

            <th>${message(code: 'default.from')}</th>
            <th>${message(code: 'default.from')}</th>
            <th colspan="4" rowspan="2"></th>
        </tr>
        <tr>
            <th>${message(code: 'default.to')}</th>
            <th>${message(code: 'default.to')}</th>
        </tr>
        </thead>

        <tbody>
        <g:each in="${tipps}" var="tipp" status="t">
            <tr>
                <td><input type="checkbox" name="_bulkflag.${tipp.id}" class="bulkcheck" ${checked[t]}/></td>
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
            <g:each in="${tipp.title.ids.sort { it.identifier.ns.ns }}" var="id">
                <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                    <span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail"><a
                            href="${id.identifier.value}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                    </div>
                    </span>
                    <span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail"><a
                            href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                    </div>
                    </span>
                </g:if>
                <g:else>
                    <span class="ui small teal image label">
                        ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                    </span>
                </g:else>
            </g:each>


            <div class="ui list">
                <div class="item"
                     title="${tipp.availabilityStatusExplanation}"><b>${message(code: 'default.access.label', default: 'Access')}:</b> ${tipp.availabilityStatus?.getI10n('value')}
                </div>

            </div>

            <div class="item"><b>${message(code: 'default.status.label', default: 'Status')}:</b>  <semui:xEditableRefData
                    owner="${tipp}" field="status" config="TIPP Status"/></div>

            <div class="item"><b>${message(code: 'tipp.package', default: 'Package')}:</b>

                <div class="la-flexbox">
                    <i class="icon gift scale la-list-icon"></i>
                    <g:link controller="package" action="show"
                            id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
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
                            controller="platform" action="show" id="${tipp?.platform.id}"><i
                            class="pencil alternate icon"></i></g:link>
                </g:if>
                <g:if test="${tipp.hostPlatformURL}">
                    <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                       href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"
                       target="_blank"><i class="share square icon"></i></a>
                </g:if>
            </div>



            </div>
        </td>

            <td>
                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">

                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                    ${tipp?.title?.dateFirstInPrint}
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                    ${tipp?.title?.title?.dateFirstOnline}

                </g:if>
                <g:else>
                    <!-- von -->
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.startDate}"/><br/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.volume')}"></i>
                    ${tipp?.startVolume}<br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.issue')}"></i>
                    ${tipp?.startIssue}
                    <semui:dateDevider/>
                    <!-- bis -->
                    <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.endDate}"/><br/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.volume')}"></i>
                    ${tipp?.endVolume} <br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.issue')}"></i>
                    ${tipp?.endIssue}
                </g:else>
            </td>
            <td>
                <!-- von -->
                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                              date="${tipp.accessStartDate}"/>
                <semui:dateDevider/>
                <!-- bis -->
                <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessEndDate}"/>
            </td>
            <td>
                ${tipp.coverageDepth}
            </td>



            </td>
            <td>${tipp.embargo}</td>
            <td>${tipp.coverageNote}</td>
            <td>
                <g:link class="ui icon positive button" action="processAddEntitlements"
                        params="${[siid: subscriptionInstance.id, ('_bulkflag.' + tipp.id): 'Y']}"
                        data-tooltip="${message(code: 'subscription.details.addEntitlements.add_now', default: 'Add now')}">
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
                        params="${params + [identifiers: identifiers, pagination: true]}"
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
    function selectAll() {
      $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
    }

    $("simpleHiddenRefdata").editable({
        url: function(params) {
          var hidden_field_id = $(this).data('hidden-id');
          $("#"+hidden_field_id).val(params.value);
          // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
        }
      });
</r:script>

</body>
</html>
