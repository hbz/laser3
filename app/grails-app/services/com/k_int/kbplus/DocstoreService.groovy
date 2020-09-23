package com.k_int.kbplus

import de.laser.helper.RDStore
import grails.transaction.Transactional
import org.apache.commons.io.IOUtils
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class DocstoreService {

  File zipDirectory(File directory) throws IOException {
    File testZip = File.createTempFile("bag.", ".zip");
    String path = directory.getAbsolutePath();
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(testZip));

    ArrayList<File> fileList = getFileList(directory);
    for (File file : fileList) {
      ZipEntry ze = new ZipEntry(file.getAbsolutePath().substring(path.length() + 1));
      zos.putNextEntry(ze);
  
      FileInputStream fis = new FileInputStream(file);
      IOUtils.copy(fis, zos);
      fis.close();
  
      zos.closeEntry();
    }
  
    zos.close();
    return testZip;
  }

  ArrayList<File> getFileList(File file) {
    ArrayList<File> fileList = new ArrayList<File>();
    if (file.isFile()) {
      fileList.add(file);
    }
    else if (file.isDirectory()) {
      for (File innerFile : file.listFiles()) {
        fileList.addAll(getFileList(innerFile));
      }
    }
    return fileList;
  }

    def unifiedDeleteDocuments(params) {

        // copied from / used in ..
        // licenseController
        // MyInstitutionsController
        // PackageController
        // SubscriptionDetailsController

        params.each { p ->
            if (p.key.startsWith('_deleteflag.') ) {
                String docctx_to_delete = p.key.substring(12)
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save(flush: true)
            }
            if (p.key.startsWith('_deleteflag"@.') ) { // PackageController
                String docctx_to_delete = p.key.substring(12);
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save(flush: true)
            }
        }

        if (params.deleteId) {
            String docctx_to_delete = params.deleteId
            log.debug("Looking up docctx ${docctx_to_delete} for delete")

            DocContext docctx = DocContext.get(docctx_to_delete)
            docctx.status = RDStore.DOC_CTX_STATUS_DELETED
            docctx.save(flush: true)
        }
    }
}
