package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@ToString(exclude = {"programs", "completedExercises"})
@NoArgsConstructor
public class User extends BaseEntity {
    @NotBlank
    @Size(min = 2, max = 24)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 24)
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Min(10)
    @Max(120)
    @Column(nullable = false)
    private Integer age;

    @Min(20)
    @Max(300)
    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty experience;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Size(min = 4)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false)
    private LocalDate lastUpdated = LocalDate.now();

    @OneToMany(mappedBy = "user")
    private Set<Program> programs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ExerciseProgress> completedExercises = new LinkedHashSet<>();

    @Column
    private String profileImageUrl;

    @Column(nullable = false)
    private String theme = "dark";

    @Column(nullable = false)
    private String language = "en";

    @Column(nullable = false)
    private String measuringUnits = "kg";

    @ElementCollection
    @CollectionTable(name = "user_weight_entries", joinColumns = @JoinColumn(name = "user_id"))
    @OrderColumn(name = "entry_order")
    private List<WeightEntry> weightChanges = new ArrayList<>();

    @OneToMany(mappedBy = "observer", cascade = CascadeType.ALL)
    private Set<Notification> notifications = new HashSet<>();

    //trainer only todo
    // private Set<User> trainees = new HashSet<>();

    public User(String firstName, String lastName, String username, Role role, Gender gender,
                Integer age, Double weight, Difficulty experience, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.experience = experience;
        this.email = email;
        this.password = password;
        weightChanges.add(new WeightEntry(weight, LocalDate.now()));
    }

    public void setWeight(Double weight){
        this.weight = weight;
        weightChanges.add(new WeightEntry(weight, LocalDate.now()));
    }

    //test purposes only
    public void setWeight(Double weight, LocalDate date){
        this.weight = weight;
        weightChanges.add(new WeightEntry(weight, date));
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setObserver(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setObserver(null);
    }
}