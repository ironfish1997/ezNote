package service;

/**
 * 笔记本找不到时报这个异常
 */
public class NotebookNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotebookNotFoundException() {
    }

    public NotebookNotFoundException(String message) {
        super(message);
    }

    public NotebookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotebookNotFoundException(Throwable cause) {
        super(cause);
    }

    public NotebookNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
