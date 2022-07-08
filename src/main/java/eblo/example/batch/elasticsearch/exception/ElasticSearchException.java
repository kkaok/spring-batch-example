package eblo.example.batch.elasticsearch.exception;

public class ElasticSearchException extends RuntimeException {

    private static final long serialVersionUID = 2776672172925914809L;

    private String errorMessage;
    
    public ElasticSearchException() {
        super();
    }
    
    public ElasticSearchException(String message, String errorMessage, Throwable e) {
        super(message, e);
        this.errorMessage = errorMessage;
    }
    
    public ElasticSearchException(String message) {
        super(message);
    }

    public ElasticSearchException(String message, Throwable e) {
        super(message, e);
    }
    
    public ElasticSearchException(Throwable e) {
        super(e);
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
}
