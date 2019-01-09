<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue" %>
<semui:modal id="ciecModal" message="costItemElementConfiguration.create_new.label">

    <g:form class="ui form" url="${formUrl}" method="POST">

        <div class="ui grid">
            <%
                def signPreset = institution.costConfigurationPreset ? institution.costConfigurationPreset : RDStore.CIEC_POSITIVE
                def considerationPreset = institution.considerationPreset ? institution.considerationPreset : RDStore.YN_YES
            %>
            <div class="twelve wide column">
                <label>${message(code:'financials.costItemElement')}</label>
                <g:select name="cie" from="${costItemElements}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}"/>
            </div>
            <div class="four wide column">
                <label>${message(code:'financials.costItemConfiguration')}</label>
                <g:select name="sign" from="${elementSigns}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}" value="${signPreset}"/>
            </div><!-- .row -->
        </div><!-- .grid -->
    </g:form>
</semui:modal>