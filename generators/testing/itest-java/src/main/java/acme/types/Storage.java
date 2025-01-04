package acme.types;

import com.schemarise.alfa.runtime.*;
import com.schemarise.alfa.runtime.utils.DefaultTypeDescriptor;
import schemarise.alfa.runtime.model.UdtDataType;
import schemarise.alfa.runtime.model.UdtMetaType;

import java.util.Collections;
import java.util.Map;

/***
 * Storage is an example Alfa 'native' typedef type. It can be declared as
 * <pre>
 * typedefs {
 *     storage = native acme.types.Storage
 * }
 * </pre>
 *
 * Once declared, storage as be used a normal data type in Alfa.
 */
public class Storage implements NativeAlfaObject {
    private final String _size;

    /**
     * Create a new immutable Storage instance
     *
     * @param size Size of storage
     */
    public Storage(String size) {
        this._size = size;
    }

    @Override
    public int hashCode() {
        return _size.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Storage) {
            Storage rhs = (Storage) obj;
            return rhs._size.equals(_size);
        }
        return false;
    }

    @Override
    public String toString() {
        return _size;
    }

    @Override
    public TypeDescriptor descriptor() {
        return StorageDescriptor.INSTANCE;
    }

    @Override
    public Object get(String fieldName) {
        return _size;
    }

    /***
     * Convert object to a string representation, which also will be the string
     * that can be passed to the constructor to create a new instance.
     * @return
     */
    @Override
    public String encodeToString() {
        return _size;
    }

    /**
     * This class is required and used by Alfa JsonCodec classes.
     * A Storage builder is able to create a new immutable instance of Storage.
     */
    public static class $Builder implements Builder {
        private String value;

        @Override
        public <T extends AlfaObject> T build() {
            if (value == null)
                throw new NullPointerException("Value not assigned to storage builder");

            return (T) new Storage(value);
        }

        @Override
        public void modify(String fieldName, Object val) {
            value = (String) val;
        }

        @Override
        public Object get(String fieldName) {
            return value;
        }

        @Override
        public TypeDescriptor descriptor() {
            return null;
        }
    }

    /**
     * This class is required and used by Alfa JsonCodec classes.
     * Descriptor for the Storage class.
     */
    public static class StorageDescriptor extends DefaultTypeDescriptor {
        public static TypeDescriptor INSTANCE = new StorageDescriptor();
        private static UdtDataType udtDataType = UdtDataType.builder().setUdtType(UdtMetaType.nativeUdtType).setFullyQualifiedName(Storage.class.getName()).build();

        @Override
        public UdtDataType getUdtDataType() {
            return udtDataType;
        }

        @Override
        public Builder builder(IBuilderConfig cc) {
            return new $Builder();
        }


        @Override
        public <T extends AlfaObject> Map<String, FieldMeta<T>> getAllFieldsMeta() {
            return Collections.emptyMap();
        }
    }
}
