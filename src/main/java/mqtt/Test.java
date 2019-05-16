package mqtt;

/**
 * @author iwant
 * @date 19-5-15 19:53
 * @desc
 */
public class Test {

    public static void main(String[] args) {
        int t = 38783;
        System.out.println(String.format("%x", (t >>> 8)));
        System.out.println(String.format("%x", t));
        t = 65514;
        System.out.println(String.format("%x", (t >>> 8)));
        System.out.println(String.format("%x", t));
    }
}
