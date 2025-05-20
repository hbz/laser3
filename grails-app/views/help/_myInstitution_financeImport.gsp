<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

    <h1 class="ui header">
        <g:message code="myinst.financeImport.headline"/>
    </h1>
    <ui:msg class="warning" message="myinst.financeImport.mandatory" hideClose="true" showIcon="true"/>
    <div class="content">
        <table class="ui la-ignore-fixed compact table">
            <thead>
                <tr>
                    <%-- <th>tsv column name</th>
                    <th>Description</th>
                    <th>maps to</th> --%>
                    <th>${message(code:'myinst.financeImport.tsvColumnName')}</th>
                    <%--<th>${message(code:'myinst.financeImport.descriptionColumnName')}</th>--%>
                    <th>${message(code:'myinst.financeImport.necessaryFormat')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${mappingCols}" var="mpg">
                    <%
                        List args = []
                        boolean mandatory = false
                        switch(mpg) {
                            case 'status': List<RefdataValue> costItemStatus = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)
                                costItemStatus.remove(RDStore.COST_ITEM_DELETED)
                                args.addAll(costItemStatus.collect { it -> it.getI10n('value') })
                                break
                            case 'currency': mandatory = true
                                break
                            case 'element': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT).collect { it -> it.getI10n('value') })
                                break
                            case 'elementSign': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION).collect { it -> it.getI10n('value') })
                                break
                            case 'taxType': List<RefdataValue> taxTypes = RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE)
                                args.addAll(taxTypes.collect { it -> it.getI10n('value') })
                                break
                            case 'taxRate': args.addAll([0,5,7,16,19])
                                break
                        }
                    %>
                    <tr <g:if test="${mandatory}">class="negative"</g:if>>
                        <td>${message(code:"myinst.financeImport.${mpg}")}</td>
                        <%--<td>${message(code:"myinst.financeImport.description.${mpg}") ?: ''}</td>--%>
                        <td>
                            ${message(code:"myinst.financeImport.format.${mpg}")}
                            <g:if test="${args}">
                                <ul>
                                    <g:each in="${args}" var="arg">
                                        <li>${arg}</li>
                                    </g:each>
                                </ul>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
