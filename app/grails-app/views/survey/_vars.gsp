%{--Run once data... can be reused for edit based functionality too. Pointless sending back this static data every request --}%
<%@page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.CostItem;com.k_int.kbplus.Org"%>
<g:set var="costItemStatus"   scope="request" value="${RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.COST_ITEM_STATUS)}"/>
<g:set var="costItemCategory" scope="request" value="${RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.COST_ITEM_CATEGORY)}"/>
<g:set var="costItemElement"  scope="request" value="${RefdataValue.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:org])}"/>
<g:set var="taxType"          scope="request" value="${RefdataCategory.getAllRefdataValues('TaxType')}"/>
<g:set var="yn"               scope="request" value="${RefdataCategory.getAllRefdataValues('YN')}"/>
<g:set var="currency"         scope="request" value="${CostItem.orderedCurrency()}"/>