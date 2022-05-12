<%@ page import="de.laser.finance.CostItemGroup; de.laser.finance.BudgetCode"%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'menu.institutions.budgetCodes')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.budgetCodes" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.institutions.budgetCodes')}</h1>

        <semui:messages data="${flash}" />

        <g:if test="${editable}">
            <div class="content ui form ui left floated  la-clear-before">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" data-href="#addBudgetCodeModal" data-semui="modal">${message(code:'budgetCode.create_new.label')}</button>
                    </div>
                </div>
            </div>
        </g:if>

    <table class="ui celled sortable table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <th>${message(code: 'financials.budgetCode')}</th>
                <th>${message(code: 'financials.budgetCode.description')}</th>
                <th>
                    ${message(code: 'financials.budgetCode.usage')}
                    <span data-position="right center" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'financials.budgetCode.usage.explanation')}">
                        <i class="question circle icon"></i>
                    </span>
                </th>
                <g:if test="${editable}">
                    <th class="la-action-info one wide">${message(code:'default.actions.label')}</th>
                </g:if>
            </tr>
        </thead>
        <tbody>
            <g:each in="${budgetCodes}" var="bcode">
                <tr>
                    <td>
                        <semui:xEditable owner="${bcode}" field="value" />
                    </td>
                    <td>
                        <semui:xEditable owner="${bcode}" field="descr" />
                    </td>
                    <td>
                        <div class="ui list">
                            <g:each in="${costItemGroups.get(bcode)}" var="cig">

                                <div class="item">
                                    <g:if test="${cig.costItem.sub}">
                                        ${cig.costItem.sub.name}
                                    </g:if>

                                    <g:if test="${cig.costItem.costTitle}">
                                        - ${cig.costItem.costTitle}
                                    </g:if>
                                    <g:elseif test="${cig.costItem.costTitle}">
                                        - ${cig.costItem.globalUID}
                                    </g:elseif>

                                    <g:if test="${cig.costItem.costDescription}">
                                        (${cig.costItem.costDescription})
                                    </g:if>

                                    <g:if test="${cig.costItem.sub}">
                                        <g:link mapping="subfinance" class="ui button icon blue la-modern-button"
                                                params="[sub:cig.costItem.sub.id, filterCIBudgetCode: bcode.id, submit: message(code:'default.filter.label')]">
                                            <i class="share icon"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <g:link controller="finance" action="index" class="ui button icon blue la-modern-button"
                                                params="[filterCIBudgetCode: bcode.id, submit: message(code:'default.filter.label')]">
                                            <i class="share icon"></i>
                                        </g:link>
                                    </g:else>
                                </div>
                            </g:each>
                        </div>
                    </td>
                    <g:if test="${editable}">
                        <td class="x">
                            <g:if test="${!costItemGroups.get(bcode)}">
                                <g:link controller="myInstitution"
                                        action="budgetCodes"
                                        params="${[cmd: 'deleteBudgetCode', bc: BudgetCode.class.name + ':' + bcode.id]}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.budgetcode", args: [fieldValue(bean: bcode, field: "value")])}"
                                        data-confirm-term-how="delete"
                                        class="ui icon negative button la-modern-button js-open-confirm-modal"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </g:link>
                            </g:if>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </tbody>
    </table>


    <semui:modal id="addBudgetCodeModal" message="budgetCode.create_new.label">

        <g:form class="ui form" url="[controller: 'myInstitution', action: 'budgetCodes']" method="POST">
            <input type="hidden" name="cmd" value="newBudgetCode"/>

            <div class="field">
                <label for="bc">Budgetcode</label>
                <input type="text" id="bc" name="bc"/>
            </div>

            <div class="field">
                <label for="descr">Beschreibung</label>
                <textarea id="descr" name="descr"></textarea>
            </div>

        </g:form>
    </semui:modal>

  </body>
</html>
