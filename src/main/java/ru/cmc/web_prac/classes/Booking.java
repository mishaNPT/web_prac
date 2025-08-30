package ru.cmc.web_prac.classes;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Booking implements CommonEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", referencedColumnName = "id")
    private Flight flight;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now(); // Значение по умолчанию - текущее время

    @Column(name = "status", length = 20)
    private String status = "BOOKED"; // Значение по умолчанию

    @Column(name = "paid_with_miles")
    private Boolean paidWithMiles = false; // Значение по умолчанию

    @Column(name = "miles_used")
    private Integer milesUsed = 0; // Значение по умолчанию
}