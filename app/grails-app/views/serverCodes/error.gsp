<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI"/>
		<title>${message(code:'laser', default:'LAS:eR')} - ${message(code:'serverCode.error.message1')}</title>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
	</head>

		<div class="ui grid">
			<div class="twelve wide column">

				<g:renderException exception="${exception}" />

			</div><!-- .twelve -->
		</div><!-- .grid -->

	</body>
</html>