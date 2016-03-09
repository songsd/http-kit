package org.httpkit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_ACCEPT;

/**
 * Created by ssd on 15/6/19.
 */
public class Test {
    private static ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws Exception{
        Selector selector = Selector.open();
        ServerSocketChannel sc = ServerSocketChannel.open();

        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_ACCEPT);

        SocketAddress sa = new InetSocketAddress("127.0.0.1",8081);
        sc.bind(sa);
        sc.register(selector, OP_ACCEPT);

        while(true){
            System.out.println("begin");
            int i = selector.select() ;

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if(key.isAcceptable()){
                    System.out.println("acceptable");
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel c = ch.accept();
                    c.configureBlocking(false);
                    c.register(selector,SelectionKey.OP_READ);

                }
                else if(key.isReadable()){
                    System.out.println("readable");
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    // Clear out our read buffer so it's ready for new data
                    readBuffer.clear();

                    // Attempt to read off the channel
                    int numRead;
                    try {
                        numRead = socketChannel.read(readBuffer);
                        System.out.println(numRead);
                    } catch (IOException e) {
                        key.cancel();
                        socketChannel.close();

                        System.out.println("Forceful shutdown");
                        return;
                    }

                    if (numRead == -1) {
                        System.out.println("Graceful shutdown");
                        key.channel().close();
                        key.cancel();

                        return;
                    }
                }
                iter.remove();
            }
        }
    }
}
