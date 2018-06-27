package nl.tudelft.cs4160.trustchain_android.stresstest;

public class UsernameGenerator {

    private static String NAME_TEMPLATE = "stress_test_user_";

    private static int count = 0;

    public static String getUsername() {
        String username = NAME_TEMPLATE + count;
        count++;
        return username;
    }
}
