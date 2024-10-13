package oleborn.network;

/**
 * Интерфейс {@code TCPConnectionListener} предоставляет слой абстракции для обработки
 * событий TCP-соединения в различных частях программы. Реализация этого интерфейса
 * позволяет различным компонентам по-разному реагировать на разные события, связанные
 * с TCP-соединениями.
 *
 * <p>Реализующие классы должны определить поведение для следующих событий:
 * <ul>
 *     <li>Готовность соединения</li>
 *     <li>Получение строкового сообщения</li>
 *     <li>Разрыв соединения</li>
 *     <li>Обработка исключений при соединении</li>
 * </ul>
 * </p>
 */
public interface TCPConnectionListener {

    /**
     * Вызывается, когда TCP-соединение установлено и готово к общению.
     *
     * @param connection установленное TCP-соединение
     */
    void onConnectionReady(TCPConnection connection);

    /**
     * Вызывается, когда строковое сообщение получено через TCP-соединение.
     *
     * @param connection TCP-соединение, через которое было получено сообщение
     * @param value полученное строковое сообщение
     */
    void onReceiveString(TCPConnection connection, String value);

    /**
     * Вызывается, когда TCP-соединение разорвано.
     *
     * @param connection разорванное TCP-соединение
     */
    void onConnectionDisconnect(TCPConnection connection);

    /**
     * Вызывается, когда происходит исключение во время работы с TCP-соединением.
     *
     * @param connection TCP-соединение, в котором возникло исключение
     * @param e исключение, возникшее во время работы с соединением
     */
    void onConnectionException(TCPConnection connection, Exception e);
}
