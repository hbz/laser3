<%@ page import="de.laser.storage.RDStore; de.laser.IssueEntitlement; de.laser.PermanentTitle" %>
<g:set var="counter" value="${(offset ?: 0) + 1}"/>
<laser:serviceInjection/>

<g:set var="ptOwner" value="${contextService.getOrg()}"/>

<g:if test="${tipps}">
    <div class="ui fluid card">
        <div class="content">
            <div class="ui accordion la-accordion-showMore la-js-showMoreCloseArea">
                <g:each in="${tipps}" var="tipp">
                    <div class="ui raised segments la-accordion-segments">

                        <g:render template="/templates/titles/title_segment_accordion"
                                  model="${[ie: null, tipp: tipp, permanentTitle: ptOwner ? PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and (pt.owner = :owner or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :owner and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))", [owner: ptOwner, tipp: tipp, subscriberCons: RDStore.OR_SUBSCRIBER_CONS])[0] : null]}"/>

                        <g:render template="/templates/titles/title_content_segment_accordion" model="[ie: null, tipp: tipp]"/>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/>
        <strong><g:message code="filter.result.empty.object" args="${[message(code: "title.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/>
        <strong><g:message code="result.empty.object" args="${[message(code: "title.plural")]}"/></strong>
    </g:else>
</g:else>
