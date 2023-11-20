package com.example.ltm.entity;

public class Diem {
	private int id;
    private int diem;

    public Diem() {
    }

    public Diem(int id, int diem) {
        this.id = id;
        this.diem = diem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDiem() {
        return diem;
    }

    public void setDiem(int diem) {
        this.diem = diem;
    }
}
