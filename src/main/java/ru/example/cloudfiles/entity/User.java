package ru.example.cloudfiles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "users")// indexes = @Index(name = "users_name", columnList = "name", unique = true))
@RequiredArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

          @Serial
          private static final long serialVersionUID = 1L;

          @Id
          @GeneratedValue(strategy = GenerationType.IDENTITY)
          @Column(name = "id", nullable = false)
          private Long id;

          @Size(max = 20)
          @NonNull
          @Column(name = "username", length = 20, nullable = false)
          private String username;

          @NonNull
          @Column(name = "password", nullable = false, columnDefinition = "TEXT")
          private String password;
}