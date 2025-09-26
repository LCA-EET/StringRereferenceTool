import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ByteUtils {
    private static final ByteOrder _order = ByteOrder.LITTLE_ENDIAN;
    private static ByteBuffer _intBuffer;
    private static ByteBuffer _longBuffer;
    private static ByteBuffer _floatBuffer;
    public static void init(){
        _intBuffer = ByteBuffer.allocate(4);
        _intBuffer.order(_order);
        _longBuffer = ByteBuffer.allocate(8);
        _longBuffer.order(_order);
        _floatBuffer = ByteBuffer.allocate(4);
        _floatBuffer.order(_order);
    }

    public static int ExtractInt(byte[] decrypted, int index){
        return ByteBuffer.wrap(decrypted).order(_order).getInt(index);
    }

    public static byte[] IntToByteArray(int value) {
        return _intBuffer.putInt(0, value).array();
    }

    public static byte[] FloatToByteArray(float value){
        return  _floatBuffer.putFloat(value).array();
    }

    public static byte[] LongToByteArray(long value){
        return _longBuffer.putLong(0, value).array();
    }

    public static byte[] ArrayListToByteArray(ArrayList<byte[]> arrayList, int totalLength, int startIndex){
        byte[] toReturn = new byte[totalLength];
        int index = startIndex;
        for(byte[] bytes : arrayList){
            System.arraycopy(bytes, 0, toReturn, index , bytes.length);
            index += bytes.length;
        }
        return toReturn;
    }

    public static String BytesToUTF8(byte[] toConvert){
        return new String(toConvert, StandardCharsets.UTF_8);
    }

    public static String BytesToUTF8(byte[] decrypted, int index, int length)
    {
        byte[] nameBytes = new byte[length];
        System.arraycopy(decrypted, index, nameBytes, 0, length);
        return BytesToUTF8(nameBytes);
    }

}
