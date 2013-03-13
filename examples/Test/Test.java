import java.util.Random;

public class Test implements Cloneable {
    public static void main(String[] args) {
        long l = new Random().nextLong();
        String s = "" + l;
        System.out.println(s);
    }
}