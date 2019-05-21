<%@ page import="de.laser.helper.RDStore" %>

    <semui:messages data="${flash}" />

    <g:if test="${editable}">
        <button class="ui button" value="" data-href="#addBudgetCodeModal" data-semui="modal">${message(code:'budgetCode.create_new.label')}</button>

        <semui:modal id="addBudgetCodeModal" message="budgetCode.create_new.label">

            <g:form class="ui form" url="[controller: 'myInstitution', action: 'budgetCodes']" method="POST">
                <input type="hidden" name="cmd" value="newBudgetCode"/>
                <input type="hidden" name="redirect" value="redirect"/>

                <div class="field">
                    <label>${message(code:'financials.budgetCode.description')}</label>
                    <input type="text" name="bc"/>
                </div>

                <div class="field">
                    <label>${message(code:'financials.budgetCode.usage')}</label>
                    <textarea name="descr"></textarea>
                </div>

            </g:form>
        </semui:modal>

    </g:if>


    <div class="ui grid">
        <div class="column">
            <%--<button class="ui button" type="submit" data-semui="modal" href="#recentlyAdded_modal" id="showHideRecent">${message(code:'financials.recentCosts')}</button>--%>

            <g:if test="${editable}">
                <%--<button class="ui button pull-right" type="submit" id="BatchSelectedBtn" title="${g.message(code: 'financials.filtersearch.deleteAll')}" value="remove">Remove Selected</button>--%>

                <script>
                    var isClicked = false;
                    $('#btnAddNewCostItem').on('click', function(event) {
                        event.preventDefault();

                        // prevent 2 Clicks open 2 Modals
                        if (!isClicked) {
                            isClicked = true;
                            $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                            $('#dynamicModalContainer').empty()

                            $.ajax({
                                url: "<g:createLink controller='finance' action='editCostItem'/>",
                                data: {
                                    sub: "${fixedSubscription?.id}",
                                    tab: "${showView}"
                                }
                            }).done(function (data) {
                                $('#dynamicModalContainer').html(data);

                                $('#dynamicModalContainer .ui.modal').modal({
                                    onVisible: function () {
                                        r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                        r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                                        ajaxPostFunc()
                                    },
                                    detachable: true,
                                    closable: false,
                                    transition: 'scale',
                                    onApprove: function () {
                                        $(this).find('.ui.form').submit();
                                        return false;
                                    }
                                }).modal('show');
                            })
                            setTimeout(function () {
                                isClicked = false;
                            }, 800);
                        }
                    })
                </script>

            </g:if>
        </div>
    </div>

        <%--<g:render template="recentlyAddedModal" />--%>

        <div class="ui grid">
            <div class="sixteen wide column">
                <div id="filterTemplateWrapper" class="wrapper">
                    <div id="filterTemplate">
                        <%--${financialData}--%>
                        <g:render template="filter" model="[filterPreset:filterPresets,fixedSubscription:fixedSubscription]"/>
                        <div id="financeFilterData" class="ui top attached tabular menu" data-current="${showView}">
                            <g:if test="${(!showView.equals("consAtSubscr") || showView.equals("own")) && accessService.checkPermAffiliation("ORG_BASIC,ORG_CONSORTIUM","INST_USER")}">
                                <div class="item" data-tab="own">${message(code:'financials.tab.ownCosts')}</div>
                            </g:if>
                            <g:if test="${showView.equals("cons") || showView.equals("consAtSubscr")}">
                                <div class="item" data-tab="cons">${message(code:'financials.tab.consCosts')}</div>
                            </g:if>
                            <g:if test="${showView.equals("subscr")}">
                                <div class="item" data-tab="subscr">${message(code:'financials.tab.subscrCosts')}</div>
                            </g:if>
                        </div>
                        <g:if test="${(!showView.equals("consAtSubscr") || showView.equals("own")) && accessService.checkPermAffiliation("ORG_BASIC,ORG_CONSORTIUM","INST_USER")}">
                            <!-- OWNER -->
                            <div data-tab="own" class="ui bottom attached tab">
                                <br />
                                <g:render template="result_tab_owner" model="[fixedSubscription: fixedSubscription, editable: editable, data: own]"/>
                            </div><!-- OWNER -->
                        </g:if>
                        <g:if test="${showView.equals("cons") || showView.equals("consAtSubscr")}">
                            <div data-tab="cons" class="ui bottom attached tab">
                                <br />
                                <g:render template="result_tab_cons" model="[fixedSubscription: fixedSubscription, editable: editable, data: cons, orgRoles: financialData.consSubscribers]"/>
                            </div>
                        </g:if>
                        <g:if test="${showView.equals("subscr")}">
                            <div data-tab="subscr" class="ui bottom attached tab">
                                <br />
                                <g:render template="result_tab_subscr" model="[fixedSubscription: fixedSubscription, editable: editable, data:subscr]"/>
                            </div>
                        </g:if>
                    </div>
                </div>
            </div><!-- .sixteen -->
        </div><!-- .grid -->

        <r:script>
            $(document).ready(function() {
                var tab = "${view}";
                var rawHref = $(".exportCSV").attr("href");
                $("[data-tab='"+tab+"']").addClass("active");
                $(".exportCSV").attr("href",rawHref+"&showView="+tab);
                if(tab === "consAtSubscr")
                    $("[data-tab='cons']").addClass("active");

                $('#financeFilterData .item').tab({
                    onVisible: function(tabPath) {
                        $('#financeFilterData').attr('data-current', tabPath);
                        //console.log(tabPath);
                        $(".exportCSV").attr("href",rawHref+"&showView="+tabPath);
                    }
                });

                $('table[id^=costTable] .x .trigger-modal').on('click', function(e) {
                    e.preventDefault();

                    $.ajax({
                        url: $(this).attr('href')
                    }).done( function(data) {
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty().html(data);

                        $('#dynamicModalContainer .ui.modal').modal({
                            onVisible: function () {
                                r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                                ajaxPostFunc()
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
            });
        </r:script>