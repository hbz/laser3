<%@ page import="de.laser.Subscription; de.laser.remote.ApiSource; grails.converters.JSON; de.laser.storage.RDStore; de.laser.Platform;de.laser.titles.BookInstance; de.laser.IssueEntitlementGroup;" %>

<laser:htmlStart message="subscription.details.addEntitlements.label" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}"/>
    <ui:crumb class="active"
                 text="${message(code: 'subscription.details.addEntitlements.label')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon floated="true">
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <laser:render template="iconSubscriptionIsChild"/>
</g:if>
<ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.addEntitlements.label')}</h2>
<%-- <laser:render template="nav"/> --%>
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <laser:render template="message"/>
</g:if>

<g:set var="counter" value="${offset + 1}"/>

<laser:render template="/templates/filter/tipp_ieFilter"/>

    <ui:messages data="${flash}"/>

<ui:form>
    <h3 class="ui dividing header"><g:message code="subscription.details.addEntitlements.header"/></h3>
    <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.warning" />

    <g:form class="ui form" controller="subscription" action="addEntitlements"
        params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscription.id]}"
        method="post" enctype="multipart/form-data">
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

        <div class="one fields">
            <div class="field">
                <div class="ui checkbox toggle">
                    <g:checkBox name="preselectValues" value="${preselectValues}" checked="true" readonly="readonly"/>
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

        </div>

        <div class="field la-field-right-aligned">
            <input type="submit"
                   value="${message(code: 'subscription.details.addEntitlements.preselect')}"
                   class="ui button"/>
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
</ui:form>
<br>
<br>

<ui:modal id="linkToIssueEntitlementGroup" message="issueEntitlementGroup.entitlementsRenew.selected.add"
          refreshModal="true"
          msgSave="${g.message(code: 'subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup')}">

    <g:form action="processAddEntitlements" class="ui form">
        <input type="hidden" name="id" value="${subscription.id}"/>
        <g:hiddenField name="preselectCoverageDates" value="${preselectCoverageDates}"/>
        <g:hiddenField name="uploadPriceInfo" value="${uploadPriceInfo}"/>
        <g:hiddenField name="process" value="withTitleGroup"/>

        <div class="ui three fields">

            <g:if test="${subscription.ieGroups}">
                <div class="field">
                    <label for="issueEntitlementGroup">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.add')}:</label>

                    <select name="issueEntitlementGroupID" id="issueEntitlementGroup"
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

            <div class="field"></div>

            <div class="field">
                <label for="issueEntitlementGroup">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}:</label>
                <input type="text" name="issueEntitlementGroupNew"
                       value="Phase ${IssueEntitlementGroup.findAllBySubAndNameIlike(subscription, 'Phase').size() + 1}">
            </div>

        </div>
    </g:form>
</ui:modal>

