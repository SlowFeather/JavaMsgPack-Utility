import java.util.ArrayList;

public class Person {
    public String name;
    public int age;
    public ArrayList<Integer> skill;
    public ArrayList<Person> friends;

    public Person() {
    }

    public Person(String name, int age, ArrayList<Integer> skill, ArrayList<Person> friends) {
        this.name = name;
        this.age = age;
        this.skill = skill;
        this.friends = friends;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public ArrayList<Integer> getSkill() {
        return this.skill;
    }

    public ArrayList<Integer> getFriend() {
        return this.friends;
    }
    
    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", skill=" + skill + ", firends=" + friends + '}';
    }
}
