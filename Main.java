class test {
    int a;

    test(int t) {
        a = t;
    }
}

public class Main {

    static void swap(test a, test b) {
        test t;
        t = a;
        a = b;
        b = t;
    }

    public static void main(String[] args) {
        test a = new test(1);
        test b = new test(2);


    }
}



