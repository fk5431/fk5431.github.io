package factory.pattern.filter_pattern;

/**
 * Created by fk5431 on 7/27/17.
 */
public class Person {
    private String name;
    private String sex;
    private String marital;

    public Person(String name, String sex, String marital){
        this.name = name;
        this.sex = sex;
        this.marital = marital;
    }
    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getMarital() {
        return marital;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setMarital(String marital) {
        this.marital = marital;
    }

    @Override
    public String toString() {
        return "Persion  name : " + this.name + "  sex  " + this.sex + "  marital  "  + this.marital;
    }
}
