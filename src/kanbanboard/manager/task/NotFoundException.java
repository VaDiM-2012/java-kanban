package kanbanboard.manager.task;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}