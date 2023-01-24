<laser:htmlStart text="Frontend for Developers" serviceInjection="true" />

<br />
<br />

<h1 class="ui header center aligned">
    Reminder &amp; Playground
</h1>

<div class="ui segment">
    <p class="ui header">
        <i class="icon large kiwi bird"></i> simple color helper
    </p>
    <p>
        <i class="icon large stop red"></i> fomantic red <br/>
        <i class="icon large stop sc_red"></i> fallback <br/>

        <i class="icon large stop blue"></i> fomantic blue <br/>
        <i class="icon large stop sc_blue"></i> fallback <br/>

        <i class="icon large stop yellow"></i> fomantic yellow <br/>
        <i class="icon large stop sc_yellow"></i> fallback <br/>

        <i class="icon large stop green"></i> fomantic green <br/>
        <i class="icon large stop olive"></i> fomantic olive <br/>
        <i class="icon large stop sc_green"></i> fallback <br/>

        <i class="icon large stop orange"></i> fomantic orange <br/>
        <i class="icon large stop sc_orange"></i> fallback <br/>

        <i class="icon large stop sc_grey"></i> fallback <br/>

        <i class="icon large stop sc_darkgrey"></i> fallback <br/>
    </p>
</div>

<div class="ui segment">

    <select class="ui search selection dropdown" id="c3po-new"></select>

%{--    <div id="c3po-new" class="ui search selection dropdown">--}%
%{--        <input type="hidden" name="xxx" />--}%
%{--        <div class="text"></div>--}%
%{--        <i class="dropdown icon"></i>--}%
%{--        <div class="menu"></div>--}%
%{--    </div>--}%

    <script>
        $(function(){

            $.fn.dropdown.settings.message = {
                // addResult     : 'Add <b>{term}</b>',
                // count         : '{count} selected',
                // maxSelections : 'Max {maxCount} selections',
                noResults     : JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)
            };

            var ajaxurl = '/ajax/json/lookup'
            var desc = 'Organisation Property'
            var oid = 'Org:1'
            var baseClass = 'de.laser.properties.PropertyDefinition'
            var tenantId = ''

            console.log('+++')
            console.log(URL)
            console.log('+++')

            setTimeout( function() {
                $('#c3po-new').dropdown('destroy').dropdown({
                    apiSettings: {
                        url: ajaxurl + '?q={query}'+
                            (desc ? '&desc=' + desc : '') +
                            (oid ? '&oid=' + oid : '') +
                            (baseClass ? '&baseClass=' + baseClass : '') +
                            (tenantId ? '&tenant=' + tenantId : ''),

                        cache: false,

                        onResponse: function (response) {
                            // make some adjustments to response
                            console.log( 'onResponse' )
                            console.log( response )

                            return { succes: true, values: response.values };
                        },
                        // onComplete: function (response, element, xhr) {
                        //     // always called after XHR complete
                        //     console.log( 'onComplete' )
                        //     console.log( response )
                        // },
                        // onSuccess: function (response, element, xhr) {
                        //     // valid response and response.success = true
                        //     console.log( 'onSuccess' )
                        //     console.log( response )
                        // },
                        // onFailure: function (response, element, xhr) {
                        //     // request failed, or valid response but response.success = false
                        //     console.log( 'onFailure' )
                        //     console.log( response )
                        // },
                        // onError: function (errorMessage, element, xhr) {
                        //     // invalid response
                        //     console.log( 'onError' )
                        //     console.log( errorMessage )
                        // },
                        // onAbort: function (errorMessage, element, xhr) {
                        //     // navigated to a new page, CORS issue, or user canceled request
                        //     console.log( 'onAbort' )
                        //     console.log( errorMessage )
                        // }
                    },

                    filterRemoteData: true,
                    saveRemoteData: false,

                    fields: {
                        remoteValues : 'values', // mapping: grouping for api results
                        // values       : 'values', // mapping: grouping for all dropdown values
                        name         : 'text',   // mapping: displayed dropdown text
                        value        : 'id',     // mapping: actual dropdown value
                        text         : 'text'    // mapping: displayed text when selected
                    }
                })
            }, 500)
        })
    </script>
</div>

<laser:htmlEnd />
