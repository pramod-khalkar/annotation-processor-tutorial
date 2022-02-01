package tutorial.consumer;

import tutorial.annotation.MakeBuilder;

/**
 * Date: 01/02/22
 * Time: 7:00 pm
 * This file is project specific to annotation-processor-tutorial
 * Author: Pramod Khalkar
 */
@MakeBuilder
public class Bike {
    public String name;
    public Integer cc;
    public Integer topSpeed;

    @Override
    public String toString() {
        return "Bike{" +
                "name='" + name + '\'' +
                ", cc=" + cc +
                ", topSpeed=" + topSpeed +
                '}';
    }
}
