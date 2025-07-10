package com.kzrdut.client.rpcClient.impl;

import com.kzrdut.client.rpcClient.RPCClient;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @Author 林落
 * @LastChangeDate 2025/2/21
 * @Version 3.0
 */
@Slf4j
public class
SimpleSocketRPCClient implements RPCClient {
    private String host;
    private int port;

    public SimpleSocketRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        RpcResponse rpcResponse = null;
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            rpcResponse = (RpcResponse) objectInputStream.readObject();
        } catch (UnknownHostException e) {
            log.error("未知的主机: {}", host);
        } catch (IOException e) {
            log.error("I/O 错误:  {}", e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("无法识别的类: {}", e.getMessage());
        }
        return rpcResponse;
    }

    @Override
    public void close() {

    }
}
