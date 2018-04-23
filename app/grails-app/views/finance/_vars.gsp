%{--Run once data... can be reused for edit based functionality too. Pointless sending back this static data every request --}%

<g:set var="costItemStatus"   scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','CostItemStatus')}"/>
<g:set var="costItemCategory" scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','CostItemCategory')}"/>
<g:set var="costItemElement"  scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','CostItemElement')}"/>
<g:set var="taxType"          scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','TaxType')}"/>
<g:set var="yn"               scope="request" value="${com.k_int.kbplus.RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?','YN')}"/>
<g:set var="currency"         scope="request" value="${com.k_int.kbplus.CostItem.orderedCurrency()}"/>