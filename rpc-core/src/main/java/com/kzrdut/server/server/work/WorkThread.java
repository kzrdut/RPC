package com.kzrdut.server.server.work;

import com.kzrdut.server.provider.ServiceProvider;
import lombok.AllArgsConstructor;
import common.message.RpcRequest;
import common.message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@AllArgsConstructor
public class WorkThread implements Runnable {
    private Socket socket;
    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 读取客户端传过来的 rpcRequest
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            // 反射调用服务方法获取返回值
            RpcResponse rpcResponse = getResponse(rpcRequest);
            // 向客户端写入 rpcResponse
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        // 得到服务名
        String interfaceName = rpcRequest.getInterfaceName();
        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        try {
            // 反射调用方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service, rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return RpcResponse.fail("方法执行错误");
        }
    }
}
