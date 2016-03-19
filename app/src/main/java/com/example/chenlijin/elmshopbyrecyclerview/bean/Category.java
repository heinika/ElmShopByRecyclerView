package com.example.chenlijin.elmshopbyrecyclerview.bean;

import java.util.List;

/**
 * Created by chenlijin on 2016/3/17.
 */
public class Category {
    private String sortName;
    private boolean seleted;
    private List<Team> teamList;

    public Category(String sortName, List<Team> teamList) {
        this.sortName = sortName;
        this.teamList = teamList;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public boolean isSeleted() {
        return seleted;
    }

    public void setSeleted(boolean seleted) {
        this.seleted = seleted;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList;
    }
}
