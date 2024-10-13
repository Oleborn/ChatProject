package oleborn.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Класс ClientWindowSettings предоставляет панель настроек для клиента.
 * Он позволяет пользователю вводить никнейм, IP-адрес и порт для подключения к серверу.
 * Также он содержит кнопку "Connect", которая инициализирует подключение.
 */
public class ClientWindowSettings {
    private final JPanel panel; // Панель настроек
    private final JTextField fieldNickname; // Поле для ввода никнейма
    private final JTextField fieldIp; // Поле для ввода IP-адреса
    private final JTextField fieldPort; // Поле для ввода порта
    private final JButton btnConnect; // Кнопка подключения

    private final ClientWindow clientWindow; // Ссылка на главное окно клиента

    /**
     * Конструктор ClientWindowSettings инициализирует элементы графического интерфейса (GUI) для панели настроек.
     * Он добавляет текстовые поля для ввода никнейма, IP-адреса и порта, а также кнопку для подключения к серверу.
     *
     * @param clientWindow ссылка на главное окно клиента, к которому относятся настройки.
     */
    public ClientWindowSettings(ClientWindow clientWindow) {
        this.clientWindow = clientWindow;

        // Инициализация панели с использованием GridBagLayout для управления выравниванием компонентов
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2); // Отступы между компонентами
        gbc.fill = GridBagConstraints.HORIZONTAL; // Растягивание компонентов по горизонтали

        // Поля и метки
        fieldNickname = new JTextField("Введите ваше имя", 0); // Начальный никнейм
        fieldIp = new JTextField("127.0.0.1", 10); // Начальный IP
        fieldPort = new JTextField("8888", 5); // Начальный порт
        btnConnect = new JButton("Подключение"); // Кнопка подключения

        // Добавление метки "Nickname"
        gbc.gridx = 0; // Колонка 0
        gbc.gridy = 0; // Строка 0
        gbc.anchor = GridBagConstraints.WEST; // Выравнивание метки по левому краю
        panel.add(new JLabel("Имя:"), gbc);

        // Добавление поля для ввода никнейма
        gbc.gridx = 1; // Колонка 1
        panel.add(fieldNickname, gbc);

        // Добавление метки "IP"
        gbc.gridx = 0;
        gbc.gridy = 1; // Переход на следующую строку
        panel.add(new JLabel("IP:"), gbc);

        // Добавление поля для ввода IP-адреса
        gbc.gridx = 1;
        panel.add(fieldIp, gbc);

        // Добавление метки "Port"
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Порт:"), gbc);

        // Добавление поля для ввода порта
        gbc.gridx = 1;
        panel.add(fieldPort, gbc);

        // Добавление кнопки "Connect"
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Кнопка растягивается на всю ширину панели
        panel.add(btnConnect, gbc);

        // Обработчик события для кнопки подключения
        btnConnect.addActionListener(this::onConnect);
    }

    /**
     * Возвращает панель настроек для отображения в графическом интерфейсе.
     *
     * @return панель JPanel с настройками.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Обработчик кнопки "Connect".
     * При нажатии на кнопку вызывается метод, который подключается к серверу с использованием введенных IP и порта.
     *
     * @param e объект события ActionEvent, который возникает при нажатии на кнопку.
     */
    private void onConnect(ActionEvent e) {
        clientWindow.connectToServer(getIp(), getPort()); // Подключение к серверу с указанными IP и портом
    }

    /**
     * Получает текущий введенный никнейм.
     *
     * @return никнейм, введенный пользователем.
     */
    public String getNickname() {
        return fieldNickname.getText();
    }

    /**
     * Получает текущий введенный IP-адрес.
     *
     * @return IP-адрес, введенный пользователем.
     */
    public String getIp() {
        return fieldIp.getText();
    }

    /**
     * Получает текущий введенный порт.
     *
     * @return номер порта, введенный пользователем.
     */
    public int getPort() {
        return Integer.parseInt(fieldPort.getText()); // Преобразование текста в целое число
    }
}
