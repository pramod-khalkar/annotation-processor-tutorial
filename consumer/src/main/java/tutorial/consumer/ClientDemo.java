package tutorial.consumer;

/**
 * Date: 01/02/22
 * Time: 6:59 pm
 * This file is project specific to annotation-processor-tutorial
 * Author: Pramod Khalkar
 */
public class ClientDemo {

    public static void main(String[] args) {
        Bike bike = new BikeBuilder()
                .cc(1000)
                .topSpeed(300)
                .name("R1")
                .build();

        System.out.println(bike.toString());
    }
}
