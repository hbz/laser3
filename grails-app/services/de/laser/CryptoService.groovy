package de.laser

import de.laser.config.ConfigMapper
import de.laser.utils.RandomUtils
import grails.gorm.transactions.Transactional

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.spec.KeySpec

@Transactional
class CryptoService {

    public final static String ALGORITHM_CIPHER = 'AES/CBC/PKCS5Padding'
    public final static String ALGORITHM_SKF    = 'PBKDF2WithHmacSHA256'
    public final static String ALGORITHM_SK     = 'AES'

    boolean encryptRawFile(File rawFile, Doc doc) {
        log.debug '[encryptRawFile] ' + rawFile.toPath() + ', doc #' + doc.id

        Path rfPath     = rawFile.toPath()
        File encTmpFile = encrypToTmpFile(rawFile, doc.ckey)
        File decTmpFile = decryptToTmpFile(encTmpFile, doc.ckey)

        if (validateFiles(decTmpFile, rawFile)) {
            log.debug '[encryptRawFile: OK] ' + rfPath
            Files.copy(encTmpFile.toPath(), rfPath, StandardCopyOption.REPLACE_EXISTING)
        }
        else {
            log.debug '[encryptRawFile: FAILED] ' + rfPath
            doc.ckey = null
            doc.save()
        }
        encTmpFile.delete()
        decTmpFile.delete()

        true
    }

    File encrypToTmpFile(File inFile, String ckey) {
        File outFile = File.createTempFile('doc_', '.enc', getTempDirectory())
        xcryptFile(inFile, outFile, Cipher.ENCRYPT_MODE, ckey)
        outFile
    }

    File decryptToTmpFile(File inFile, String ckey) {
        File outFile = File.createTempFile('doc_', '.dec', getTempDirectory())
        if (ckey) {
            xcryptFile(inFile, outFile, Cipher.DECRYPT_MODE, ckey)
        }
        else {
            log.debug'[decryptToTmpFile: FAILED] - copy as fallback'
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING) // todo: ERMS-6382 FALLBACK
        }
        outFile
    }

    void xcryptFile(File inFile, File outFile, int cipherMode, String ckey) {
        String passphrase = ConfigMapper.getDocumentStorageKey()
        String salt       = ckey.take(32)
        String iv         = ckey.takeRight(16)

        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM_SKF)
        KeySpec spec    = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), 65536, 256)
        SecretKey key   = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_SK)

//        byte[] iv = new byte[16]
//        new SecureRandom().nextBytes(iv)
//        IvParameterSpec ivspec = new IvParameterSpec(iv)

        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes())

        Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER)
        cipher.init(cipherMode, key, ivspec)

        FileInputStream fis  = new FileInputStream(inFile)
        FileOutputStream fos = new FileOutputStream(outFile)

        byte[] buffer = new byte[64]
        int bytesRead

        while ((bytesRead = fis.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead)
            if (output != null) {
                fos.write(output)
            }
        }

        byte[] outBytes = cipher.doFinal()
        if (outBytes != null) {
            fos.write(outBytes)
        }

        fis.close()
        fos.close()
    }

    boolean validateFiles(File aFile, File bFile) {
        (-1L == Files.mismatch(aFile.toPath(), bFile.toPath()))
    }

    String generateCKey() {
        RandomUtils.getAlphaNumeric(64)
    }

    File getTempDirectory() {
        File dir = new File(System.getProperty('java.io.tmpdir') + '/laser/docs')
        if (! dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
}