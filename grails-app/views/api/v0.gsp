<!doctype html>
<html>
<head>
    <meta charset="UTF-8">

    <title>${message(code:'laser')} - API</title>
    <style>
        html {
            box-sizing: border-box;
            overflow: -moz-scrollbars-vertical;
            overflow-y: scroll;
        }
        *,
        *:before, *:after {
            box-sizing: inherit;
        }
        body {
          margin:0;
          background: #fff;
        }
        #hmac-info {
            position: fixed;
            top: 0;
            width: 100%;
            z-index: 999;
            background-color: #fff;
            border-top: 1px solid #ccc;
            border-bottom: 1px solid #ccc;
        }
        #hmac-info pre {
            margin: 3em 0;
            font-size: 12px;
        }
        #main-container {
            margin-top: 100px;
        }
        #main-container .topbar {
            position: fixed;
            top: 0;
            width: 100%;
            padding: 0;
            background-color: rgba(0,0,0, 0.75);
            z-index: 99;
        }
        #main-container .topbar .topbar-wrapper {
            border-left: 1px solid #000;
            border-right: 1px solid #000;
        }
        #main-container .topbar .ui-box {
            font-family: "Courier New";
            font-size: 12px;
            padding: 5px 10px;
            color: #fff;
        }
        #main-container .topbar input {
            width: 200px;
            margin: 3px 0;
            padding: 8px 10px;
            border: none;
        }
        #main-container .topbar input[name=apiContext] {
            width: 250px;
        }
        #main-container .topbar input[name=apiAuth] {
            width: 470px;
        }
        #main-container .topbar .link,
        #main-container .topbar .download-url-wrapper {
            display: none;
        }
        #main-container .information-container pre.base-url + a {
            display: none;
        }
        #main-container textarea.curl {
            color: #666;
            background-color: #fff;
        }
        .display_none {
            display: none;
        }
    </style>

    <asset:stylesheet src="swagger.css"/><laser:javascript src="swagger.js"/>%{-- dont move --}%

    <tmpl:/layouts/favicon />
</head>

<body>
  <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" style="position:absolute;width:0;height:0">
    <defs>
      <symbol viewBox="0 0 20 20" id="unlocked">
            <path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V6h2v-.801C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8z"></path>
      </symbol>

      <symbol viewBox="0 0 20 20" id="locked">
        <path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8zM12 8H8V5.199C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="close">
        <path d="M14.348 14.849c-.469.469-1.229.469-1.697 0L10 11.819l-2.651 3.029c-.469.469-1.229.469-1.697 0-.469-.469-.469-1.229 0-1.697l2.758-3.15-2.759-3.152c-.469-.469-.469-1.228 0-1.697.469-.469 1.228-.469 1.697 0L10 8.183l2.651-3.031c.469-.469 1.228-.469 1.697 0 .469.469.469 1.229 0 1.697l-2.758 3.152 2.758 3.15c.469.469.469 1.229 0 1.698z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="large-arrow">
        <path d="M13.25 10L6.109 2.58c-.268-.27-.268-.707 0-.979.268-.27.701-.27.969 0l7.83 7.908c.268.271.268.709 0 .979l-7.83 7.908c-.268.271-.701.27-.969 0-.268-.269-.268-.707 0-.979L13.25 10z"/>
      </symbol>

      <symbol viewBox="0 0 20 20" id="large-arrow-down">
        <path d="M17.418 6.109c.272-.268.709-.268.979 0s.271.701 0 .969l-7.908 7.83c-.27.268-.707.268-.979 0l-7.908-7.83c-.27-.268-.27-.701 0-.969.271-.268.709-.268.979 0L10 13.25l7.418-7.141z"/>
      </symbol>


      <symbol viewBox="0 0 24 24" id="jump-to">
        <path d="M19 7v4H5.83l3.58-3.59L8 6l-6 6 6 6 1.41-1.41L5.83 13H21V7z"/>
      </symbol>

      <symbol viewBox="0 0 24 24" id="expand">
        <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/>
      </symbol>

    </defs>
  </svg>

<div id="hmac-info" class="display_none">
    <div class="swagger-ui">
        <div class="wrapper">
            <p>
                <strong>Example:</strong> <br />
                Runnable javascript implementation for HMAC generation.
                Just copy into your browser dev-tools.
            </p>
<pre>
var apiKey        = &quot;&lt;your apiKey&gt;&quot;
var apiPassword   = &quot;&lt;your apiPassword&gt;&quot;
var method        = &quot;GET&quot;
var path          = &quot;/api/v0/&lt;apiEndpoint&gt;&quot;
var timestamp     = &quot;&quot; // not used yet
var nounce        = &quot;&quot; // not used yet
var q             = &quot;&lt;q&gt;&quot;
var v             = &quot;&lt;v&gt;&quot;
var context       = &quot;&lt;context&gt;&quot;
var changedFrom   = &quot;&lt;changedFrom&gt;&quot;

var query         = $.grep( [(q ? &quot;q=&quot; + q : null), (v ? &quot;v=&quot; + v : null), (context ? &quot;context=&quot; + context : null), (changedFrom ? &quot;changedFrom=&quot; + changedFrom : null)], function(e, i){ return e }).join('&amp;')
var body          = &quot;&quot; // not used yet

