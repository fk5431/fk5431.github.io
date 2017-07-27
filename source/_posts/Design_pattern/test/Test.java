package factory.pattern.filter_pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fk5431 on 7/27/17.
 */
public class Test {
    public static void main(String[] args) {
        List<Person> persons = new ArrayList<>();
        persons.add(new Person("霍一", "FEMALE", "MARRIED"));
        persons.add(new Person("邓二", "MALE", "MARRIED"));
        persons.add(new Person("张三", "MALE", "SINGLE"));
        persons.add(new Person("李四", "FEMALE", "MARRIED"));
        persons.add(new Person("王五", "MALE", "SINGLE"));
        persons.add(new Person("赵六", "FEMALE", "SINGLE"));
        persons.add(new Person("孙七", "MALE", "SINGLE"));
        persons.add(new Person("罗八", "MALE", "MARRIED"));
        persons.add(new Person("刘九", "FEMALE", "SINGLE"));
        persons.add(new Person("史十", "FEMALE", "SINGLE"));

        List<Person> malePerson = new MaleFilter().filter(persons);
        for(Person p : malePerson){
            System.out.println(p.toString());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        List<Person> singlePerson = new MaleFilter().filter(persons);
        for(Person p : singlePerson){
            System.out.println(p.toString());
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        List<Person> singleAndMalePerson = new FilterAnd(new MaleFilter(), new SingleFilter()).filter(persons);
        for(Person p : singleAndMalePerson){
            System.out.println(p.toString());
        }
    }
}
