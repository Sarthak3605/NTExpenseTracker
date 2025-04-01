package com.expenseTrackerProject.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "budget")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department; // Each department has one budget

     @Column(nullable = false)
    private Double totalBudget;

	@Column(nullable = false)
	private Double remainingBudget;

    @Column(nullable = false)
     private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

	@PrePersist // this sets the date or runs before the budget entity execute
	public void setDefaultDates(){
	 	if(this.startDate == null){
			this.startDate = LocalDate.now();
		}
		if(this.endDate == null){
			this.endDate = this.startDate.plusMonths(1);
		}
	}
}
