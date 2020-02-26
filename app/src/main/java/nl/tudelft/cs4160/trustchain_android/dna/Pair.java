package nl.tudelft.cs4160.trustchain_android.dna;

public class Pair<T> {
    private T key;
    private T value;


    public Pair(T value, T key) {
        this.key = key;
        this.value = value;
    }


    public T Y() {
        return key;
    }

    public T X() {
        return value;
    }
}