<laser:htmlStart message="menu.yoda.systemOddments" serviceInjection="true"/>

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb message="menu.yoda.systemOddments" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.systemOddments" type="yoda" />

<br />
<br />

<h2 class="ui header">Filter Chain</h2>

<g:each in="${filters}" var="filter" status="f">
    ${f+1}. ${filter.getClass()} <br/>
</g:each>

<laser:htmlEnd />
