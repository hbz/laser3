%{--Run once data... can be reused for edit based functionality too. Pointless sending back this static data every request --}%
<%@page import="com.k_int.kbplus.RefdataCategory;de.laser.helper.RDConstants"%>
<g:set var="costItemStatus"   scope="request" value="${RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)}"/>
<g:set var="costItemCategory" scope="request" value="${RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_CATEGORY)}"/>
<g:set var="costItemElement"  scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:org])}"/>
<g:set var="taxType"          scope="request" value="${RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE)}"/>
<g:set var="yn"               scope="request" value="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"/>
<g:set var="currency"         scope="request" value="${com.k_int.kbplus.FinanceService.orderedCurrency()}"/>