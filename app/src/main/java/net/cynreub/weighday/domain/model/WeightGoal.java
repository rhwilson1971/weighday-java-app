package net.cynreub.weighday.domain.model;

import java.time.LocalDate;

public class WeightGoal {
    private int id;
    private double goalWeight;
    private double startWeight;
    private LocalDate startDate;
    private LocalDate achievedDate;
    private String userId;

    public WeightGoal(int id, double goalWeight, double startWeight, LocalDate startDate, LocalDate achievedDate, String userId) {
        this.id = id;
        this.goalWeight = goalWeight;
        this.startWeight = startWeight;
        this.startDate = startDate;
        this.achievedDate = achievedDate;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getGoalWeight() { return goalWeight; }
    public void setGoalWeight(double goalWeight) { this.goalWeight = goalWeight; }

    public double getStartWeight() { return startWeight; }
    public void setStartWeight(double startWeight) { this.startWeight = startWeight; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getAchievedDate() { return achievedDate; }
    public void setAchievedDate(LocalDate achievedDate) { this.achievedDate = achievedDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
