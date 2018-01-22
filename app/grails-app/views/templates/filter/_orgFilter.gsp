<%@ page import="com.k_int.kbplus.*" %>
<% /*
<semui:filter>
    <g:form action="addMembers" method="get" class="ui form">
*/ %>

        <div class="fields">
            <div class="field">
                <label>${message(code: 'org.orgType.label')}</label>
                <g:select name="orgType" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                          from="${RefdataCategory.getAllRefdataValues('OrgType')}" value="${params.orgType}" optionKey="id" optionValue="value"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.sector.label')}</label>
                <g:select name="orgSector" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                          from="${RefdataCategory.getAllRefdataValues('OrgSector')}" value="${params.orgSector}" optionKey="id" optionValue="value"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.federalState.label')}</label>
                <g:select name="federalState" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                          from="${RefdataCategory.getAllRefdataValues('Federal State')}" value="${params.federalState}" optionKey="id" optionValue="value"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.libraryNetwork.label')}</label>
                <g:select name="libraryNetwork" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                          from="${RefdataCategory.getAllRefdataValues('Library Network')}" value="${params.libraryNetwork}" optionKey="id" optionValue="value"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.libraryType.label')}</label>
                <g:select name="libraryType" noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"
                          from="${RefdataCategory.getAllRefdataValues('Library Type')}" value="${params.libraryType}" optionKey="id" optionValue="value"/>
            </div>
        </div>
        <div class="field">
            <input type="submit" value="${message(code:'default.button.search.label')}" class="ui secondary button"/>
        </div>

<% /*
    </g:form>
</semui:filter>
*/ %>