<g:form action="processAddEntitlements" class="ui form">
    <input type="hidden" name="id" value="${subscription.id}"/>
    <g:hiddenField name="preselectCoverageDates" value="${preselectCoverageDates}"/>
    <g:hiddenField name="uploadPriceInfo" value="${uploadPriceInfo}"/>

    <div class="field">
        <g:if test="${blockSubmit}">
            <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.thread.running" />
        </g:if>
        <a class="ui left floated button" id="processButton" data-ui="modal" href="#linkToIssueEntitlementGroup" ${blockSubmit ? 'disabled="disabled"' : '' }>
            ${checkedCount} <g:message code="subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup"/></a>

        <button type="submit" name="process" id="processButton2" value="withoutTitleGroup" ${blockSubmit ? 'disabled="disabled"' : '' } class="ui right floated button">
            ${checkedCount} ${message(code: 'subscription.details.addEntitlements.add_selected')}</button>
    </div>

    <div class="field"></div>

    <div class="ui blue large label"><g:message code="title.plural"/>: <div class="detail">${num_tipp_rows}</div>
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
                <laser:render template="/templates/title_short"
                          model="${[ie: null, tipp: tipp,
                                    showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                <!-- END TEMPLATE -->
            </td>

            <td>
                <g:if test="${(tipp.titleType == 'Book')}">
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.dateFirstInPrint')}"></i>
                   %{-- <ui:datepicker class="ieOverwrite" placeholder="${message(code: 'tipp.dateFirstInPrint')}" name="ieAccessStart" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstInPrint : tipp.title?.dateFirstInPrint}"/>
                    <%--${tipp.title.dateFirstInPrint}--%>--}%
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstInPrint}"/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
                    %{--<ui:datepicker class="ieOverwrite" placeholder="${message(code: 'tipp.dateFirstOnline')}" name="ieAccessEnd" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.dateFirstOnline : tipp.title?.dateFirstOnline}"/>
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
                        <ui:datepicker class="ieOverwrite coverage" name="startDate${key}" value="${covStmt.startDate}" placeholder="${message(code:'tipp.startDate')}"/>
                        <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.startDate}"/>--%><br />
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.startVolume')}"></i>
                        <input data-coverage="true" name="startVolume${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startVolume}" placeholder="${message(code: 'tipp.startVolume')}">
                        <%--${tipp?.startVolume}--%><br />
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.startIssue')}"></i>
                        <input data-coverage="true" name="startIssue${key}" type="text" class="ui input ieOverwrite" value="${covStmt.startIssue}" placeholder="${message(code: 'tipp.startIssue')}">
                        <%--${tipp?.startIssue}--%>
                        <ui:dateDevider/>
                        <!-- bis -->
                        <ui:datepicker class="ieOverwrite coverage" name="endDate${key}" value="${covStmt.endDate}" placeholder="${message(code:'tipp.endDate')}"/>
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
                        <input data-coverage="true" class="ieOverwrite" name="coverageNote${key}" type="text" placeholder="${message(code:'default.note.label')}" value="${covStmt.coverageNote}"><br />
                    </g:each>
                </g:else>
            </td>
                <td>
                    <!-- von -->
                    <ui:datepicker class="ieOverwrite" name="accessStartDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessStartDate : tipp.accessStartDate}" placeholder="${message(code:'tipp.accessStartDate')}"/>
                    <%--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.accessStartDate}"/>--%>
                    <ui:dateDevider/>
                    <!-- bis -->
                    <ui:datepicker class="ieOverwrite" name="accessEndDate" value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.accessEndDate : tipp.accessEndDate}" placeholder="${message(code:'tipp.accessEndDate')}"/>
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
                    <ui:datepicker class="ieOverwrite" name="priceDate" value="${issueEntitlementOverwrite[tipp.gokbId]?.priceDate}" placeholder="${message(code:'tipp.price.startDate')}"/>
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
                    <div class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.details.addEntitlements.thread.running')}">
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
        <div class="field">
            <g:if test="${blockSubmit}">
                <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.thread.running" />
            </g:if>
            <a class="ui left floated button" id="processButton3" data-ui="modal" href="#linkToIssueEntitlementGroup" ${blockSubmit ? 'disabled="disabled"' : '' }>
                ${checkedCount} <g:message code="subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup"/></a>

            <button type="submit" name="process" id="processButton4" value="withoutTitleGroup" ${blockSubmit ? 'disabled="disabled"' : '' } class="ui right floated button">
                ${checkedCount} ${message(code: 'subscription.details.addEntitlements.add_selected')}</button>
        </div>
    </div>


    <g:if test="${tipps}">
        <%
            params.remove("kbartPreselect")
        %>
        <ui:paginate controller="subscription"
                        action="addEntitlements"
                        params="${params + [pagination: true]}"
                        max="${max}"
                        total="${num_tipp_rows}"/>
    </g:if>

</g:form>




<laser:render template="/templates/export/individuallyExportTippsModal" model="[modalID: 'individuallyExportTippsModal']" />

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.selectAll = function () {
        $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
        JSPC.app.updateSelectionCache("all",$('#select-all').prop('checked'));
    }

    JSPC.app.updateSelectionCache = function (index,checked) {
        let filterParams = {
                    filter: "${params.filter}",
                    pkgFilter: "${params.pkgfilter}",
                    asAt: "${params.asAt}",
                    series_names: ${params.list("series_names")},
                    subject_references: ${params.list("subject_references")},
                    ddcs: ${params.list("ddcs")},
                    languages: ${params.list("languages")},
                    yearsFirstOnline: ${params.list("yearsFirstOnline")},
                    identifier: "${params.identifier}",
                    title_types: ${params.list("title_types")},
                    publishers: ${params.list("pulishers")},
                    coverageDepth: ${params.list("coverageDepth")},
                    hasPerpetualAccess: "${params.hasPerpetualAccess}"
        };
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateChecked" />",
            data: {
                sub: ${subscription.id},
                index: index,
                filterParams: JSON.stringify(filterParams),
                referer: "${actionName}",
                checked: checked
            },
            success: function (data) {
                $("#processButton").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup')}");
                $("#processButton2").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selected')}");
                 $("#processButton3").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup')}");
                  $("#processButton4").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selected')}");
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

<laser:htmlEnd />
