package com.schemarise.alfa.runtime;

import schemarise.alfa.runtime.model.Expression;
import schemarise.alfa.runtime.model.Expression__LambdaExpr;
import com.schemarise.alfa.runtime_int.IntImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface passed into Builders and other classes that need to
 * support runtime behaviour. Also supports encryption/decryption functions that
 * can be implemented to encforce the required behaviour.
 */
public interface RuntimeContext extends PersistenceSupport, MessagingSupport {

    public static RuntimeContext getDefaultRuntimeContext() {
        return IntImpl.defaultRuntimeContext();
    }

    public static PersistenceSupport getDefaultPersistenceSupport() {
        return IntImpl.defaultRuntimeContext();
    }

    public static MessagingSupport getDefaultMessagingSupport() {
        return IntImpl.defaultRuntimeContext();
    }

    public static RuntimeContext createRuntimeContext(PersistenceSupport ps) {
        return IntImpl.createRuntimeContext(ps, getDefaultMessagingSupport());
    }

    public static RuntimeContext createRuntimeContext(PersistenceSupport ps, MessagingSupport ms) {
        return IntImpl.createRuntimeContext(ps, ms);
    }

    /**
     * Encrypt the given byte[]
     *
     * @param inputData Byte[] to encrypt
     * @return An encrypted byte[]
     */
    byte[] encrypt(byte[] inputData);

    /**
     * Decrypt the given byte[]
     *
     * @param inputData Byte[] to decrypt
     * @return An decrypted byte[]
     */
    byte[] decrypt(byte[] inputData);

    ILogger getLogger();

//    OutputStream compress(InputStream uncompressedStream );
//
//    OutputStream uncompress(InputStream compressedStream );

    /**
     * Compress the input bytes using a compression implementation
     *
     * @param inputData
     * @return
     */
    byte[] compress(byte[] inputData);

    /**
     * Decompress the input bytes using the reverse of the compression implementation
     *
     * @param inputData
     * @return
     */
    byte[] uncompress(byte[] inputData);

    IBuiltinFunctions getBuiltinFunctions();
}
