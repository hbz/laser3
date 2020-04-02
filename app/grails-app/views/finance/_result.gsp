<%@ page import="de.laser.helper.RDStore" %>
    <semui:messages data="${flash}" />
    <div id="filterTemplateWrapper" class="wrapper">
        <div id="filterTemplate">
            <g:render template="filter" model="[filterPresets:filterPresets,fixedSubscription:fixedSubscription]"/>
            <div id="financeFilterData" class="ui top attached tabular menu" data-current="${showView}">
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
                    <g:if test="${view in ['coll','collAtSubscr']}">
                        <div class="item" data-tab="coll">
                            <g:message code="financials.tab.collCosts"/>
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

                        <br>
                        <g:if test="${editable}">
                            <div class="field" style="text-align: right;">
                                <button id="bulkCostItems-toggle"
                                        class="ui button"><g:message code="financials.bulkCostItems.show"/></button>
                                <script>
                                    $('#bulkCostItems-toggle').on('click', function () {
                                        $('#bulkCostItems').toggleClass('hidden')
                                        if ($('#bulkCostItems').hasClass('hidden')) {
                                            $(this).text("${g.message(code: 'financials.bulkCostItems.show')}")
                                        } else {
                                            $(this).text("${g.message(code: 'financials.bulkCostItems.hidden')}")
                                        }
                                    })
                                </script>
                            </div>

                            <g:form action="processCostItemsBulk" name="costItemsBulk" method="post" class="ui form">
                                <div id="bulkCostItems" class="hidden">
                                    <g:render template="bulkCostItems"/>
                                </div>

                                <g:render template="result_tab_cons" model="[tmplShowCheckbox: true, fixedSubscription: fixedSubscription, editable: editable, data: cons, customerType: 'CONS', showView: view, offset: offsets.consOffset]"/>
                            </g:form>
                        </g:if>
                        <g:else>
                            <g:render template="result_tab_cons" model="[tmplShowCheckbox: false, fixedSubscription: fixedSubscription, editable: editable, data: cons, customerType: 'CONS', showView: view, offset: offsets.consOffset]"/>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${view in ['coll','collAtSubscr']}">
                    <div data-tab="coll" class="ui bottom attached tab">
                        <g:render template="result_tab_cons" model="[fixedSubscription: fixedSubscription, editable: editable, data: coll, customerType: 'COLL', showView: view, offset: offsets.collOffset]"/>
                    </div>
                </g:if>
                <g:if test="${view == 'subscr'}">
                    <div data-tab="subscr" class="ui bottom attached tab">
                        <g:render template="result_tab_subscr" model="[fixedSubscription: fixedSubscription, editable: editable, data:subscr, showView: view, offset: offsets.subscrOffset]"/>
                    </div>
                </g:if>
            </g:each>
        </div>

    </div>
    <r:script>
            $(document).ready(function() {
                var tab = "${showView}";
                var rawHref = $(".exportCSV").attr("href");
                var isClicked = false;
                $("[data-tab='"+tab+"']").addClass("active");
                $(".exportCSV").attr("href",rawHref+"&showView="+tab);

                $('#financeFilterData .item').tab({
                    onVisible: function(tabPath) {
                        $('#financeFilterData').attr('data-current', tabPath);
                        //console.log(tabPath);
                        $(".exportCSV").attr("href",rawHref+"&showView="+tabPath);
                    }
                });

                $('#btnAddNewCostItem').on('click', function(event) {
                    event.preventDefault();

                    // prevent 2 Clicks open 2 Modals
                    if (!isClicked) {
                        isClicked = true;
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty();
                        $.ajax({
                               url: "<g:createLink controller='finance' action='newCostItem'/>",
                               data: {
                                   sub: "${fixedSubscription?.id}",
                                   showView: "${showView}"
                               }
                        }).done(function (data) {
                            $('#dynamicModalContainer').html(data);
                            $('#dynamicModalContainer .ui.modal').modal({
                                   onVisible: function () {
                                       r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                       r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');
                                       ajaxPostFunc();
                                       setupCalendar();
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
                            isClicked = false;
                            }, 800);
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

                                ajaxPostFunc();
                                setupCalendar();
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