package com.profile.candidate.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class UserDetails {

  @Id
  @Column(unique = true, nullable = false)
  private String userId;

  private String userName;

  private String entity;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Roles> roles = new HashSet<>();
}
