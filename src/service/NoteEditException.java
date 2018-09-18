package service;

public class NoteEditException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoteEditException() {
    }

    public NoteEditException(String message) {
        super(message);
    }

    public NoteEditException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoteEditException(Throwable cause) {
        super(cause);
    }

    public NoteEditException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
