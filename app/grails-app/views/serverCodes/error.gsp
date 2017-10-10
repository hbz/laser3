<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI"/>
		<title>${message(code:'laser', default:'LAS:eR')} - ${message(code:'serverCode.error.message1')}</title>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
	</head>
	<body>
        <div class="container">
            <h1>${message(code:'serverCode.error.message2')}</h1>
        </div>

        <div class="container">
		    <g:renderException exception="${exception}" />
        </div>
	</body>
</html>