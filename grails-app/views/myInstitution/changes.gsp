<%@page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'myinst.pendingChanges.label')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.pendingChanges.label" class="active" />
        </semui:breadcrumbs>
        <br />
        <h1 class="ui icon header la-clear-before"><semui:headerIcon />
            ${message(code:'myinst.pendingChanges.label')}
            <%--${message(code:'myinst.todo.pagination', args:[(params.offset?:1), (java.lang.Math.min(num_todos,(params.int('offset')?:0)+10)), num_todos])}--%>
        </h1>

        <div class="la-float-right">
            <g:if test="${packages}">
                <g:form controller="pendingChange" action="processAll">
                    <g:select from="${packages}" noSelection="${['':message(code:'default.select.choose.label')]}" name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{it.id}}" optionValue="${{it.pkg.name}}"/>
                    <div class="ui buttons">
                        <g:submitButton class="ui button positive" name="acceptAll" value="${message(code:'pendingChange.takeAll')}"/>
                        <div class="or" data-text="${message(code:'default.or')}"></div>
                        <g:submitButton class="ui button negative" name="rejectAll" value="${message(code:'pendingChange.rejectAll')}"/>
                    </div>
                </g:form>
            </g:if>
        </div>

        <div class="ui internally celled grid">
            <div class="row">
                <div class="two wide column">
                    <g:message code="profile.dashboard.changes.eventtype"/>
                </div><!-- .column -->
                <div class="two wide column">
                    <g:message code="profile.dashboard.changes.objecttype"/>
                </div><!-- .column -->
                <div class="two wide column">
                    <g:message code="profile.dashboard.changes.object"/>
                </div><!-- .column -->
                <div class="seven wide column">
                    <g:message code="profile.dashboard.changes.event"/>
                </div><!-- .column -->
                <div class="three wide column">
                    <g:message code="profile.dashboard.changes.action"/>
                </div><!-- .column -->
            </div>
            <g:each in="${pending}" var="entry">
                <g:set var="row" value="${pendingChangeService.printRow(entry.change)}" />
                <g:set var="event" value="${row.eventData}"/>
                <div class="row">
                    <div class="two wide column">
                        ${raw(row.eventIcon)}
                    </div><!-- .column -->
                    <div class="two wide column">
                        ${raw(row.instanceIcon)}
                    </div><!-- .column -->
                    <div class="two wide column">
                        <g:if test="${entry.change.subscription}">
                            <g:link controller="subscription" action="index" id="${entry.target.id}">${entry.target.dropdownNamingConvention()}</g:link>
                        </g:if>
                        <g:elseif test="${entry.change.costItem}">
                            <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.target.sub.id]}">${entry.target.sub.dropdownNamingConvention()}</g:link>
                        </g:elseif>
                    </div><!-- .column -->
                    <div class="seven wide column">
                        ${raw(row.eventString)}

                        <g:if test="${entry.change.msgToken == "pendingChange.message_SU_NEW_01"}">
                            <div class="right aligned wide column">
                                <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(entry.change.subscription)]}">
                                    <g:message code="myinst.copyMyElements"/>
                                </g:link>
                            </div>
                        </g:if>
                    </div><!-- .column -->
                    <div class="three wide column">
                        <div class="ui buttons">
                            <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.change.id}"><g:message code="default.button.accept.label"/></g:link>
                            <div class="or" data-text="${message(code:'default.or')}"></div>
                            <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.change.id}"><g:message code="default.button.reject.label"/></g:link>
                        </div>
                    </div><!-- .column -->
                </div><!-- .row -->

            </g:each>
        </div><!-- .grid -->
        <div>
            <semui:paginate offset="${offset}" max="${max}" total="${pendingCount}"/>
        </div>

  </body>
</html>
