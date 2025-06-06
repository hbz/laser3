<laser:htmlStart message="menu.yoda.systemOddments" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb message="menu.yoda.systemOddments" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.systemOddments" type="yoda" />

    <div class="ui fluid card">
        <div class="content">
            <div class="header">Filter Chain</div>
        </div>
        <div class="content">
            <g:each in="${filters}" var="filter" status="f">
                ${f+1}. ${filter.getClass()} <br/>
            </g:each>
        </div>
    </div>

<laser:htmlEnd />
