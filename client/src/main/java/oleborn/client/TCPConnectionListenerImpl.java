package oleborn.client;

import oleborn.network.TCPConnection;
import oleborn.network.TCPConnectionListener;

public class TCPConnectionListenerImpl implements TCPConnectionListener {

    private final ClientWindow clientWindow; // Ссылка на основное окно

    public TCPConnectionListenerImpl(ClientWindow clientWindow) {
        this.clientWindow = clientWindow;
    }

    @Override
    public void onConnectionReady(TCPConnection connection) {
        clientWindow.printMessage("Подключение успешно осуществлено"); // Вывод сообщения при успешном подключении
    }

    @Override
    public void onReceiveString(TCPConnection connection, String value) {
        clientWindow.printMessage(value); // Вывод полученного сообщения
    }

    @Override
    public void onConnectionDisconnect(TCPConnection connection) {
        clientWindow.printMessage("Подключение прервано"); // Сообщение при отключении
    }

    @Override
    public void onConnectionException(TCPConnection connection, Exception e) {
        clientWindow.printMessage("Исключение: " + e.getMessage()); // Сообщение при возникновении ошибки
    }
}
