package com.schemarise.alfa.runtime_int;

import com.schemarise.alfa.runtime.*;
import schemarise.alfa.runtime.model.asserts.ConstraintType;
import schemarise.alfa.runtime.model.Expression;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class DefaultRuntimeContext implements RuntimeContext {
    private static String ALGORITHM = "RSA";
    private final IBuiltinFunctions builtins;
    private final PersistenceSupport persistenceSupport;
    private final MessagingSupport messagingSupport;
    private Cipher publicKeyCipher;
    private Cipher privateKeyCipher;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private ILogger logger = Logger.getOrCreateDefault();

    public DefaultRuntimeContext(PersistenceSupport ps, MessagingSupport ms) {
        setupKeyPair();
        builtins = IntImpl.createBuiltinFunctions(this);
        persistenceSupport = ps;
        messagingSupport = ms;
    }

    private static DefaultRuntimeContext self = new DefaultRuntimeContext(new DefaultPersistenceSupport(), new DefaultMessagingSupport());

    public static DefaultRuntimeContext getInstance() {
        return self;
    }

//    @Override
//    public <T> DefaultCompressed<T> createCompressed(Function<DataSupplier, T> c, byte[] v) {
//        return DefaultCompressed.fromValue(c, v);
//    }
//
//    @Override
//    public <T> DefaultEncrypted<T> createEncrypted(Function<DataSupplier, T> c, byte[] v) {
//        return DefaultEncrypted.fromValue(c, v);
//    }

    private byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    public void logKeyInfo() throws Exception {
        _logKeyInfo("Private", privateKey);
//        _logKeyInfo("Public", publicKey);
    }

    private void _logKeyInfo(String type, RSAKey key) throws Exception {
//        if ( key instanceof  RSAPublicKey ) {
//            RSAPublicKey rpub = (RSAPublicKey) key;
//            byte[] exponentBytes = rpub.getPublicExponent().toByteArray();
//            String exponentB64 = java.util.Base64.getEncoder().encodeToString(exponentBytes);
//            System.out.println(type + " DotNet Exponent: " + exponentB64);
//        }
//
//        byte[] modulusBytes = key.getModulus().toByteArray();
//
//        modulusBytes = stripLeadingZeros(modulusBytes);
//        String modulusB64 = java.util.Base64.getEncoder().encodeToString(modulusBytes);
//        System.out.println( type + " DotNet Modulus:\n" + modulusB64);


        KeyFactory kf = KeyFactory.getInstance("RSA");

        RSAPrivateCrtKeySpec ks = kf.getKeySpec(privateKey, RSAPrivateCrtKeySpec.class);
        StringBuffer sb = new StringBuffer();
        sb.append("<RSAKeyValue>\n");
        sb.append("    <Modulus>" + ks.getModulus() + "</Modulus>\n");
        sb.append("    <Exponent>" + ks.getPublicExponent() + "</Exponent>\n");
        sb.append("    <P>" + ks.getPrimeP() + "</P>\n");
        sb.append("    <Q>" + ks.getPrimeQ() + "</Q>\n");
        sb.append("    <DP>" + ks.getPrimeExponentP() + "</DP>\n");
        sb.append("    <DQ>" + ks.getPrimeExponentQ() + "</DQ>\n");
        sb.append("    <InverseQ>" + ks.getCrtCoefficient() + "</InverseQ>\n");
        sb.append("    <D>" + ks.getPrivateExponent() + "</D>\n");
        sb.append("</RSAKeyValue>");

        binToString("Modulus", ks.getModulus());
        binToString("Exponent", ks.getPublicExponent());
        binToString("P", ks.getPrimeP());
        binToString("Q", ks.getPrimeQ());
        binToString("DP", ks.getPrimeExponentP());
        binToString("DQ", ks.getPrimeExponentQ());
        binToString("InverseQ", ks.getCrtCoefficient());
        binToString("D", ks.getPrivateExponent());

        System.out.println(sb.toString());
    }

    private static void binToString(String type, BigInteger a) {
        byte[] b = a.toByteArray();
        b = stripLeadingZeros(b);
        String modulusB64 = java.util.Base64.getEncoder().encodeToString(b);
        System.out.println("private static string " + type + " = \"" + modulusB64 + "\";");
    }

    private static byte[] stripLeadingZeros(byte[] a) {
        int lastZero = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0) {
                lastZero = i;
            } else {
                break;
            }
        }
        lastZero++;
        byte[] result = new byte[a.length - lastZero];
        System.arraycopy(a, lastZero, result, 0, result.length);
        return result;
    }

    private void setupKeyPair() {
        try {
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);

            InputStream is = DefaultRuntimeContext.class.getClassLoader().getResource("alfa-sample-pke-keys/sample_private_key.der").openStream();
            byte[] keyBytes = readStream(is);
            PKCS8EncodedKeySpec specPrivate = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = (RSAPrivateKey) kf.generatePrivate(specPrivate);
            is.close();

            is = DefaultRuntimeContext.class.getClassLoader().getResource("alfa-sample-pke-keys/sample_public_key.der").openStream();
            keyBytes = readStream(is);
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(keyBytes);
            publicKey = (RSAPublicKey) kf.generatePublic(specPublic);
            is.close();

            publicKeyCipher = Cipher.getInstance(ALGORITHM);
            publicKeyCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            privateKeyCipher = Cipher.getInstance(ALGORITHM);
            privateKeyCipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (Throwable e) {
            throw new AlfaRuntimeException(ConstraintType.Unknown, "Failed to process keys", e);
        }
    }

    public byte[] encrypt(byte[] inputData) {
        try {
            byte[] encryptedBytes = publicKeyCipher.doFinal(inputData);
            return encryptedBytes;
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    public byte[] decrypt(byte[] inputData) {
        try {
            byte[] decryptedBytes = privateKeyCipher.doFinal(inputData);
            return decryptedBytes;
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public byte[] compress(byte[] data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzipStream = new GZIPOutputStream(out);

            gzipStream.write(data, 0, data.length);
            gzipStream.close();

            return out.toByteArray();
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public byte[] uncompress(byte[] data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(data));

            int len;
            byte[] buffer = new byte[1024];

            while ((len = gzipStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzipStream.close();
            out.close();

            return out.toByteArray();
        } catch (Exception e) {
            if (e instanceof AlfaRuntimeException)
                throw (AlfaRuntimeException) e;
            else
                throw new AlfaRuntimeException(ConstraintType.DataFormatError, e);
        }
    }

    @Override
    public IBuiltinFunctions getBuiltinFunctions() {
        return builtins;
    }

    @Override
    public <T extends AlfaObject> List<T> query(Optional<AlfaObject> currentObject, String entityName,
                                                Expression.CaseLambdaExpr condition, Map<String, Integer> sort, int limit, Optional<String> storeName) {
        return persistenceSupport.query(currentObject, entityName, condition, sort, limit, Optional.empty());
    }

    @Override
    public <T extends AlfaObject> Optional<T> lookup(String entityName, Key ok, Optional<String> storeName) {
        return persistenceSupport.lookup(entityName, ok, Optional.empty());
    }

    @Override
    public <T extends Entity> void save(T entity, Optional<String> storeName) {
        persistenceSupport.save(entity, Optional.empty());
    }

    @Override
    public <T extends AlfaObject> void publish(String queueName, T alfaObj) {
        messagingSupport.publish(queueName, alfaObj);
    }

    @Override
    public <T extends AlfaObject> Boolean keyExists(String entityName, Key ok, Optional<String> storeName) {
        return persistenceSupport.keyExists(entityName, ok, Optional.empty());
    }

    @Override
    public <T extends AlfaObject> boolean exists(String entityName, Expression.CaseLambdaExpr condition, Optional<String> storeName) {
        return persistenceSupport.exists(entityName, condition, Optional.empty());
    }
}
