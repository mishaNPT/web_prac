package ru.cmc.web_prac.classes;

import lombok.*;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@ToString(exclude = "bookings")
@NoArgsConstructor
@AllArgsConstructor
public class Client implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "bonus_miles")
    private Integer bonusMiles = 0;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
}