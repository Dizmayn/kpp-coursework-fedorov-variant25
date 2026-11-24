package ua.edu.nupp.kpp.client;

/**
 * Помилка звернення до сервера: недоступний сервер, HTTP-код 400 і вище
 * або некоректний JSON. Текст повідомлення показується користувачу.
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
