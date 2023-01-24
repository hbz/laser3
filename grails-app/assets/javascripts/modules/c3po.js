// module: assets/javascripts/modules/c3po.js

c3po = {

    PROP_SEARCH_NATIVE: 'PROP_SEARCH_NATIVE',
    PROP_SEARCH_GROUPED: 'PROP_SEARCH_GROUPED',

    initProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log ('c3po.initProperties( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_NATIVE, ajaxurl, cssId, tenantId)
        c3po.showHideRefData(cssId)
    },

    initGroupedProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log ('c3po.initGroupedProperties( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_GROUPED, ajaxurl, cssId, tenantId)
        c3po.showHideRefData(cssId)
    },

    refdataCatSearch: function (ajaxurl, cssId) {
        console.log ('c3po.refdataCatSearch() ' + ajaxurl + ', ' + cssId + ' )')

        $("#remoteRefdataSearch").select2({
            placeholder: "Kategorie angeben ..",
            language: JSPC.vars.locale,
            minimumInputLength: 1,
            allowClear: true,
            // formatInputTooShort: function () { return JSPC.dict.get('select2.minChars.note', JSPC.currLanguage); },
            // formatNoMatches:     function () { return JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage); },
            // formatSearching:     function () { return JSPC.dict.get('select2.formatSearching', JSPC.currLanguage); },

            ajax: {
                url: ajaxurl,
                dataType: 'json',
                data: function (p) {
                    return {
                        q: p.term || '', // search term
                        page_limit: 10,
                        baseClass: 'de.laser.RefdataCategory'
                    };
                },
                processResults: function (data) {
                    return { results: data.values };
                }
            }
        });
    },

    searchProp: function (grouped, ajaxurl, cssId, tenantId) {
        console.log ('c3po.searchProp( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        let $select = $(cssId + " .remotePropertySearch")
        let desc    = $select.find('select').attr('data-desc')
        let oid     = $select.find('select').attr('data-oid')

        let baseClass = (grouped === c3po.PROP_SEARCH_GROUPED) ? 'de.laser.properties.PropertyDefinitionGroup' : 'de.laser.properties.PropertyDefinition'
        let appender  = ajaxurl.indexOf('?') < 0 ? '?' : '&'

        $select.dropdown('destroy').dropdown({
            apiSettings: {
                url: ajaxurl + appender + 'q={query}'+
                    (oid ? '&oid=' + oid : '') +
                    (baseClass ? '&baseClass=' + baseClass : '') +
                    (desc ? '&desc=' + desc : '') +
                    (tenantId ? '&tenant=' + tenantId : ''),

                cache: false,

                onResponse: function (response) {
                    // make some adjustments to response
                    console.log( 'onResponse' )
                    console.log( response )

                    return { succes: true, values: response.values };
                }
            },

            filterRemoteData: true,
            saveRemoteData: false,
            duration: 50,

            fields: {
                remoteValues : 'values', // mapping: grouping for api results
                // values       : 'values', // mapping: grouping for all dropdown values
                name         : 'text',   // mapping: displayed dropdown text
                value        : 'id',     // mapping: actual dropdown value
                text         : 'text'    // mapping: displayed text when selected
            }
        })
    },

    // TODO -refactoring
    showHideRefData: function (cssId) {
        console.log ('c3po.showHideRefData( ' + cssId + ' )')

        $('#pd_type').change(function () {
            if ($("#pd_type option:selected").val() === "de.laser.RefdataValue") {
                $("#remoteRefdataSearchWrapper").show();
            } else {
                $("#remoteRefdataSearchWrapper").hide();
            }
        });
    }
}

JSPC.modules.add( 'c3po', c3po );
