package oleborn.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Класс TCPConnection представляет собой TCP-соединение с использованием сокетов.
 * Он управляет подключением, отправкой и приемом сообщений, а также отслеживает
 * события соединения через интерфейс TCPConnectionListener.
 */
public class TCPConnection {

    private final Socket socket; // Сокет для TCP-соединения
    private Thread thread; // Поток для получения сообщений
    private final BufferedReader in; // Поток ввода для чтения сообщений
    private final BufferedWriter out; // Поток вывода для отправки сообщений

    private final TCPConnectionListener listener; // Слушатель для обработки событий соединения

    /**
     * Конструктор TCPConnection, который создает новое соединение по IP и порту.
     * Он открывает сокет на указанном IP и порте и инициализирует потоки ввода и вывода.
     *
     * @param listener слушатель событий соединения.
     * @param ip       IP-адрес сервера.
     * @param port     Порт сервера.
     * @throws IOException если не удается установить соединение.
     */
    public TCPConnection(TCPConnectionListener listener, String ip, int port) throws IOException {
        this(listener, new Socket(ip, port)); // Вызов другого конструктора с новым сокетом
    }

    /**
     * Конструктор TCPConnection, который принимает готовый сокет.
     * Он устанавливает слушателя, инициализирует потоки и запускает поток для получения сообщений.
     *
     * @param listener слушатель событий соединения.
     * @param socket   готовый сокет для подключения.
     * @throws IOException если не удается инициализировать потоки.
     */
    public TCPConnection(TCPConnectionListener listener, Socket socket) throws IOException {
        this.listener = listener;
        this.socket = socket;

        // Инициализация потоков для чтения и записи с использованием кодировки UTF-8
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        // Создание и запуск потока для приема сообщений
        thread = new Thread(() -> {
            try {
                listener.onConnectionReady(TCPConnection.this); // Уведомление о готовности соединения
                while (!thread.isInterrupted()) {
                    String msg = in.readLine(); // Чтение сообщения
                    if (msg != null) {
                        listener.onReceiveString(TCPConnection.this, msg); // Уведомление о получении сообщения
                    } else {
                        disconnect(); // Отключение при разрыве связи
                    }
                }
            } catch (IOException e) {
                listener.onConnectionException(TCPConnection.this, e); // Уведомление об исключении
            } finally {
                listener.onConnectionDisconnect(TCPConnection.this); // Уведомление об отключении
            }
        });
        thread.start(); // Запуск потока приема сообщений
    }

    /**
     * Отправка строки сообщения через TCP-соединение.
     * Метод синхронизирован для безопасной работы в многопоточной среде.
     *
     * @param msg сообщение, которое нужно отправить.
     */
    public synchronized void sendString(String msg) {
        try {
            out.write(msg + "\r\n"); // Запись строки с переносом строки
            out.flush(); // Очистка буфера и отправка сообщения
        } catch (IOException e) {
            listener.onConnectionException(TCPConnection.this, e); // Уведомление о возникшей ошибке
            disconnect(); // Разрыв соединения при ошибке
        }
    }

    /**
     * Отключение TCP-соединения.
     * Метод синхронизирован для безопасного разрыва соединения.
     * Останавливает поток приема сообщений и закрывает сокет.
     */
    public synchronized void disconnect() {
        thread.interrupt(); // Остановка потока приема сообщений
        try {
            socket.close(); // Закрытие сокета
            listener.onConnectionDisconnect(this); // Уведомление об отключении
        } catch (IOException e) {
            listener.onConnectionException(TCPConnection.this, e); // Уведомление об исключении
        }
    }

    /**
     * Переопределение метода toString для предоставления информации о соединении.
     *
     * @return строка с информацией о подключенном IP-адресе и порте.
     */
    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ":" + socket.getPort(); // Информация о соединении
    }
}
