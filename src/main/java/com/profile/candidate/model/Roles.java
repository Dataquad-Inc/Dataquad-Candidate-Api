package com.profile.candidate.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Roles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private UserType name;  //  (e.g., "Admin", "Manager", "Employee")

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserType getName() {
    return name;
  }

  public void setName(UserType name) {
    this.name = name;
  }
}
