<%@ page import="de.laser.storage.RDStore; de.laser.RefdataValue" %>
<ui:modal id="ciecModal" message="costItemElementConfiguration.create_new.label">

    <g:form class="ui form" url="${formUrl}" method="POST">

        <div class="ui grid">
            <%
                def signPreset = institution.costConfigurationPreset ? institution.costConfigurationPreset : null
            %>
            <div class="eight wide column">
                <label for="cie">${message(code:'financials.costItemElement')}</label>
                <g:select class="ui dropdown la-full-width" id="cie" name="cie" from="${costItemElements}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}"/>
            </div>
            <div class="eight wide column">
                <label for="sign">${message(code:'financials.costItemConfiguration')}</label>
                <g:select class="ui dropdown la-full-width" id="sign" name="sign" from="${elementSigns}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}" value="${signPreset}"/>
            </div><!-- .row -->
        </div><!-- .grid -->
    </g:form>
</ui:modal>