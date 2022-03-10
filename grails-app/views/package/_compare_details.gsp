
<div class="modal hide" id="compare_details${currentTitle.id}">
	<div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3 class="ui header">Further details</h3>
    </div>

    <div class="modal-body">
    	<table class="ui celled la-js-responsive-table la-table table">
	    	<thead>
	    		<tr>
	    			<th>Attribute</th>
	    			<th> ${pkgAName} </th>
	    			<th> ${pkgBName} </th>
	    		</tr>
	    	</thead>
	    	<tbody>
	    		<tr>
	    			<td><strong>Coverage Depth</strong></td>
	    			<td>${pkgA?.coverageDepth} </td>
	    			<td>${pkgB?.coverageDepth} </td>
	    		</tr>
	    		<tr>
	    			<td><strong>Embargo</strong></td>
	    			<td>${pkgA?.embargo}</td>
	    			<td>${pkgB?.embargo}</td>
	    		</tr>
	    		<tr>
	    			<td><strong>Platform Host URL</strong></td>
	    			<td>${pkgA?.hostPlatformURL}</td>
	    			<td>${pkgB?.hostPlatformURL}</td>
	    		</tr>
	    		<tr>
	    			<td><strong>Hybrid OA</strong></td>
	    			<td>${pkgA?.hybridOA}</td>
	    			<td>${pkgB?.hybridOA}</td>
	    		</tr>	    		
	    	</tbody>  		
    	</table>
	</div>
</div>
