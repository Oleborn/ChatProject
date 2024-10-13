package oleborn.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Класс ServerManager предоставляет интерфейс управления сервером через
 * подключение на определенный порт. Он поддерживает команды для запуска,
 * остановки, изменения порта и завершения работы сервера.
 */
public class ServerManager implements Runnable {

    private final ServerController serverController; // Контроллер для управления основным сервером
    private final int managementPort; // Порт для подключения к серверу управления
    private volatile boolean running; // Флаг, указывающий, запущен ли менеджер

    /**
     * Конструктор ServerManager, который принимает контроллер сервера и порт управления.
     *
     * @param serverController контроллер для управления сервером.
     * @param managementPort   порт для подключения клиентов управления.
     */
    public ServerManager(ServerController serverController, int managementPort) {
        this.serverController = serverController;
        this.managementPort = managementPort;
        this.running = true;
    }

    /**
     * Метод run() запускает сервер управления, который принимает команды
     * от клиентов, подключающихся к managementPort.
     * Сервер управления работает в отдельном потоке.
     */
    @Override
    public void run() {
        System.out.println("Сервер управления запущен на порту " + managementPort); // Сообщение о запуске менеджера сервера
        try (ServerSocket managementSocket = new ServerSocket(managementPort)) {
            while (running) {
                try {
                    Socket clientSocket = managementSocket.accept(); // Ожидание подключения клиента управления
                    handleClient(clientSocket); // Обработка команд от клиента
                } catch (IOException e) {
                    System.out.println("Исключение при принятии соединения управления: " + e.getMessage()); // Сообщение об ошибке при подключении клиента
                }
            }
        } catch (IOException e) {
            System.out.println("Не удалось запустить сервер управления: " + e.getMessage()); // Сообщение о невозможности запуска сервера управления
        }
        System.out.println("Сервер управления остановлен."); // Сообщение об остановке менеджера
    }

    /**
     * Обрабатывает команды клиента, подключенного к серверу управления.
     * Принимает команды и отправляет ответ клиенту.
     *
     * @param clientSocket сокет клиента управления.
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            String command;
            while ((command = in.readLine()) != null) {
                String response = handleCommand(command); // Обработка команды
                out.write(response + "\n"); // Отправка ответа клиенту
                out.flush(); // Очистка буфера вывода
            }
        } catch (IOException e) {
            System.out.println("Исключение при обработке клиента управления: " + e.getMessage()); // Сообщение об исключении при обработке клиента
        }
    }

    /**
     * Обрабатывает команду, полученную от клиента управления, и возвращает ответ.
     *
     * @param command строка с командой.
     * @return ответ на команду.
     */
    private String handleCommand(String command) {
        return switch (command.toLowerCase()) {
            case "start" -> {
                if (!serverController.isRunning()) {
                    serverController.startServer();
                    yield "Сервер запущен"; // Сообщение о запуске сервера
                }
                yield "Сервер уже работает"; // Уведомление, если сервер уже запущен
            }
            case "stop" -> {
                if (serverController.isRunning()) {
                    serverController.stopServer();
                    yield "Сервер остановлен"; // Сообщение об остановке сервера
                }
                yield "Сервер не запущен"; // Сообщение, если сервер не запущен
            }
            case "status" ->
                    serverController.isRunning() ? "Сервер работает" : "Сервер остановлен"; // Проверка статуса сервера

            case "fullstop" -> {
                serverController.fullStopApp();
                yield "Приложение остановлено"; // Сообщение о полной остановке приложения
            }
            default -> {
                if (command.startsWith("port ")) { // Команда для изменения порта
                    try {
                        int newPort = Integer.parseInt(command.split(" ")[1]);
                        serverController.setPort(newPort);
                        yield "Порт сервера изменен на " + newPort; // Сообщение об изменении порта
                    } catch (NumberFormatException e) {
                        yield "Неверный номер порта"; // Сообщение об ошибке в номере порта
                    }
                }
                yield "Неизвестная команда"; // Сообщение о неизвестной команде
            }
        };
    }

    /**
     * Останавливает сервер управления.
     * После вызова этого метода сервер управления перестает принимать новые подключения.
     */
    public void stopManager() {
        running = false; // Остановка сервера управления
    }
}
