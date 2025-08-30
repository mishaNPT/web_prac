package ru.cmc.web_prac.classes;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "airlines")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Airline implements CommonEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "miles_rate", precision = 3, scale = 2)
    private BigDecimal milesRate = BigDecimal.ONE;

    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flight> flights;
}
