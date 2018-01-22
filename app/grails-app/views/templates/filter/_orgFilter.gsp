<%@ page import="com.k_int.kbplus.*" %>
<% /*
<semui:filter>
    <g:form action="addMembers" method="get" class="ui form">
*/ %>

        <div class="fields">
            <div class="field">
                <label>${message(code: 'org.orgType.label')}</label>
                <laser:select name="orgType"
                              from="${RefdataCategory.getAllRefdataValues('OrgType')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.orgType}"
                              noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.sector.label')}</label>
                <laser:select name="orgSector"
                              from="${RefdataCategory.getAllRefdataValues('OrgSector')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.orgSector}"
                              noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.federalState.label')}</label>
                <laser:select name="federalState"
                              from="${RefdataCategory.getAllRefdataValues('Federal State')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.federalState}"
                              noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.libraryNetwork.label')}</label>
                <laser:select name="libraryNetwork"
                              from="${RefdataCategory.getAllRefdataValues('Library Network')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.libraryNetwork}"
                              noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'org.libraryType.label')}</label>
                <laser:select name="libraryType"
                              from="${RefdataCategory.getAllRefdataValues('Library Type')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.libraryType}"
                              noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
            </div>
        </div>
        <div class="field">
            <input type="submit" value="${message(code:'default.button.search.label')}" class="ui secondary button"/>
        </div>

<% /*
    </g:form>
</semui:filter>
*/ %>
