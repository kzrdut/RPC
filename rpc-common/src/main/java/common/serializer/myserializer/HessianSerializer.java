package common.serializer.myserializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import common.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
public class
HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("无法序列空对象");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(object);      // 将对象写入输出流
            return byteArrayOutputStream.toByteArray();     // 返回字节数组
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject();      // 从输入流读取对象
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public String toString() {
        return "Hessian";
    }
}
