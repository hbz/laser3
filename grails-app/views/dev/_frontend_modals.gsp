<h2 class="ui dividing header">Modale<a class="anchor" id="icons"></a></h2>
<h4 class="ui header">Mehrere Modale auf einer Seite-Values in Formularfeldern werden mit Data-Attributen übertrage</h4>
<div class="html ui top attached segment example">
    <div class="ui top attached label">Javascript in der View definiert die Data-Attribute</div>
    <pre aria-hidden="true">
        Javascript:

        JSPC.callbacks.modal.show.customerTypeModal = function(trigger) {
        $('#customerTypeModal #orgName_ct').attr('value', $(trigger).<strong>attr('data-orgName')</strong>)
    $('#customerTypeModal input[name=target]').attr('value', $(trigger).<strong>attr('data-ctTarget')</strong>)
    .
    .
    .</pre>
</div>
<div class="html ui top attached segment example">
    <div class="ui top attached label"></div>
    <pre aria-hidden="true">

        var customerType = $(trigger).attr('data-customerType')
        if (customerType) {
        $('#customerTypeModal select[name=customerType]').dropdown(<strong>'set selected'</strong>, customerType)
    } else {
    $('#customerTypeModal select[name=customerType]').dropdown('clear')
    }</pre>
</div>
