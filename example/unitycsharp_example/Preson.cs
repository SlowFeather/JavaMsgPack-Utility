using MessagePack;
using System;
using System.Collections.Generic;
using System.Text;
using UnityEditor.Experimental.GraphView;

[MessagePackObject]
public class Person
{
    [Key(0)]
    public string name;
    [Key(1)]
    public int age;
    [Key(2)]
    public List<int> skill;
    [Key(3)]
    public List<Person> friends;
    public Person()
    {
    }

    public Person(String name, int age, List<int> skills, List<Person> people)
    {
        this.name = name;
        this.age = age;
        this.skill = skills;
        this.friends = people;
    }
}
