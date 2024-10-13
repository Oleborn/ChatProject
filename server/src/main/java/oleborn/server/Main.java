package oleborn.server;

public class Main {
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.startServer(); // Запуск
        server.startManagementServer(9999); // Запуск сервера управления на порту 9999
    }
}
