package org.grails.plugins.fileviewer

/**
 * @author Himanshu Seth (himanshu@intelligrape.com)
 * @author Fabien Benichou (fabien.benichou@gmail.com)
 */

class FileLocations {
    List locations
    Integer linesCount
    Boolean areDoubleDotsAllowedInFilePath = false

    /**
     *
     * @param filePath
     * @return whether the path provided should be displayed or not
     */
    boolean isValidPath(String filePath) {
        boolean isValid = this.locations.any {filePath.startsWith(it)}
        if(isValid && !areDoubleDotsAllowedInFilePath) {
            isValid = !filePath.contains("..")
        }
        isValid
    }


}
