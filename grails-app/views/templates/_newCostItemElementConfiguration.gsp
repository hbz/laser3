<%@ page import="de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection/>
<ui:modal id="ciecModal" message="costItemElementConfiguration.create_new.label">

    <g:form class="ui form" url="${formUrl}" method="POST">

        <div class="ui grid">
            <%
                def signPreset = institution.costConfigurationPreset ? institution.costConfigurationPreset : null
            %>
            <div class="six wide column field">
                <label for="cie">${message(code:'financials.costItemElement')}</label>
                <ui:select class="ui dropdown la-full-width" id="cie" name="cie" from="${costItemElements}" optionKey="id" optionValue="value"/>
            </div>
            <div class="four wide column field">
                <label for="sign">${message(code:'financials.costItemConfiguration')}</label>
                <ui:select class="ui dropdown la-full-width" id="sign" name="sign" from="${elementSigns}" optionKey="id" optionValue="value" value="${signPreset}"/>
            </div>
            <div class="six wide column field">
                <label for="useForCostperUse">${message(code:'costConfiguration.useForCostItems')}</label>
                <g:checkBox name="useForCostPerUse"/>
            </div><!-- .row -->
        </div><!-- .grid -->
    </g:form>
</ui:modal>