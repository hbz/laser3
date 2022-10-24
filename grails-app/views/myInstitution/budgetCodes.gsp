<%@ page import="de.laser.finance.CostItemGroup; de.laser.finance.BudgetCode"%>

<laser:htmlStart message="menu.institutions.budgetCodes" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.institutions.budgetCodes" class="active"/>
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="menu.institutions.budgetCodes" />

        <ui:messages data="${flash}" />

        <g:if test="${editable}">
            <div class="content ui form ui left floated  la-clear-before">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" data-href="#addBudgetCodeModal" data-ui="modal">${message(code:'budgetCode.create_new.label')}</button>
                    </div>
                </div>
            </div>
        </g:if>

    <table class="ui celled sortable table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <th>${message(code: 'financials.budgetCode')}</th>
                <th>${message(code: 'default.description.label')}</th>
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
                        <ui:xEditable owner="${bcode}" field="value" />
                    </td>
                    <td>
                        <ui:xEditable owner="${bcode}" field="descr" />
                    </td>
                    <td>
                        <g:link controller="finance" action="index"
                                params="[filterCIBudgetCode: bcode.id, submit: message(code:'default.filter.label')]">
                            <div class="ui blue circular label">
                                ${costItemGroups.get(bcode) ?: 0}
                            </div>
                        </g:link>
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


    <ui:modal id="addBudgetCodeModal" message="budgetCode.create_new.label">

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
    </ui:modal>

<laser:htmlEnd />
