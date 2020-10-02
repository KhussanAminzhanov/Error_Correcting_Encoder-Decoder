import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        Random random = new Random(a + b);
        int sum = 0;
        int intervalLength = Math.abs(b - a) + 1;
        int min = Math.min(a, b);
        for (int i = 0; i < n; i++) {
            sum += random.nextInt(intervalLength) + min;
        }
        System.out.println(sum);
    }
}