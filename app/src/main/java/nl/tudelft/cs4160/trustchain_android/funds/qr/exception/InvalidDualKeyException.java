package nl.tudelft.cs4160.trustchain_android.funds.qr.exception;

public class InvalidDualKeyException extends QRWalletImportException {
    public InvalidDualKeyException(String message) {
        super(message);
    }
    public InvalidDualKeyException(Exception cause) {
        super(cause);
    }
}
