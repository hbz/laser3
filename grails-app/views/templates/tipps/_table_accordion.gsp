<%@ page import="de.laser.PermanentTitle" %>
<g:set var="counter" value="${(offset ?: 0) + 1}"/>
<laser:serviceInjection/>

<g:set var="ptOwner" value="${institution ?: contextService.getOrg()}"/>

<g:if test="${tipps}">
    <div class="ui fluid card">
        <div class="content">
            <div class="ui accordion la-accordion-showMore">
                <g:each in="${tipps}" var="tipp">
                    <div class="ui raised segments la-accordion-segments">

                        <g:render template="/templates/title_segment_accordion"
                                  model="[ie: ie, tipp: tipp, permanentTitle: PermanentTitle.findByOwnerAndTipp(ptOwner, tipp)]"/>

                        <g:render template="/templates/title_content_segment_accordion" model="[ie: ie, tipp: tipp]"/>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</g:if>
