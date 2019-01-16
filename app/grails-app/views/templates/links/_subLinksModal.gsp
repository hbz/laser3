<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Subscription;com.k_int.kbplus.RefdataValue" %>
<g:if test="${editmode}">
    <a class="ui button" data-semui="modal" href="#${tmplModalID}">${tmplButtonText}</a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}">
    <g:form id="link_subs" class="ui form" url="[controller: 'ajax', action: 'linkSubscriptions']" method="post">
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <div class="row">
                    <div class="column">
                        Diese Lizenz mit einer anderen Lizenz verknüpfen
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        Diese Lizenz
                    </div>
                    <div class="twelve wide column">
                        <g:set var="linkTypes" value="${RefdataValue.findAllByOwner(RefdataCategory.getByI10nDesc('Link type'))}"/>
                        <g:select name="linkType" from="${linkTypes}" optionKey="${it}" optionValue="${it.getI10n('value').split('|')[0]}" />
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        Lizenz
                    </div>
                    <div class="twelve wide column">

                    </div>
                </div>
                <div class="row">
                    <div class="column">
                        Kommentar
                    </div>
                    <div class="two column">

                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:modal>