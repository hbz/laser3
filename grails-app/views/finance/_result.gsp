<%@ page import="de.laser.helper.RDStore" %>
    <semui:messages data="${flash}" />
    <div id="filterTemplateWrapper" class="wrapper">
        <div id="filterTemplate">
            <g:render template="filter" model="[filterPresets:filterPresets,fixedSubscription:fixedSubscription,showView:showView,ciTitles:ciTitles]"/>
            <div id="financeFilterData" class="ui top attached tabular la-tab-with-js menu" data-current="${showView}">
                <g:each in="${dataToDisplay}" var="view">
                    <g:if test="${view == 'own'}">
                        <div class="item" data-tab="own">
                            <g:message code="financials.tab.ownCosts"/>
                        </div>
                    </g:if>
                    <g:if test="${view in ['cons','consAtSubscr']}">
                        <div class="item" data-tab="cons">
                            <g:message code="financials.tab.consCosts"/>
                        </div>
                    </g:if>
                    <g:if test="${view == 'subscr'}">
                        <div class="item" data-tab="subscr">
                            <g:message code="financials.tab.subscrCosts"/>
                        </div>
                    </g:if>
                </g:each>
            </div>



            <g:each in="${dataToDisplay}" var="view">
                <g:if test="${view == 'own'}">
                    <div data-tab="own" class="ui bottom attached tab">
                        <g:render template="result_tab_owner" model="[fixedSubscription: fixedSubscription, editable: editable, data: own, customerType: 'OWNER', showView: view, offset: offsets.ownOffset]"/>
                    </div>
                </g:if>
                <g:if test="${view in ['cons','consAtSubscr']}">
                    <div data-tab="cons" class="ui bottom attached tab">

                        <br />
                        <g:if test="${editable}">
                            <div class="field" style="text-align: right;">
                                <button id="bulkCostItems-toggle"
                                        class="ui button"><g:message code="financials.bulkCostItems.show"/></button>
                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('#bulkCostItems-toggle').on('click', function () {
                                        $('#bulkCostItems').toggleClass('hidden')
                                        if ($('#bulkCostItems').hasClass('hidden')) {
                                            $(this).text("${g.message(code: 'financials.bulkCostItems.show')}")
                                        } else {
                                            $(this).text("${g.message(code: 'financials.bulkCostItems.hidden')}")
                                        }
                                    })
                                </laser:script>
                            </div>

                            <g:form action="processCostItemsBulk" name="editCost_${idSuffix}" method="post" class="ui form">
                                <div id="bulkCostItems" class="hidden">
                                    <g:render template="costItemInput" />
                                    <div class="ui horizontal divider"><g:message code="search.advancedSearch.option.OR"/></div>
                                    <div class="fields la-forms-grid">
                                        <fieldset class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                                            <div class="field center aligned">

                                                <label>${message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}</label>
                                                <div class="ui right labeled input">
                                                    <input type="number"
                                                           name="percentOnOldPrice"
                                                            id="percentOnOldPrice"
                                                           placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}"
                                                           value="" step="0.01"/>
                                                    <div class="ui basic label">%</div>
                                                </div>
                                            </div>
                                        </fieldset>
                                    </div>
                                    <div class="two fields">
                                        <div class="eight wide field" style="text-align: left;">
                                            <button class="ui button" type="submit">${message(code: 'financials.bulkCostItems.submit')}</button>
                                        </div>

                                        <div class="eight wide field" style="text-align: right;">
                                        </div>
                                    </div>
                                </div>

                                <div class="field la-field-right-aligned">
                                    <input name="delete" type="hidden" value="false"/>
                                    <button type="submit" id="deleteButton" class="ui negative button js-open-confirm-modal" role="button"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItem.bulk")}"
                                            data-confirm-term-how="delete">${message(code: 'financials.bulkCostItems.delete')}</button>
                                </div>
                                <g:render template="result_tab_cons" model="[tmplShowCheckbox: true, fixedSubscription: fixedSubscription, editable: editable, data: cons, customerType: 'CONS', showView: view, offset: offsets.consOffset]"/>
                            </g:form>
                        </g:if>
                        <g:else>
                            <g:render template="result_tab_cons" model="[tmplShowCheckbox: false, fixedSubscription: fixedSubscription, editable: editable, data: cons, customerType: 'CONS', showView: view, offset: offsets.consOffset]"/>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${view == 'subscr'}">
                    <div data-tab="subscr" class="ui bottom attached tab">
                        <g:render template="result_tab_cons" model="[tmplShowCheckbox: false, fixedSubscription: fixedSubscription, editable: false, data:subscr, showView: view, offset: offsets.subscrOffset]"/>
                    </div>
                </g:if>
            </g:each>
        </div>

    </div>
    <laser:script file="${this.getGroovyPageFileName()}">
                <g:if test="${showView == 'consAtSubscr'}">
                    JSPC.app.tab = "cons";
                </g:if>
                <g:else>
                    JSPC.app.tab = "${showView}";
                </g:else>
                JSPC.app.rawHref = $(".exportCSV").attr("href");
                JSPC.app.isClicked = false;

                $("[data-tab='" + JSPC.app.tab + "']").addClass("active");
                $(".exportCSV").attr("href", JSPC.app.rawHref + "&showView=" + JSPC.app.tab);

                $('#financeFilterData .item').tab({
                    onVisible: function(tabPath) {
                        $('#financeFilterData').attr('data-current', tabPath);
                        //console.log(tabPath);
                        $(".exportCSV").attr("href", JSPC.app.rawHref + "&showView=" + tabPath);
                        $("#showView").val(tabPath);
                    }
                });

                $('#btnAddNewCostItem').on('click', function(event) {
                    event.preventDefault();

                    // prevent 2 Clicks open 2 Modals
                    if (! JSPC.app.isClicked) {
                        JSPC.app.isClicked = true;
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty();
                        let preselectedSubscriptions = []
                        for(let i = 0;i < $("[name='selectedCostItems']:checked").length;i++) {
                            preselectedSubscriptions.push($("[name='selectedCostItems']:checked").get(i).value);
                        }
                        let idSuffix = "new";
                        $.ajax({
                            url: "<g:createLink controller='finance' action='newCostItem'/>",
                            data: {
                                sub: "${fixedSubscription?.id}",
                                showView: "${showView}",
                                preselectedSubscriptions: JSON.stringify(preselectedSubscriptions)
                            }
                        }).done(function (data) {
                            $('#dynamicModalContainer').html(data);
                            $('#dynamicModalContainer .ui.modal').modal({
                                onVisible: function () {
                                    r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                    r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');
                                    JSPC.app['finance'+idSuffix].updateTitleDropdowns();
                                    JSPC.app.setupCalendar();
                                    JSPC.app['finance'+idSuffix].preselectMembers();
                                },
                                detachable: true,
                                closable: false,
                                transition: 'scale',
                                onApprove: function () {
                                    $(this).find('.ui.form').submit();
                                    return false;
                                }
                            }).modal('show');
                        });
                        setTimeout(function () {
                            JSPC.app.isClicked = false;
                            }, 800);
                    }
                });

                $('#js-confirmation-button').on('click', function(e) {
                    e.preventDefault();
                    $('[name="delete"]').val('true');
                    $('#editCost_${idSuffix}').unbind('submit').submit();
                });

                $('table[id^=costTable] .x .trigger-modal').on('click', function(e) {
                    e.preventDefault();
                    let idSuffix = $(this).attr("data-id_suffix");
                    $.ajax({
                        url: $(this).attr('href')
                    }).done( function(data) {
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty().html(data);

                        $('#dynamicModalContainer .ui.modal').modal({
                            onVisible: function () {
                                r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                                JSPC.app['finance'+idSuffix].updateTitleDropdowns();
                                JSPC.app.setupCalendar();
                            },
                            detachable: true,
                            closable: false,
                            transition: 'scale',
                            onApprove : function() {
                                $(this).find('.ui.form').submit();
                                return false;
                            }
                        }).modal('show');
                    })
                });
    </laser:script>