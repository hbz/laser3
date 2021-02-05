<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.orgIdList}">

        <div class="ui message info">
            <p>
                ${result.orgIdList.size()} Einrichtungen
            </p>
        </div>

        <div class="ui styled fluid accordion">
            <g:if test="${result.orgIdList}">
                <div class="title">
                    <i class="dropdown icon"></i>${result.orgIdList.size()} Einrichtungen
                </div>
                <div class="content">
                    <p>
                        <g:each in="${result.orgIdList}" var="org" status="i">
                            <g:link controller="organisation" action="show" params="[id: org]"><i class="ui icon university"></i>${Org.get(org)}</g:link>
                            &nbsp;
                        </g:each>
                    </p>
                </div>
            </g:if>
        </div>

    </g:if>
    <g:else>
        <div class="ui message info">
            Keine Treffer ..
        </div>
    </g:else>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
</laser:script>