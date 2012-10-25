package eu.htcl.android.portknocker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.net.*;

/**
 *
 * @author everard
 */
public class Knocker {

    public static boolean doKnock(String host, int port, String protocol) {
        Socket tcp_socket = null;
        DatagramSocket udp_socket = null;
        InetAddress inetAddress = null;

        try {
            if ("udp".equals(protocol)) {
                // udp knock (a ping would wait for a connection/resonse)
                udp_socket = new DatagramSocket();
                inetAddress = InetAddress.getByName(host);
                String message = "-";
                DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), inetAddress, port);
                udp_socket.send(request);
            } else {
                // tcp knock (a ping would wait for a connection/resonse)
                tcp_socket = new Socket();
                inetAddress = InetAddress.getByName(host);
                SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
                tcp_socket.connect(socketAddress, 100);
                //if (tcp_socket.isConnected()) {
                //    tcp_socket.close();
                //}
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (ConnectException e) {
            // We dismiss "connection refused" as knockd operates at link-layer
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (udp_socket != null) {
                udp_socket.close();
            }
            if (tcp_socket != null) {
                try {
                    tcp_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
