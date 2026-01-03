package net.cynreub.weighday.domain.model;

import java.time.LocalDateTime;

public class WeightEntry {
    private int id;
    private double weight;
    private String note;
    private LocalDateTime date;
    private String userId;
    private Integer goalId;

    public WeightEntry(int id, double weight, String note, LocalDateTime date, String userId, Integer goalId) {
        this.id = id;
        this.weight = weight;
        this.note = note;
        this.date = date;
        this.userId = userId;
        this.goalId = goalId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }
}
