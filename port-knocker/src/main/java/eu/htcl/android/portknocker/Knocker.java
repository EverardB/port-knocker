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

        try {
            if ("udp".equals(protocol)) {
                // udp knock (a ping would wait for a connection/resonse)
                DatagramSocket udp_socket = new DatagramSocket();
                InetAddress inetAddress = InetAddress.getByName(host);
                String message = "-";
                DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), inetAddress, port);
                udp_socket.send(request);
                udp_socket.close();
            } else {
                // tcp knock (a ping would wait for a connection/resonse)
                Socket tcp_socket = new Socket();
                InetAddress inetAddress = InetAddress.getByName(host);
                SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
                tcp_socket.connect(socketAddress, 1000);
                tcp_socket.close();
            }
        } catch (UnknownHostException e) {
            Log.i("PortKnocker", e.toString());
            //e.printStackTrace();
            return false;
        } catch (ConnectException e) {
            // We can ignore "connection refused" as knockd operates at link-layer
        } catch (IOException e) {
            Log.i("PortKnocker", e.toString());
            //e.printStackTrace();
            return false;
        } finally {
            // Do nothing
        }

        return true;
    }
}
