package oleborn.client;

import oleborn.network.TCPConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Класс ClientWindow представляет графический интерфейс клиента для общения через TCP-соединение.
 * Этот класс предоставляет текстовую область для отображения сообщений, поле ввода для отправки
 * сообщений, а также управляет соединением с сервером через TCP.
 */
public class ClientWindow extends JFrame implements ActionListener {

    private final JTextArea textArea = new JTextArea(); // Основная текстовая область для отображения сообщений
    private final JTextField fieldInput = new JTextField(); // Поле для ввода сообщений
    private TCPConnection connection; // TCP соединение
    private JScrollPane scrollPane; // Область прокрутки для текстовой области

    private final ClientWindowSettings settings; // Настройки окна клиента
    private final TCPConnectionListenerImpl connectionListener; // Слушатель TCP-соединения

    /**
     * Конструктор ClientWindow инициализирует графический интерфейс и подключается к серверу.
     * В нем создаются основные компоненты окна, такие как текстовая область, поле для ввода сообщений
     * и область настроек. При создании окна также устанавливается TCP-соединение с сервером.
     */
    public ClientWindow() {
        // Инициализация базовых настроек окна
        setTitle("Клиент чата");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setAlwaysOnTop(false);

        // Инициализация компонентов GUI
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        add(textArea, BorderLayout.CENTER);
        add(fieldInput, BorderLayout.SOUTH);

        // Инициализация настроек окна и слушателей
        settings = new ClientWindowSettings(this);
        scrollPane = new JScrollPane(textArea);  // Добавляем область прокрутки
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  // Всегда показывать вертикальную прокрутку
        add(scrollPane, BorderLayout.CENTER);  // Добавляем JScrollPane вместо JTextArea

        add(settings.getPanel(), BorderLayout.NORTH); // Добавляем панель настроек в верхнюю часть окна

        connectionListener = new TCPConnectionListenerImpl(this); // Создаем слушателя соединения

        fieldInput.addActionListener(this); // Привязка обработчика событий для текстового поля

        setVisible(true); // Делаем окно видимым

        // Подключение к серверу с начальной конфигурацией (IP и порт)
        connectToServer(settings.getIp(), settings.getPort());
    }

    /**
     * Подключение к серверу по указанным IP и порту.
     * Если текущее соединение существует, оно разрывается, а затем создается новое TCP-соединение.
     *
     * @param ip   IP-адрес сервера
     * @param port Порт сервера
     */
    public void connectToServer(String ip, int port) {
        if (connection != null) connection.disconnect(); // Если соединение существует, разрываем его
        try {
            connection = new TCPConnection(connectionListener, ip, port); // Инициализация нового соединения с сервером
        } catch (IOException e) {
            printMessage("Исключение: " + e.getMessage()); // Отображаем сообщение об ошибке при подключении
        }
    }

    /**
     * Обрабатывает событие ввода текста в поле ввода.
     * Если текст введен, он отправляется через TCP-соединение на сервер с указанием текущего ника.
     *
     * @param e Событие, связанное с действием (ввод текста и нажатие Enter)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String message = fieldInput.getText(); // Получаем текст из поля ввода
        if (message.isEmpty()) return; // Если поле пустое, не отправляем сообщение
        fieldInput.setText(null); // Очищаем поле ввода
        connection.sendString(settings.getNickname() + ": " + message); // Отправляем сообщение с текущим ником
    }

    /**
     * Добавляет сообщение в текстовую область безопасным способом.
     * Этот метод используется для добавления сообщений в текстовую область из разных потоков.
     *
     * @param message Сообщение для добавления в текстовую область
     */
    public synchronized void printMessage(String message) {
        SwingUtilities.invokeLater(() -> { // Используем SwingUtilities для безопасного обновления GUI
            textArea.append(message + "\n"); // Добавляем сообщение в текстовую область
            textArea.setCaretPosition(textArea.getDocument().getLength()); // Прокручиваем текстовую область к последней строке
        });
    }
}
