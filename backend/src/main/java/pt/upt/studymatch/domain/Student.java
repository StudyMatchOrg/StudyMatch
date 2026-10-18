package pt.upt.studymatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String studentNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne(optional = false)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    @Column(nullable = false)
    private int enrolmentYear;

    protected Student() {
        // required by JPA
    }

    public Student(String studentNumber, String name, String email, Degree degree, int enrolmentYear) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.email = email;
        this.degree = degree;
        this.enrolmentYear = enrolmentYear;
    }

    public void update(String studentNumber, String name, String email, Degree degree, int enrolmentYear) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.email = email;
        this.degree = degree;
        this.enrolmentYear = enrolmentYear;
    }

    public Long getId() {
        return id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Degree getDegree() {
        return degree;
    }

    public int getEnrolmentYear() {
        return enrolmentYear;
    }
}
