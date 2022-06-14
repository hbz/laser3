package de.laser

import grails.plugin.springsecurity.annotation.Secured

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class ResourcesController {

    /**
     * Provides a file download. The filename has to be specified at the id position (i.e. the third parameter of the request path)
     *
     * @return the requested file
     * @deprecated was one of my first contributions to the app; file download is now handled in the respective cotroller calls
     */
    @Deprecated
    @Secured(['ROLE_USER'])
    def downloadFile() {
        //Get the current working server root to have an absolute path to work with
        String root = request.getSession().getServletContext().getRealPath("/")
        //Fetch file name from the ID parameter argument
        String filename = params.id
        //Establish file connection
        File f = new File(root+"resources/"+filename)
        //Check if file exists; if so, proceed
        if(f.exists()) {
            response.setContentType("application/octet-stream")
            response.setHeader("Content-disposition", "attachment;filename=\"${f.name}\"")
            response.outputStream << f.bytes
        }
        else render view: '/serverCodes/notFound404'
    }
}
