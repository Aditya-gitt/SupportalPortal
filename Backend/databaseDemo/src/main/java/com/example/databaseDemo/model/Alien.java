package com.example.databaseDemo.model;

import javax.persistence.*;

@Entity(name = "Alien")
public class Alien {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int aid;
    @Column(name = "aname", nullable = false)
    private String aname;

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public String getAname() {
        return aname;
    }

    public void setAname(String aname) {
        this.aname = aname;
    }

    @Override
    public String toString() {
        return "Alien{" +
                "aid=" + aid +
                ", aname='" + aname + '\'' +
                '}';
    }
}