var message       = method + path + timestamp + nounce + query + body
var digest        = CryptoJS.HmacSHA256(message, apiPassword)
var authorization = &quot;hmac &quot; + apiKey + &quot;:&quot; + timestamp + &quot;:&quot; + nounce + &quot;:&quot; + digest + &quot;,hmac-sha256&quot;

console.log('(debug only) message: ' + message)
console.log('(http-header) x-authorization: ' + authorization)
</pre>
            <p>
                <button class="btn">Close</button>
            </p>
        </div>
    </div>
</div>

    <div id="main-container"></div>

    <laser:script>

            var selectors = {
                query_q:       'tr[data-param-name="q"] input',
                query_v:       'tr[data-param-name="v"] input',
                query_changedFrom: 'tr[data-param-name="changedFrom"] input',
                query_context: 'tr[data-param-name="context"] input',

                top:      '.topbar-wrapper',
                top_key:  '.topbar-wrapper input[name=apiKey]',
                top_pass: '.topbar-wrapper input[name=apiPassword]',
                top_auth: '.topbar-wrapper input[name="apiAuth"]'
            }

            var jabba = SwaggerUIBundle({
                url: "${de.laser.utils.ConfigMapper.getGrailsServerURL()}/api/${apiVersion}/specs.yaml",
                dom_id: '#main-container',
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
            })

            window.jabba = jabba

            setTimeout(function(){
                jQuery(selectors.top).append('<span class="ui-box">Key <input name="apiKey" type="text" placeholder="Current API Key" value="${apiKey}"></span>')
                jQuery(selectors.top).append('<span class="ui-box">Pass <input name="apiPassword" type="password" placeholder="Current API Password" value="${apiPassword}"></span>')
                jQuery(selectors.top).append('<span class="ui-box">Context <input name="apiContext" type="text" placeholder="Current Context" value="${apiContext}"></span>')
                jQuery(selectors.top).append('<span class="ui-box">Authorization <input name="apiAuth" type="text" placeholder="Generate after the request is defined .." value=""></span>')

                jQuery('.information-container a[href=""]').on('click', function(event) {
                    event.preventDefault()
                    jQuery('#hmac-info').removeClass('display_none')
                })

                jQuery('#hmac-info button').on('click', function(event) {
                    jQuery('#hmac-info').addClass('display_none')
                })
            }, 750)

            setTimeout(function(){
                jQuery(selectors.top + ' input').on('focus', function(){
                    jQuery(this).select()
                })

                jQuery('.opblock-summary').append('<button name="generateApiAuth" class="btn">Generate Auth</button>')

                jQuery('button[name=generateApiAuth]').on('click', function(event) {
                    event.stopPropagation()

                    var div = jQuery(event.target).parents('.opblock').find('.parameters').first()
                    if (div.length) {
                        var auth = genDigist(div)
                        jQuery(selectors.top_auth).val(auth)
                    }
                })
            }, 2500)

            function genDigist(div) {
                var id        = jQuery(selectors.top_key).val().trim()
                var key       = jQuery(selectors.top_pass).val().trim()
                var method    = jQuery(div).parents('.opblock').find('.opblock-summary-method').text()
                var path      = "/api/${apiVersion}" + jQuery(div).parents('.opblock').find('.opblock-summary-path span').text()
                var timestamp = ""
                var nounce    = ""

                var context = jQuery(div).find(selectors.query_context)
                if (context.length) {
                    context = context.val().trim()
                }
                else {
                    context = ""
                }

                var query     = ""
                var body      = ""

                if(method == "GET") {
                    var q = jQuery(div).find(selectors.query_q)
                    var v = jQuery(div).find(selectors.query_v)
                    var changedFrom = jQuery(div).find(selectors.query_changedFrom)

                    q = (q.length && q.val().trim().length) ? "q=" + q.val().trim() : ''
                    v = (v.length && v.val().trim().length) ? "v=" + v.val().trim() : ''
                    changedFrom = (changedFrom.length && changedFrom.val().trim().length) ? "changedFrom=" + changedFrom.val().trim() : ''

                    query = q + ( q ? '&' : '') + v + (context ? (q || v ? '&' : '') + "context=" + context : '') + ( changedFrom ? (q || v || context ? '&' : '') : '' ) + changedFrom
                }
                else if(method == "POST") {
                    query = (context ? "&context=" + context : '')
                    body  = jQuery(div).find('.body-param > textarea').val().trim()
                }

                var algorithm     = "hmac-sha256"
                var digest        = CryptoJS.HmacSHA256(method + path + timestamp + nounce + query + body, key)
                var authorization = "hmac " + id + ":" + timestamp + ":" + nounce + ":" + digest + "," + algorithm

                /*if (debug) {
                    console.log('id:            ' + id)
                    console.log('key:           ' + key)
                    console.log('method:        ' + method)
                    console.log('path:          ' + path)
                    console.log('timestamp:     ' + timestamp)
                    console.log('nounce:        ' + nounce)
                    console.log('context:       ' + context)
                    console.log('query:         ' + query)
                    console.log('body:          ' + body)
                    console.log('algorithm:     ' + algorithm)
                    console.log('digest:        ' + digest)
                    console.log('authorization: ' + authorization)
                }*/
                return authorization
            }
    </laser:script>

    <laser:scriptBlock/>%{-- dont move --}%

</body>
</html>
