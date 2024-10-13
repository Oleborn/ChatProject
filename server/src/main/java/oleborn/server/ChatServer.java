package oleborn.server;

import oleborn.network.TCPConnection;
import oleborn.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс ChatServer представляет собой многопользовательский сервер для чата.
 * Сервер управляет подключениями клиентов, принимает и отправляет сообщения,
 * а также предоставляет методы для запуска и остановки сервера.
 */
public class ChatServer implements TCPConnectionListener, ServerController {

    private final CopyOnWriteArrayList<TCPConnection> connections = new CopyOnWriteArrayList<>(); // Список активных соединений
    private volatile boolean running; // Статус работы сервера
    private int port = 8888; // Порт для подключения
    private ServerSocket serverSocket; // Сокет для приема соединений
    private Thread serverThread; // Поток, в котором работает сервер
    private ServerManager serverManager; // Менеджер сервера для управления

    /**
     * Уведомление о готовности соединения.
     * Клиент успешно подключился к серверу.
     *
     * @param connection соединение клиента.
     */
    @Override
    public synchronized void onConnectionReady(TCPConnection connection) {
        connections.add(connection);
        sendToAll("Клиент подключился: " + connection); // Уведомление о подключении клиента
    }

    /**
     * Уведомление о получении сообщения от клиента.
     * Сообщение отправляется всем подключенным клиентам.
     *
     * @param connection соединение клиента.
     * @param value      сообщение от клиента.
     */
    @Override
    public synchronized void onReceiveString(TCPConnection connection, String value) {
        sendToAll(value); // Отправка полученного сообщения всем клиентам
    }

    /**
     * Уведомление о разрыве соединения.
     * Клиент отключился от сервера.
     *
     * @param connection соединение клиента.
     */
    @Override
    public synchronized void onConnectionDisconnect(TCPConnection connection) {
        connections.remove(connection);
        sendToAll("Клиент отключился: " + connection); // Уведомление об отключении клиента
    }

    /**
     * Уведомление о возникновении исключения в соединении.
     * Исключение обрабатывается, а клиент отключается.
     *
     * @param connection соединение клиента.
     * @param e          исключение, возникшее при работе с соединением.
     */
    @Override
    public synchronized void onConnectionException(TCPConnection connection, Exception e) {
        System.out.println("Исключение: " + e.getMessage()); // Сообщение об исключении
        connections.remove(connection);
    }

    /**
     * Отправляет сообщение всем подключенным клиентам.
     *
     * @param message сообщение для отправки.
     */
    private void sendToAll(String message) {
        for (TCPConnection connection : connections) connection.sendString(message); // Отправка сообщения каждому клиенту
    }

    /**
     * Запуск сервера. Если сервер уже запущен, выводится уведомление.
     */
    @Override
    public void startServer() {
        if (running) {
            System.out.println("Сервер уже запущен."); // Уведомление, если сервер уже работает
            return;
        }
        running = true;
        serverThread = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                serverSocket = server;
                System.out.println("Сервер запущен на порту " + port); // Сообщение о запуске сервера
                while (running) {
                    try {
                        new TCPConnection(this, server.accept()); // Прием нового соединения
                    } catch (IOException e) {
                        if (!running) {
                            System.out.println("Сервер остановлен."); // Сообщение об остановке сервера
                        } else {
                            System.out.println("Исключение: " + e.getMessage()); // Сообщение об исключении
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Не удалось запустить сервер: " + e.getMessage()); // Сообщение о невозможности запуска сервера
            } finally {
                stopServer(); // Остановка сервера при завершении работы
            }
        });
        serverThread.start(); // Запуск потока сервера
    }

    /**
     * Остановка сервера. Если сервер уже остановлен, выводится уведомление.
     */
    @Override
    public void stopServer() {
        if (!running) {
            System.out.println("Сервер не запущен."); // Уведомление, если сервер уже остановлен
            return;
        }
        running = false;
        try {
            for (TCPConnection connection : connections) {
                connection.disconnect(); // Отключение всех клиентов
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Закрытие серверного сокета
            }
            System.out.println("Сервер остановлен."); // Сообщение об остановке сервера
        } catch (IOException e) {
            System.out.println("Исключение при остановке сервера: " + e.getMessage()); // Сообщение об исключении при остановке сервера
        }
    }

    /**
     * Запуск сервера управления для мониторинга и управления основным сервером.
     *
     * @param managementPort порт для сервера управления.
     */
    public void startManagementServer(int managementPort) {
        serverManager = new ServerManager(this, managementPort);
        new Thread(serverManager).start(); // Запуск сервера управления в отдельном потоке
    }

    /**
     * Проверка, запущен ли сервер.
     *
     * @return true, если сервер запущен; false в противном случае.
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Установка порта сервера.
     * Порт можно изменить только тогда, когда сервер не запущен.
     *
     * @param port новый порт для сервера.
     */
    @Override
    public void setPort(int port) {
        if (isRunning()) {
            System.out.println("Невозможно изменить порт, пока сервер запущен. Остановите сервер сначала."); // Сообщение о невозможности изменить порт
        } else {
            this.port = port;
            System.out.println("Порт сервера установлен на: " + port); // Сообщение об успешном изменении порта
        }
    }

    /**
     * Полная остановка приложения. Останавливает сервер и сервер управления, если он запущен.
     * После этого программа завершает работу.
     */
    @Override
    public void fullStopApp() {
        System.out.println("Завершается работа сервера..."); // Сообщение о начале остановки сервера

        // Остановка основного потока сервера
        stopServer();

        if (serverManager != null) {
            serverManager.stopManager(); // Остановка сервера управления
        }

        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.join(); // Ожидание завершения работы потока сервера
            } catch (InterruptedException e) {
                System.out.println("Прерывание при остановке потока сервера: " + e.getMessage()); // Сообщение об ошибке при ожидании завершения потока
            }
        }

        System.out.println("Сервер полностью остановлен. Завершение работы приложения..."); // Сообщение об успешной полной остановке сервера

        // Завершение работы JVM
        System.exit(0);
    }
}
